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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static com.facebook.litho.Column.create;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ComponentsTestRunner.class)
public class LayoutStateAddChildWithInputTest {
  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testNewEmptyLayout() {
    InternalNode node = (InternalNode) create(mContext)
        .child(TestLayoutComponent.create(mContext))
        .child(TestLayoutComponent.create(mContext))
        .build();

    assertThat(node.getChildCount()).isEqualTo(2);
    assertThat(node.getChildAt(0).getChildCount()).isEqualTo(0);
    assertThat(node.getChildAt(1).getChildCount()).isEqualTo(0);
  }
}
