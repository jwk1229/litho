/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import com.facebook.litho.testing.TestLayoutComponent;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;
import com.facebook.litho.testing.util.InlineLayoutSpec;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ComponentsTestRunner.class)
public class LayoutStateEventHandlerTest {
  private int mUnspecifiedSizeSpec = 0; //SizeSpec.makeSizeSpec(0, SizeSpec.UNSPECIFIED);

  private Component mRootComponent;
  private Component mNestedComponent;

  private static void assertCorrectEventHandler(
      EventHandler eventHandler,
      int expectedId,
      Component<?> expectedInput) {
    assertThat(eventHandler.mHasEventDispatcher).isEqualTo(expectedInput);
    assertThat(eventHandler.id).isEqualTo(expectedId);
  }

  @Before
  public void setup() {
    mUnspecifiedSizeSpec = SizeSpec.makeSizeSpec(0, SizeSpec.UNSPECIFIED);
    mRootComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        assertCorrectEventHandler(c.newEventHandler(1), 1, mRootComponent);
        Layout.create(c, mNestedComponent).build();
        assertCorrectEventHandler(c.newEventHandler(2), 2, mRootComponent);
        Layout.create(c, mNestedComponent).build();
        assertCorrectEventHandler(c.newEventHandler(3), 3, mRootComponent);

        return TestLayoutComponent.create(c)
            .buildWithLayout();
      }
    };
    mNestedComponent = new InlineLayoutSpec() {
      @Override
      protected ComponentLayout onCreateLayout(ComponentContext c) {
        assertCorrectEventHandler(c.newEventHandler(1), 1, mNestedComponent);

        return TestLayoutComponent.create(c)
            .buildWithLayout();
      }
    };
  }

  @Test
  public void testNestedEventHandlerInput() {
    LayoutState.calculate(
        new ComponentContext(RuntimeEnvironment.application),
        mRootComponent,
        -1,
        mUnspecifiedSizeSpec,
        mUnspecifiedSizeSpec,
        false,
        false,
        null,
        false);
  }
}
