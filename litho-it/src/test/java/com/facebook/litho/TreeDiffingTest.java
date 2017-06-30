/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.SparseArrayCompat;

import com.facebook.litho.testing.TestComponent;
import com.facebook.litho.testing.TestDrawableComponent;
import com.facebook.litho.testing.TestSizeDependentComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;
import com.facebook.yoga.YogaMeasureFunction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;

import static android.R.drawable.btn_default;
import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.graphics.Color.TRANSPARENT;
import static com.facebook.litho.Column.create;
import static com.facebook.litho.LayoutOutput.STATE_DIRTY;
import static com.facebook.litho.LayoutOutput.STATE_UNKNOWN;
import static com.facebook.litho.LayoutOutput.STATE_UPDATED;
import static com.facebook.litho.LayoutState.calculate;
import static com.facebook.litho.LayoutState.createDiffNode;
import static com.facebook.litho.SizeSpec.AT_MOST;
import static com.facebook.litho.SizeSpec.getMode;
import static com.facebook.litho.SizeSpec.getSize;
import static com.facebook.litho.SizeSpec.makeSizeSpec;
import static com.facebook.yoga.YogaAlign.FLEX_START;
import static com.facebook.yoga.YogaConstants.UNDEFINED;
import static com.facebook.yoga.YogaEdge.ALL;
import static com.facebook.yoga.YogaEdge.HORIZONTAL;
import static com.facebook.yoga.YogaMeasureMode.EXACTLY;
import static com.facebook.yoga.YogaMeasureOutput.getHeight;
import static com.facebook.yoga.YogaMeasureOutput.getWidth;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.reflect.Whitebox.getInternalState;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(ComponentsTestRunner.class)
public class TreeDiffingTest {

  private int mUnspecifiedSpec;

  private ComponentContext mContext;

  @Before
  public void setup() throws Exception {
    mContext = new ComponentContext(RuntimeEnvironment.application);
    mUnspecifiedSpec = SizeSpec.makeSizeSpec(0, SizeSpec.UNSPECIFIED);
  }

  @Test
  public void testDiffTreeDisabled() {
    final Component component = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    LayoutState layoutState = calculate(
        mContext,
        component,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        false,
        false,
        null,
        false);

    // Check diff tree is null.
    assertThat(layoutState.getDiffTree()).isNull();
  }

  @Test
  public void testDiffTreeEnabled() {
    final Component component = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    LayoutState layoutState = calculate(
        mContext,
        component,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    // Check diff tree is not null and consistent.
    DiffNode node = layoutState.getDiffTree();
    assertThat(node).isNotNull();
    assertThat(4).isEqualTo(countNodes(node));
  }

  private static int countNodes(DiffNode node) {
    int sum = 1;
    for (int i = 0; i < node.getChildCount(); i++) {
      sum += countNodes(node.getChildAt(i));
    }

    return sum;
  }

  private InternalNode createInternalNodeForMeasurableComponent(Component component) {
    InternalNode node = LayoutState.createTree(
            component,
            mContext);

    return node;
  }

  private long measureInternalNode(
          InternalNode node,
          float widthConstranint,
          float heightConstraint) {

    final YogaMeasureFunction measureFunc =
            Whitebox.getInternalState(
                    node.mYogaNode,
                    "mMeasureFunction");

    return measureFunc.measure(
            node.mYogaNode,
            widthConstranint,
            EXACTLY,
            heightConstraint,
            EXACTLY);
  }

  @Test
  public void testCachedMeasureFunction() {
    final Component component = TestDrawableComponent.create(mContext)
        .build();

    InternalNode node = createInternalNodeForMeasurableComponent(component);
    DiffNode diffNode = new DiffNode();
    diffNode.setLastHeightSpec(mUnspecifiedSpec);
    diffNode.setLastWidthSpec(mUnspecifiedSpec);
    diffNode.setLastMeasuredWidth(10);
    diffNode.setLastMeasuredHeight(5);
    diffNode.setComponent(component);

    node.setCachedMeasuresValid(true);
    node.setDiffNode(diffNode);

    long output = measureInternalNode(
        node,
        UNDEFINED,
        UNDEFINED);

    assertThat(getHeight(output) == (int) diffNode.getLastMeasuredHeight()).isTrue();
    assertThat(getWidth(output) == (int) diffNode.getLastMeasuredWidth()).isTrue();
  }

  @Test
  public void tesLastConstraints() {
    final Component component = TestDrawableComponent.create(mContext)
        .build();

    InternalNode node = createInternalNodeForMeasurableComponent(component);
    DiffNode diffNode = new DiffNode();
    diffNode.setLastWidthSpec(makeSizeSpec(10, SizeSpec.EXACTLY));
    diffNode.setLastHeightSpec(makeSizeSpec(5, SizeSpec.EXACTLY));
    diffNode.setLastMeasuredWidth(10f);
    diffNode.setLastMeasuredHeight(5f);
    diffNode.setComponent(component);

    node.setCachedMeasuresValid(true);
    node.setDiffNode(diffNode);

    long output = measureInternalNode(node, 10f, 5f);

    assertThat(getHeight(output) == (int) diffNode.getLastMeasuredHeight()).isTrue();
    assertThat(getWidth(output) == (int) diffNode.getLastMeasuredWidth()).isTrue();

    int lastWidthSpec = node.getLastWidthSpec();
    int lastHeightSpec = node.getLastHeightSpec();

    assertThat(getMode(lastWidthSpec) == SizeSpec.EXACTLY).isTrue();
    assertThat(getMode(lastHeightSpec) == SizeSpec.EXACTLY).isTrue();
    assertThat(getSize(lastWidthSpec) == 10).isTrue();
    assertThat(getSize(lastHeightSpec) == 5).isTrue();
  }

  @Test
  public void measureAndCreateDiffNode() {
    final Component component = TestDrawableComponent.create(mContext)
        .build();

    InternalNode node = createInternalNodeForMeasurableComponent(component);
    long output = measureInternalNode(
        node,
        UNDEFINED,
        UNDEFINED);

    node.setCachedMeasuresValid(false);
    DiffNode diffNode = createDiffNode(node, null);
    assertThat(getHeight(output) == (int) diffNode.getLastMeasuredHeight()).isTrue();
    assertThat(getWidth(output) == (int) diffNode.getLastMeasuredWidth()).isTrue();
  }

  @Test
  public void testCachedMeasures() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .build();
      }
    };

    LayoutState prevLayoutState = LayoutState.calculate(
            mContext,
            component1,
            -1,
            SizeSpec.makeSizeSpec(350, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(200, SizeSpec.EXACTLY),
            true,
            false,
            null,
            false);

    // Check diff tree is consistent.
    DiffNode node = prevLayoutState.getDiffTree();

    InternalNode layoutTreeRoot = LayoutState.createTree(
            component2,
            mContext);
    LayoutState.applyDiffNodeToUnchangedNodes(layoutTreeRoot, node);
    checkAllComponentsHaveMeasureCache(layoutTreeRoot);
  }

  @Test
  public void testPartiallyCachedMeasures() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .child(TestDrawableComponent.create(c))
                .build();
      }
    };

    LayoutState prevLayoutState = LayoutState.calculate(
            mContext,
            component1,
            -1,
            SizeSpec.makeSizeSpec(350, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(200, SizeSpec.EXACTLY),
            true,
            false,
            null,
            false);

    // Check diff tree is consistent.
    DiffNode node = prevLayoutState.getDiffTree();

    InternalNode layoutTreeRoot = LayoutState.createTree(
            component2,
            mContext);
    LayoutState.applyDiffNodeToUnchangedNodes(layoutTreeRoot, node);
    InternalNode child_1 = (InternalNode) layoutTreeRoot.getChildAt(0);
    assertCachedMeasurementsDefined(child_1);

    InternalNode child_2 = (InternalNode) layoutTreeRoot.getChildAt(1);
    assertCachedMeasurementsNotDefined(child_2);
    InternalNode child_3 = (InternalNode) child_2.getChildAt(0);
    assertCachedMeasurementsDefined(child_3);

    InternalNode child_4 = (InternalNode) layoutTreeRoot.getChildAt(2);
    assertCachedMeasurementsNotDefined(child_4);
  }

  @Test
  public void testLayoutOutputReuse() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    LayoutState prevLayoutState = calculate(
        mContext,
        component1,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    LayoutState layoutState = calculate(
        mContext,
        component2,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        prevLayoutState.getDiffTree(),
        false);

    assertThat(layoutState.getMountableOutputCount()).isEqualTo(prevLayoutState.getMountableOutputCount());
    for (int i = 0, count = prevLayoutState.getMountableOutputCount(); i < count; i++) {
      assertThat(layoutState.getMountableOutputAt(i).getId()).isEqualTo(prevLayoutState.getMountableOutputAt(i).getId());
    }
  }

  @Test
  public void testLayoutOutputPartialReuse() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return Column.create(c)
                .child(TestDrawableComponent.create(c))
                .child(
                        Column.create(c)
                                .child(TestDrawableComponent.create(c)))
                .child(TestDrawableComponent.create(c))
                .build();
      }
    };

    LayoutState prevLayoutState = LayoutState.calculate(
            mContext,
            component1,
            -1,
            SizeSpec.makeSizeSpec(350, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(200, SizeSpec.EXACTLY),
            true,
            false,
            null,
            false);
    LayoutState layoutState = LayoutState.calculate(
            mContext,
            component2,
            -1,
            SizeSpec.makeSizeSpec(350, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(200, SizeSpec.EXACTLY),
            true,
            false,
            prevLayoutState.getDiffTree(),
            false);

    assertNotEquals(
            prevLayoutState.getMountableOutputCount(),
            layoutState.getMountableOutputCount());
    for (int i = 0, count = prevLayoutState.getMountableOutputCount(); i < count; i++) {
      assertThat(layoutState.getMountableOutputAt(i).getId()).isEqualTo(prevLayoutState.getMountableOutputAt(i).getId());
    }
  }

  private void assertCachedMeasurementsNotDefined(InternalNode node) {
    assertThat(node.areCachedMeasuresValid()).isFalse();
  }

  private void checkAllComponentsHaveMeasureCache(InternalNode node) {
    if (node.getRootComponent() != null && node.getRootComponent().getLifecycle().canMeasure()) {
      assertCachedMeasurementsDefined(node);
    }
    int numChildren = node.getChildCount();
    for (int i = 0; i < numChildren; i++) {
      checkAllComponentsHaveMeasureCache((InternalNode) node.getChildAt(i));
    }
  }

  @Test
  public void testComponentHostMoveItem() {
    ComponentHost hostHolder = new ComponentHost(mContext);
    MountItem mountItem = new MountItem();
    MountItem mountItem1 = new MountItem();
    MountItem mountItem2 = new MountItem();
    hostHolder.mount(0, mountItem, new Rect());
    hostHolder.mount(1, mountItem1, new Rect());
    hostHolder.mount(2, mountItem2, new Rect());
    assertThat(mountItem).isEqualTo(hostHolder.getMountItemAt(0));
    assertThat(mountItem1).isEqualTo(hostHolder.getMountItemAt(1));
    assertThat(mountItem2).isEqualTo(hostHolder.getMountItemAt(2));
    hostHolder.moveItem(mountItem, 0, 2);
    hostHolder.moveItem(mountItem2, 2, 0);
    assertThat(mountItem2).isEqualTo(hostHolder.getMountItemAt(0));
    assertThat(mountItem1).isEqualTo(hostHolder.getMountItemAt(1));
    assertThat(mountItem).isEqualTo(hostHolder.getMountItemAt(2));
  }

  @Test
  public void testComponentHostMoveItemPartial() {
    ComponentHost hostHolder = new ComponentHost(mContext);
    MountItem mountItem = new MountItem();
    MountItem mountItem1 = new MountItem();
    MountItem mountItem2 = new MountItem();
    hostHolder.mount(0, mountItem, new Rect());
    hostHolder.mount(1, mountItem1, new Rect());
    hostHolder.mount(2, mountItem2, new Rect());
    assertThat(mountItem).isEqualTo(hostHolder.getMountItemAt(0));
    assertThat(mountItem1).isEqualTo(hostHolder.getMountItemAt(1));
    assertThat(mountItem2).isEqualTo(hostHolder.getMountItemAt(2));
    hostHolder.moveItem(mountItem2, 2, 0);
    assertThat(mountItem2).isEqualTo(hostHolder.getMountItemAt(0));
    assertThat(mountItem1).isEqualTo(hostHolder.getMountItemAt(1));

    assertThat(1).isEqualTo(((SparseArrayCompat<MountItem>)
        getInternalState(hostHolder, "mScrapMountItemsArray")).size());

    hostHolder.unmount(0, mountItem);

    assertThat(2).isEqualTo(((SparseArrayCompat<MountItem>)
        getInternalState(hostHolder, "mMountItems")).size());
    assertThat(getInternalState(hostHolder, "mScrapMountItemsArray")).isNull();
  }

  @Test
  public void testLayoutOutputUpdateState() {
    final Component firstComponent = TestDrawableComponent.create(mContext)
            .color(Color.BLACK)
            .build();
    final Component secondComponent = TestDrawableComponent.create(mContext)
            .color(Color.BLACK)
            .build();
    final Component thirdComponent = TestDrawableComponent.create(mContext)
            .color(Color.WHITE)
            .build();

    ComponentTree componentTree = ComponentTree.create(mContext, firstComponent)
            .incrementalMount(false)
            .layoutDiffing(false)
            .build();
    LayoutState state = componentTree.calculateLayoutState(
            null,
            mContext,
            firstComponent,
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            true,
            false,
            null);

    assertOutputsState(state, LayoutOutput.STATE_UNKNOWN);

    LayoutState secondState = componentTree.calculateLayoutState(
            null,
            mContext,
            secondComponent,
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            true,
            false,
            state.getDiffTree());

    assertOutputsState(secondState, LayoutOutput.STATE_UPDATED);

    LayoutState thirdState = componentTree.calculateLayoutState(
            null,
            mContext,
            thirdComponent,
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            SizeSpec.makeSizeSpec(10, SizeSpec.EXACTLY),
            true,
            false,
            secondState.getDiffTree());

    assertOutputsState(thirdState, LayoutOutput.STATE_DIRTY);
  }

  @Test
  public void testLayoutOutputUpdateStateWithBackground() {
    final Drawable redDrawable = new ColorDrawable(RED);
    final Drawable blackDrawable = new ColorDrawable(BLACK);
    final Drawable transparentDrawable = new ColorDrawable(TRANSPARENT);

    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(redDrawable)
            .foreground(transparentDrawable)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(redDrawable)
            .foreground(transparentDrawable)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component3 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(blackDrawable)
            .foreground(transparentDrawable)
            .child(TestDrawableComponent.create(c))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    ComponentTree componentTree = ComponentTree.create(mContext, component1)
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    LayoutState state = componentTree.calculateLayoutState(
        null,
        mContext,
        component1,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        null);

    assertOutputsState(state, STATE_UNKNOWN);

    LayoutState secondState = componentTree.calculateLayoutState(
        null,
        mContext,
        component2,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        state.getDiffTree());

    assertThat(5).isEqualTo(secondState.getMountableOutputCount());
    assertOutputsState(secondState, STATE_UPDATED);

    LayoutState thirdState = componentTree.calculateLayoutState(
        null,
        mContext,
        component3,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        secondState.getDiffTree());

    assertThat(5).isEqualTo(thirdState.getMountableOutputCount());
    assertThat(thirdState.getMountableOutputAt(1).getUpdateState()).isEqualTo(STATE_DIRTY);
    assertThat(thirdState.getMountableOutputAt(2).getUpdateState()).isEqualTo(STATE_UPDATED);
    assertThat(thirdState.getMountableOutputAt(3).getUpdateState()).isEqualTo(STATE_UPDATED);
    assertThat(thirdState.getMountableOutputAt(4).getUpdateState()).isEqualTo(STATE_UPDATED);
  }

  // This test covers the same case with the foreground since the code path is the same!
  @Test
  public void testLayoutOutputUpdateStateWithBackgroundInWithLayout() {
    final Drawable redDrawable = new ColorDrawable(RED);
    final Drawable blackDrawable = new ColorDrawable(BLACK);

    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(redDrawable)
            .foregroundRes(btn_default)
            .child(
                TestDrawableComponent.create(c)
                    .withLayout()
                    .background(blackDrawable))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(redDrawable)
            .foregroundRes(btn_default)
            .child(TestDrawableComponent.create(c)
                .withLayout()
                .background(blackDrawable))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component3 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .background(redDrawable)
            .foregroundRes(btn_default)
            .child(TestDrawableComponent.create(c)
                .withLayout()
                .background(redDrawable))
            .child(
                create(c)
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    ComponentTree componentTree = ComponentTree.create(mContext, component1)
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    LayoutState state = componentTree.calculateLayoutState(
        null,
        mContext,
        component1,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        null);

    assertThat(state.getMountableOutputAt(2).getUpdateState()).isEqualTo(STATE_UNKNOWN);

    LayoutState secondState = componentTree.calculateLayoutState(
        null,
        mContext,
        component2,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        state.getDiffTree());

    assertThat(secondState.getMountableOutputAt(2).getUpdateState()).isEqualTo(STATE_UPDATED);

    LayoutState thirdState = componentTree.calculateLayoutState(
        null,
        mContext,
        component3,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        secondState.getDiffTree());

    assertThat(thirdState.getMountableOutputAt(2).getUpdateState()).isEqualTo(STATE_DIRTY);
  }

  @Test
  public void testLayoutOutputUpdateStateIdClash() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(
                create(c)
                    .wrapInView()
                    .child(TestDrawableComponent.create(c)))
            .child(
                create(c)
                    .wrapInView()
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(
                create(c)
                    .wrapInView()
                    .child(TestDrawableComponent.create(c))
                    .child(TestDrawableComponent.create(c)))
            .child(
                create(c)
                    .wrapInView()
                    .child(TestDrawableComponent.create(c)))
            .build();
      }
    };

    ComponentTree componentTree = ComponentTree.create(mContext, component1)
        .incrementalMount(false)
        .layoutDiffing(false)
        .build();
    LayoutState state = componentTree.calculateLayoutState(
        null,
        mContext,
        component1,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        null);

    assertOutputsState(state, STATE_UNKNOWN);

    LayoutState secondState = componentTree.calculateLayoutState(
        null,
        mContext,
        component2,
        makeSizeSpec(10, SizeSpec.EXACTLY),
        makeSizeSpec(10, SizeSpec.EXACTLY),
        true,
        false,
        state.getDiffTree());

    assertThat(6).isEqualTo(secondState.getMountableOutputCount());
    assertThat(STATE_DIRTY).isEqualTo(secondState.getMountableOutputAt(0).getUpdateState());
    assertThat(STATE_UNKNOWN).isEqualTo(secondState.getMountableOutputAt(1).getUpdateState());
    assertThat(STATE_UPDATED).isEqualTo(secondState.getMountableOutputAt(2).getUpdateState());
    assertThat(STATE_UNKNOWN).isEqualTo(secondState.getMountableOutputAt(3).getUpdateState());
    assertThat(STATE_UNKNOWN).isEqualTo(secondState.getMountableOutputAt(4).getUpdateState());
    assertThat(STATE_UNKNOWN).isEqualTo(secondState.getMountableOutputAt(5).getUpdateState());
  }

  @Test
  public void testDiffTreeUsedIfRootMeasureSpecsAreDifferentButChildHasSame() {
    final TestComponent component = TestDrawableComponent.create(mContext)
        .color(BLACK)
        .build();

    final Component layoutComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .alignItems(FLEX_START)
            .child(Layout.create(c, component).heightPx(50))
            .build();
      }
    };

    LayoutState firstLayoutState = calculate(
        mContext,
        layoutComponent,
        0,
        makeSizeSpec(100, SizeSpec.EXACTLY),
        makeSizeSpec(100, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    assertThat(component.wasMeasureCalled()).isTrue();

    final TestComponent secondComponent = TestDrawableComponent.create(mContext)
        .color(BLACK)
        .build();

    final Component secondLayoutComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .alignItems(FLEX_START)
            .child(Layout.create(c, secondComponent).heightPx(50))
            .build();
      }
    };

    calculate(
        mContext,
        secondLayoutComponent,
        0,
        makeSizeSpec(100, SizeSpec.EXACTLY),
        makeSizeSpec(90, SizeSpec.EXACTLY),
        true,
        false,
        firstLayoutState.getDiffTree(),
        false);

    assertThat(secondComponent.wasMeasureCalled()).isFalse();
  }

  @Test
  public void testDiffTreeUsedIfMeasureSpecsAreSame() {
    final TestComponent component = TestDrawableComponent.create(mContext)
        .color(BLACK)
        .build();

    final Component layoutComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(component)
            .build();
      }
    };

    LayoutState firstLayoutState = calculate(
        mContext,
        layoutComponent,
        0,
        makeSizeSpec(100, SizeSpec.EXACTLY),
        makeSizeSpec(100, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    assertThat(component.wasMeasureCalled()).isTrue();

    final TestComponent secondComponent = TestDrawableComponent.create(mContext)
        .color(BLACK)
        .build();

    final Component secondLayoutComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .child(secondComponent)
            .build();
      }
    };

    calculate(
        mContext,
        secondLayoutComponent,
        0,
        makeSizeSpec(100, SizeSpec.EXACTLY),
        makeSizeSpec(100, SizeSpec.EXACTLY),
        true,
        false,
        firstLayoutState.getDiffTree(),
        false);

    assertThat(secondComponent.wasMeasureCalled()).isFalse();
  }

  @Test
  public void testCachedMeasuresForNestedTreeComponentDelegateWithUndefinedSize() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(ALL, 2)
            .child(
                TestSizeDependentComponent.create(c)
                    .setDelegate(true)
                    .withLayout()
                    .marginPx(ALL, 11))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(ALL, 2)
            .child(
                TestSizeDependentComponent.create(c)
                    .setDelegate(true)
                    .withLayout()
                    .marginPx(ALL, 11))
            .build();
      }
    };

    LayoutState prevLayoutState = calculate(
        mContext,
        component1,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    LayoutState layoutState = calculate(
        mContext,
        component2,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        prevLayoutState.getDiffTree(),
        false);

    // The nested root measure() was called in the first layout calculation.
    TestComponent prevNestedRoot =
        (TestComponent) prevLayoutState.getMountableOutputAt(2).getComponent();
    assertThat(prevNestedRoot.wasMeasureCalled()).isTrue();

    TestComponent nestedRoot = (TestComponent) layoutState.getMountableOutputAt(2).getComponent();
    assertThat(nestedRoot.wasMeasureCalled()).isFalse();
  }

  @Test
  public void testCachedMeasuresForNestedTreeComponentWithUndefinedSize() {
    final Component component1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(ALL, 2)
            .child(
                TestDrawableComponent.create(c, false, true, true, false, false))
            .child(
                TestSizeDependentComponent.create(c)
                    .setDelegate(false)
                    .withLayout()
                    .flexShrink(0)
                    .marginPx(ALL, 11))
            .build();
      }
    };

    final Component component2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(ALL, 2)
            .child(
                TestDrawableComponent.create(c, false, true, true, false, false))
            .child(
                TestSizeDependentComponent.create(c)
                    .setDelegate(false)
                    .withLayout()
                    .flexShrink(0)
                    .marginPx(ALL, 11))
            .build();
      }
    };

    LayoutState prevLayoutState = calculate(
        mContext,
        component1,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        null,
        false);

    LayoutState layoutState = calculate(
        mContext,
        component2,
        -1,
        makeSizeSpec(350, SizeSpec.EXACTLY),
        makeSizeSpec(200, SizeSpec.EXACTLY),
        true,
        false,
        prevLayoutState.getDiffTree(),
        false);

    // The nested root measure() was called in the first layout calculation.
    TestComponent prevMainTreeLeaf =
        (TestComponent) prevLayoutState.getMountableOutputAt(1).getComponent();
    assertThat(prevMainTreeLeaf.wasMeasureCalled()).isTrue();
    TestComponent prevNestedLeaf1 =
        (TestComponent) prevLayoutState.getMountableOutputAt(3).getComponent();
    assertThat(prevNestedLeaf1.wasMeasureCalled()).isTrue();
    TestComponent prevNestedLeaf2 =
        (TestComponent) prevLayoutState.getMountableOutputAt(4).getComponent();
    assertThat(prevNestedLeaf2.wasMeasureCalled()).isTrue();

    TestComponent mainTreeLeaf = (TestComponent) layoutState.getMountableOutputAt(1).getComponent();
    assertThat(mainTreeLeaf.wasMeasureCalled()).isFalse();
    TestComponent nestedLeaf1 = (TestComponent) layoutState.getMountableOutputAt(3).getComponent();
    assertThat(nestedLeaf1.wasMeasureCalled()).isFalse();
    TestComponent nestedLeaf2 = (TestComponent) layoutState.getMountableOutputAt(4).getComponent();
    assertThat(nestedLeaf2.wasMeasureCalled()).isFalse();
  }

  @Test
  public void testCachedMeasuresForCachedLayoutSpecWithMeasure() {
    final ComponentContext c = new ComponentContext(application);
    final int widthSpecContainer = makeSizeSpec(300, SizeSpec.EXACTLY);
    final int heightSpec = makeSizeSpec(40, AT_MOST);
    final int horizontalPadding = 20;
    final int widthMeasuredComponent = makeSizeSpec(
        getSize(widthSpecContainer) - horizontalPadding - horizontalPadding,
        SizeSpec.EXACTLY);

    final Component<?> sizeDependentComponentSpy1 = spy(
        TestSizeDependentComponent.create(c)
            .setFixSizes(false)
            .setDelegate(false)
            .build());
    Size sizeOutput1 = new Size();
    sizeDependentComponentSpy1.measure(
        c,
        widthMeasuredComponent,
        heightSpec,
        sizeOutput1);

    // Now embed the measured component in another container and calculate a layout.
    final Component rootContainer1 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(HORIZONTAL, horizontalPadding)
            .child(sizeDependentComponentSpy1)
            .build();
      }
    };

    final Component<?> sizeDependentComponentSpy2 = spy(
        TestSizeDependentComponent.create(c)
            .setFixSizes(false)
            .setDelegate(false)
            .build());
    Size sizeOutput2 = new Size();
    sizeDependentComponentSpy1.measure(
        c,
        widthMeasuredComponent,
        heightSpec,
        sizeOutput2);

    // Now embed the measured component in another container and calculate a layout.
    final Component rootContainer2 = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        return create(c)
            .paddingPx(HORIZONTAL, horizontalPadding)
            .child(sizeDependentComponentSpy2)
            .build();
      }
    };

    // Reset the release/clear counts before issuing calculate().
    reset(sizeDependentComponentSpy1);

    LayoutState prevLayoutState = calculate(
        mContext,
        rootContainer1,
        -1,
        widthSpecContainer,
        heightSpec,
        true,
        false,
        null,
        false);

    // Make sure we reused the cached layout and it wasn't released.
    verify(sizeDependentComponentSpy1, never()).releaseCachedLayout();
    verify(sizeDependentComponentSpy1, times(1)).clearCachedLayout();

    LayoutState layoutState = calculate(
        mContext,
        rootContainer2,
        -1,
        widthSpecContainer,
        heightSpec,
        true,
        false,
        prevLayoutState.getDiffTree(),
        false);

    // Make sure we reused the cached layout and it wasn't released.
    verify(sizeDependentComponentSpy2, never()).releaseCachedLayout();
    verify(sizeDependentComponentSpy2, never()).clearCachedLayout();

    // The nested root measure() was called in the first layout calculation.
    TestComponent prevNestedLeaf1 =
        (TestComponent) prevLayoutState.getMountableOutputAt(2).getComponent();
    assertThat(prevNestedLeaf1.wasMeasureCalled()).isTrue();
    TestComponent prevNestedLeaf2 =
        (TestComponent) prevLayoutState.getMountableOutputAt(3).getComponent();
    assertThat(prevNestedLeaf2.wasMeasureCalled()).isTrue();

    TestComponent nestedLeaf1 = (TestComponent) layoutState.getMountableOutputAt(2).getComponent();
    assertThat(nestedLeaf1.wasMeasureCalled()).isFalse();
    TestComponent nestedLeaf2 = (TestComponent) layoutState.getMountableOutputAt(3).getComponent();
    assertThat(nestedLeaf2.wasMeasureCalled()).isFalse();
  }

  private static void assertOutputsState(
          LayoutState layoutState,
          @LayoutOutput.UpdateState int state) {
    assertThat(STATE_DIRTY).isEqualTo(layoutState.getMountableOutputAt(0).getUpdateState());
    for (int i = 1; i < layoutState.getMountableOutputCount(); i++) {
      LayoutOutput output = layoutState.getMountableOutputAt(i);
      assertThat(state).isEqualTo(output.getUpdateState());
    }
  }

  private static void assertCachedMeasurementsDefined(InternalNode node) {
    float diffHeight = node.getDiffNode() == null ? -1 : node.getDiffNode().getLastMeasuredHeight();
    float diffWidth = node.getDiffNode() == null ? -1 : node.getDiffNode().getLastMeasuredWidth();
    assertThat(diffHeight != -1).isTrue();
    assertThat(diffWidth != -1).isTrue();
    assertThat(node.areCachedMeasuresValid()).isTrue();
  }
}
