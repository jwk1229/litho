// Copyright 2004-present Facebook. All Rights Reserved.

package com.facebook.litho.widget;

import android.support.v4.util.Pools.SynchronizedPool;
import android.support.v7.util.ListUpdateCallback;

import com.facebook.litho.Component;
import com.facebook.litho.ComponentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link ListUpdateCallback} that generates the relevant {@link Component}s
 * when an item is inserted/updated.
 *
 * The user of this API is expected to provide a ComponentRenderer implementation to build a
 * Component from a generic model object.
 *
 */
public class RecyclerBinderUpdateCallback<T> implements ListUpdateCallback {

  private static final SynchronizedPool<RecyclerBinderUpdateCallback> sUpdatesCallbackPool =
      new SynchronizedPool<>(4);

  public interface ComponentRenderer<T> {
    ComponentInfo render(T t, int idx);
  }

  public interface OperationExecutor {
    void executeOperations(List<Operation> operations);
  }

  private List<T> mData;
  private List<Operation> mOperations;
  private List<ComponentContainer> mPlaceholders;
  private ComponentRenderer mComponentRenderer;
  private OperationExecutor mOperationExecutor;

  public static<T> RecyclerBinderUpdateCallback<T> acquire(
      int oldDataSize,
      List<T> data,
      ComponentRenderer<T> componentRenderer,
      RecyclerBinder recyclerBinder) {

    return acquire(
        oldDataSize,
        data,
        componentRenderer,
        new RecyclerBinderOperationExecutor(recyclerBinder));
  }

  public static<T> RecyclerBinderUpdateCallback<T> acquire(
      int oldDataSize,
      List<T> data,
      ComponentRenderer<T> componentRenderer,
      OperationExecutor operationExecutor) {

    RecyclerBinderUpdateCallback instance = sUpdatesCallbackPool.acquire();
    if (instance == null) {
      instance = new RecyclerBinderUpdateCallback();
    }

    instance.init(oldDataSize, data, componentRenderer, operationExecutor);
    return instance;
  }

  public static<T> void release(RecyclerBinderUpdateCallback<T> updatesCallback) {
    final List<Operation> operations = updatesCallback.mOperations;
    for (int i = 0, size = operations.size(); i < size; i++) {
      operations.get(i).release();
    }
    updatesCallback.mOperations = null;

    updatesCallback.mData = null;
    for (int i = 0, size = updatesCallback.mPlaceholders.size(); i < size; i++) {
      updatesCallback.mPlaceholders.get(i).release();
    }
    updatesCallback.mComponentRenderer = null;
    updatesCallback.mOperationExecutor = null;
    sUpdatesCallbackPool.release(updatesCallback);
  }

  private RecyclerBinderUpdateCallback() {

  }

  private void init(
      int oldDataSize,
      List<T> data,
      ComponentRenderer<T> componentRenderer,
      OperationExecutor operationExecutor) {
    mData = data;
    mComponentRenderer = componentRenderer;
    mOperationExecutor = operationExecutor;

    mOperations = new ArrayList<>();
    mPlaceholders = new ArrayList<>();
    for (int i = 0; i < oldDataSize; i++) {
      mPlaceholders.add(ComponentContainer.acquire());
    }
  }

  @Override
  public void onInserted(int position, int count) {

    final List<ComponentContainer> placeholders = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      final int index = position + i;
      final ComponentContainer componentContainer = ComponentContainer.acquire();
      componentContainer.mNeedsComputation = true;
      mPlaceholders.add(index, componentContainer);
      placeholders.add(componentContainer);
    }

    mOperations.add(Operation.acquire(Operation.INSERT, position, -1, placeholders));
  }

  @Override
  public void onRemoved(int position, int count) {
    for (int i = 0; i < count; i++) {
      final ComponentContainer componentContainer = mPlaceholders.remove(position);
      componentContainer.release();
    }

    mOperations.add(Operation.acquire(Operation.DELETE, position, count, null));
  }

  @Override
  public void onMoved(int fromPosition, int toPosition) {
    mOperations.add(Operation.acquire(Operation.MOVE, fromPosition, toPosition, null));
    ComponentContainer placeholder = mPlaceholders.remove(fromPosition);
    mPlaceholders.add(toPosition, placeholder);
  }

  @Override
  public void onChanged(int position, int count, Object payload) {
    final List<ComponentContainer> placeholders = new ArrayList<>();

    for (int i = 0; i < count; i++) {
      final int index = position + i;
      final ComponentContainer placeholder = mPlaceholders.get(index);
      placeholder.mNeedsComputation = true;
      placeholders.add(placeholder);
    }

    mOperations.add(Operation.acquire(Operation.UPDATE, position, -1, placeholders));
  }

  public void applyChangeset() {
    for (int i = 0, size = mPlaceholders.size(); i < size; i++) {
      if (mPlaceholders.get(i).mNeedsComputation) {
        mPlaceholders.get(i).mComponentInfo =
            mComponentRenderer.render(mData.get(i), i);
      }
    }

    mOperationExecutor.executeOperations(mOperations);
  }

  public static class Operation {

    private static final SynchronizedPool<Operation>
        sOperationsPool = new SynchronizedPool<>(8);

    public static final int INSERT = 0;
    public static final int UPDATE = 1;
    public static final int DELETE = 2;
    public static final int MOVE = 3;

    private int mType;
    private int mIndex;
    private int mToIndex;
    private List<ComponentContainer> mComponentContainers;

    private Operation() {
    }

    private void init(
        int type,
        int index,
        int toIndex,
        List<ComponentContainer> placeholder) {
      mType = type;
      mIndex = index;
      mToIndex = toIndex;
      mComponentContainers = placeholder;
    }

    private static Operation acquire(
        int type,
        int index,
        int toIndex,
        List<ComponentContainer> placeholder) {
      Operation operation = sOperationsPool.acquire();
      if (operation == null) {
        operation = new Operation();
      }
      operation.init(type, index, toIndex, placeholder);

      return operation;
    }

    private void release() {
      if (mComponentContainers != null) {
        mComponentContainers.clear();
        mComponentContainers = null;
      }

      sOperationsPool.release(this);
    }

    public int getType() {
      return mType;
    }

    public int getIndex() {
      return mIndex;
    }

    public int getToIndex() {
      return mToIndex;
    }

    public List<ComponentContainer> getComponentContainers() {
      return mComponentContainers;
    }
  }

  public static class ComponentContainer {
    private static final SynchronizedPool<ComponentContainer> sComponentContainerPool =
        new SynchronizedPool<>(8);

    private ComponentInfo mComponentInfo;
    private boolean mNeedsComputation = false;

    public static ComponentContainer acquire() {
      ComponentContainer componentContainer = sComponentContainerPool.acquire();
      if (componentContainer == null) {
        componentContainer = new ComponentContainer();
      }

      return componentContainer;
    }

    public void release() {
      mComponentInfo = null;
      mNeedsComputation = false;
      sComponentContainerPool.release(this);
    }

    public ComponentInfo getComponentInfo() {
      return mComponentInfo;
    }
  }
}
