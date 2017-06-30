/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import com.facebook.litho.testing.testrunner.ComponentsTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static com.facebook.litho.testing.ComponentTestHelper.measureAndLayout;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ComponentsTestRunner.class)
public class LifecycleMethodsTest {

  private enum LifecycleStep {
    ON_CREATE_LAYOUT,
    ON_PREPARE,
    ON_MEASURE,
    ON_BOUNDS_DEFINED,
    ON_CREATE_MOUNT_CONTENT,
    ON_MOUNT,
    ON_BIND,
    ON_UNBIND,
    ON_UNMOUNT
  }

  private LithoView mLithoView;
  private ComponentTree mComponentTree;
  private LifecycleMethodsComponent mLifecycle;
  private LifecycleMethodsInstance mComponent;

  @Before
  public void setup() throws Exception {
    mLithoView = new LithoView(RuntimeEnvironment.application);
    mLifecycle = new LifecycleMethodsComponent();
    mComponent = mLifecycle.create(10);

    final ComponentContext c = new ComponentContext(RuntimeEnvironment.application);
    mComponentTree = ComponentTree.create(c, mComponent)
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    mLithoView.setComponentTree(mComponentTree);
  }

  @Test
  public void testLifecycle() {
    mLithoView.onAttachedToWindow();
    measureAndLayout(mLithoView);

    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_BIND);

    mLithoView.onDetachedFromWindow();
    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_UNBIND);

    mLithoView.onAttachedToWindow();
    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_BIND);

    mComponentTree.setRoot(mLifecycle.create(20));
    measureAndLayout(mLithoView);
    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_UNMOUNT);

    mComponentTree.setRoot(mComponent);
    measureAndLayout(mLithoView);
    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_BIND);

    mLithoView.onDetachedFromWindow();
    mComponentTree.setRoot(mComponent);
    measureAndLayout(mLithoView);
    assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_UNBIND);
  }

  private class LifecycleMethodsComponent extends ComponentLifecycle {

    @Override
    protected ComponentLayout onCreateLayout(ComponentContext c, Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_CREATE_LAYOUT);

      return super.onCreateLayout(c, component);
    }

    @Override
    protected void onPrepare(ComponentContext c, Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_PREPARE);
    }

    @Override
    protected boolean canMeasure() {
      return true;
    }

    @Override
    protected void onMeasure(
        ComponentContext c,
        ComponentLayout layout,
        int widthSpec,
        int heightSpec,
        Size size,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_MEASURE);

      size.width = instance.getSize();
      size.height = instance.getSize();
    }

    @Override
    protected void onBoundsDefined(
        ComponentContext c,
        ComponentLayout layout,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_BOUNDS_DEFINED);
    }

    @Override
    protected Object onCreateMountContent(ComponentContext context) {
      mComponent.setCurrentStep(LifecycleStep.ON_CREATE_MOUNT_CONTENT);

      return new LifecycleMethodsDrawable(mComponent);
    }

    @Override
    protected void onMount(
        ComponentContext c,
        Object convertContent,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_MOUNT);
      final LifecycleMethodsDrawable d = (LifecycleMethodsDrawable) convertContent;

      d.setComponent(instance);
    }

    @Override
    protected void onUnmount(
        ComponentContext c,
        Object mountedContent,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_UNMOUNT);
    }

    @Override
    protected void onBind(
        ComponentContext c,
        Object mountedContent,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_BIND);
    }

    @Override
    protected void onUnbind(
        ComponentContext c,
        Object mountedContent,
        Component<?> component) {
      LifecycleMethodsInstance instance = (LifecycleMethodsInstance) component;
      instance.setCurrentStep(LifecycleStep.ON_UNBIND);
    }

    @Override
    protected boolean shouldUpdate(Component previous, Component next) {
      return true;
    }

    @Override
    public MountType getMountType() {
      return MountType.DRAWABLE;
    }

    public LifecycleMethodsInstance create(int size) {
      return new LifecycleMethodsInstance(this, size);
    }
  }

  private static class LifecycleMethodsInstance
      extends Component<LifecycleMethodsComponent> implements Cloneable {

    private final int mSize;
    LifecycleStep mCurrentStep = LifecycleStep.ON_UNMOUNT;

    protected LifecycleMethodsInstance(LifecycleMethodsComponent l, int size) {
      super(l);
      mSize = size;
    }

    @Override
    public String getSimpleName() {
      return "LifecycleMethodsInstance";
    }

    LifecycleStep getCurrentStep() {
      return mCurrentStep;
    }

    void setCurrentStep(LifecycleStep currentStep) {
      switch (currentStep) {
        case ON_CREATE_LAYOUT:
          assertThat(mCurrentStep).isEqualTo(LifecycleStep.ON_UNMOUNT);
          break;

        case ON_PREPARE:
          assertThat(mCurrentStep).isEqualTo(LifecycleStep.ON_CREATE_LAYOUT);
          break;

        case ON_MEASURE:
          assertThat(mCurrentStep == LifecycleStep.ON_PREPARE ||
              mCurrentStep == LifecycleStep.ON_MEASURE).isTrue();
          break;

        case ON_BOUNDS_DEFINED:
          assertThat(mCurrentStep == LifecycleStep.ON_PREPARE ||
              mCurrentStep == LifecycleStep.ON_MEASURE).isTrue();
          break;

        case ON_CREATE_MOUNT_CONTENT:
          assertThat(mCurrentStep == LifecycleStep.ON_BOUNDS_DEFINED).isTrue();

        case ON_MOUNT:
          assertThat(mCurrentStep == LifecycleStep.ON_BOUNDS_DEFINED ||
              mCurrentStep == LifecycleStep.ON_CREATE_MOUNT_CONTENT).isTrue();
          break;

        case ON_BIND:
          assertThat(mCurrentStep == LifecycleStep.ON_MOUNT ||
              mCurrentStep == LifecycleStep.ON_UNBIND).isTrue();
          break;

        case ON_UNBIND:
          assertThat(mCurrentStep).isEqualTo(LifecycleStep.ON_BIND);
          break;

        case ON_UNMOUNT:
          assertThat(mCurrentStep).isEqualTo(LifecycleStep.ON_UNBIND);
          break;
      }

      mCurrentStep = currentStep;
    }

    int getSize() {
      return mSize;
    }

    @Override
    public Component<LifecycleMethodsComponent> makeShallowCopy() {
      return this;
    }
  }

  private static class LifecycleMethodsDrawable extends Drawable {

    private LifecycleMethodsInstance mComponent;

    private LifecycleMethodsDrawable(LifecycleMethodsInstance component) {
      assertThat(component.getCurrentStep()).isEqualTo(LifecycleStep.ON_CREATE_MOUNT_CONTENT);
    }

    void setComponent(LifecycleMethodsInstance component) {
      mComponent = component;
      assertThat(mComponent.getCurrentStep()).isEqualTo(LifecycleStep.ON_MOUNT);
    }

    @Override
    public void setBounds(int l, int t, int r, int b) {
      super.setBounds(l, t, r, b);

      assertThat(mComponent.getCurrentStep() == LifecycleStep.ON_BIND ||
          mComponent.getCurrentStep() == LifecycleStep.ON_UNBIND).isTrue();
    }

    @Override
    public void draw(Canvas canvas) {
      assertThat(LifecycleStep.ON_BIND).isEqualTo(mComponent.getCurrentStep());
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
      return 0;
    }
  }
}
