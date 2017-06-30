/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */


package com.facebook.litho.animation;

import android.support.annotation.VisibleForTesting;
import android.support.v4.util.SimpleArrayMap;

import com.facebook.litho.dataflow.BindingListener;
import com.facebook.litho.dataflow.GraphBinding;
import com.facebook.litho.dataflow.ValueNode;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base class for defining animations for transitions between states of the component hierarchy.
 * Subclasses should define their animation by creating a {@link GraphBinding} in
 * {@link #setupBinding}.
 */
public abstract class TransitionAnimationBinding implements AnimationBinding {

  private final GraphBinding mGraphBinding;
  private final CopyOnWriteArrayList<AnimationBindingListener> mListeners =
      new CopyOnWriteArrayList<>();
  private final SimpleArrayMap<ComponentProperty, RuntimeValue> mAppearFromValues =
      new SimpleArrayMap<>();
  private final SimpleArrayMap<ComponentProperty, RuntimeValue> mDisappearToValues =
      new SimpleArrayMap<>();

  public TransitionAnimationBinding() {
    this(GraphBinding.create());
  }

  @VisibleForTesting
  TransitionAnimationBinding(GraphBinding graphBinding) {
    mGraphBinding = graphBinding;
    mGraphBinding.setListener(new BindingListener() {
      @Override
      public void onAllNodesFinished(GraphBinding binding) {
        TransitionAnimationBinding.this.onAllNodesFinished();
      }
    });
  }

  /**
   * @see GraphBinding#addBinding(ValueNode, ValueNode, String)
   */
  public void addBinding(ValueNode fromNode, ValueNode toNode, String inputName) {
    mGraphBinding.addBinding(fromNode, toNode, inputName);
  }

  /**
   * @see GraphBinding#addBinding(ValueNode, ValueNode)
   */
  public void addBinding(ValueNode fromNode, ValueNode toNode) {
    mGraphBinding.addBinding(fromNode, toNode);
  }

  /**
   * For appear animations, sets the value that the property should have before the animation
   * starts. See {@link RuntimeValue} for more info.
   */
  public void addAppearFromValue(ComponentProperty property, RuntimeValue value) {
    mAppearFromValues.put(property, value);
  }

  /**
   * For disappear animations, sets the value that the property should end at by the time the
   * animation ends and before the item is unmounted. See {@link RuntimeValue} for more info.
   */
  public void addDisappearToValue(ComponentProperty property, RuntimeValue value) {
    mDisappearToValues.put(property, value);
  }

  @Override
  public void start(Resolver resolver) {
    setupBinding(resolver);
    mGraphBinding.activate();
  }

  /**
   * Subclasses should set up their animation by creating a graph that defines how data will flow
   * to relevant {@link AnimatedPropertyNode}s.
   */
  protected abstract void setupBinding(Resolver resolver);

  @Override
  public void stop() {
    mGraphBinding.deactivate();
  }

  @Override
  public boolean isActive() {
    return mGraphBinding.isActive();
  }

  private void onAllNodesFinished() {
    for (AnimationBindingListener listener : mListeners) {
      listener.onFinish(this);
    }
    stop();
  }

  @Override
  public void collectAppearFromValues(SimpleArrayMap<ComponentProperty, RuntimeValue> outMap) {
    outMap.putAll(mAppearFromValues);
  }

  @Override
  public void collectDisappearToValues(SimpleArrayMap<ComponentProperty, RuntimeValue> outMap) {
    outMap.putAll(mDisappearToValues);
  }

  @Override
  public void addListener(AnimationBindingListener bindingListener) {
    mListeners.add(bindingListener);
  }

  @Override
  public void removeListener(AnimationBindingListener animationBindingListener) {
    mListeners.remove(animationBindingListener);
  }
}
