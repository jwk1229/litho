/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.widget;

import android.support.v4.util.Pools;

import com.facebook.litho.Component;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentInfo;
import com.facebook.litho.ComponentTree;
import com.facebook.litho.LayoutHandler;
import com.facebook.litho.Size;
import com.facebook.litho.StateHandler;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A class used to store the data backing a {@link RecyclerBinder}. For each item the
 * ComponentTreeHolder keeps the {@link ComponentInfo} which contains the original {@link Component}
 * and either the {@link ComponentTree} or the {@link StateHandler} depending upon whether
 * the item is within the current working range or not.
 */
@ThreadSafe
public class ComponentTreeHolder {
  private static final Pools.SynchronizedPool<ComponentTreeHolder> sComponentTreeHoldersPool =
      new Pools.SynchronizedPool<>(8);

  @GuardedBy("this")
  private ComponentTree mComponentTree;
  @GuardedBy("this")
  private StateHandler mStateHandler;
  @GuardedBy("this")
  private ComponentInfo mComponentInfo;
  private boolean mIsTreeValid;
  private LayoutHandler mLayoutHandler;
  private boolean mCanPrefetchDisplayLists;

  static ComponentTreeHolder acquire(
      ComponentInfo componentInfo,
      LayoutHandler layoutHandler,
      boolean canPrefetchDisplayLists) {
    ComponentTreeHolder componentTreeHolder = sComponentTreeHoldersPool.acquire();
    if (componentTreeHolder == null) {
      componentTreeHolder = new ComponentTreeHolder();
    }
    componentTreeHolder.mComponentInfo = componentInfo;
    componentTreeHolder.mLayoutHandler = layoutHandler;
    componentTreeHolder.mCanPrefetchDisplayLists = canPrefetchDisplayLists;
    return componentTreeHolder;
  }

  synchronized void acquireStateHandlerAndReleaseTree() {
    acquireStateHandler();
    releaseTree();
  }

  synchronized void invalidateTree() {
    mIsTreeValid = false;
  }

  synchronized void clearStateHandler() {
    mStateHandler = null;
  }

  void computeLayoutSync(
      ComponentContext context,
      int widthSpec,
      int heightSpec,
      Size size) {
    final ComponentTree componentTree;
    final Component component;

    synchronized (this) {
      ensureComponentTree(context);

      componentTree = mComponentTree;
      component = mComponentInfo.getComponent();
    }

    componentTree.setRootAndSizeSpec(component, widthSpec, heightSpec, size);

    synchronized (this) {
      if (componentTree == mComponentTree && component == mComponentInfo.getComponent()) {
        mIsTreeValid = true;
      }
    }
  }

  void computeLayoutAsync(
      ComponentContext context,
      int widthSpec,
      int heightSpec) {
    final ComponentTree componentTree;
    final Component component;

    synchronized (this) {
      ensureComponentTree(context);

      componentTree = mComponentTree;
      component = mComponentInfo.getComponent();
    }

    componentTree.setRootAndSizeSpecAsync(component, widthSpec, heightSpec);

    synchronized (this) {
      if (mComponentTree == componentTree && component == mComponentInfo.getComponent()) {
        mIsTreeValid = true;
      }
    }
  }

  synchronized ComponentInfo getComponentInfo() {
    return mComponentInfo;
  }

  synchronized boolean isTreeValid() {
    return mIsTreeValid;
  }

  synchronized ComponentTree getComponentTree() {
    return mComponentTree;
  }

  synchronized void setComponentInfo(ComponentInfo componentInfo) {
    invalidateTree();
    mComponentInfo = componentInfo;
  }

  synchronized void release() {
    releaseTree();
    clearStateHandler();
    mComponentInfo = null;
    mLayoutHandler = null;
    mCanPrefetchDisplayLists = false;
    sComponentTreeHoldersPool.release(this);
  }

  @GuardedBy("this")
  private void ensureComponentTree(ComponentContext context) {
    if (mComponentTree == null) {
      mComponentTree = ComponentTree.create(context, mComponentInfo.getComponent())
          .layoutThreadHandler(mLayoutHandler)
          .stateHandler(mStateHandler)
          .canPrefetchDisplayLists(mCanPrefetchDisplayLists)
          .build();
    }
  }

  @GuardedBy("this")
  private void releaseTree() {
    if (mComponentTree != null) {
      mComponentTree.release();
      mComponentTree = null;
    }

    mIsTreeValid = false;
  }

  @GuardedBy("this")
  private void acquireStateHandler() {
    if (mComponentTree == null) {
      return;
    }

    mStateHandler = mComponentTree.getStateHandler();
  }
}
