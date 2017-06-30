/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.reference;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.testing.testrunner.ComponentsTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.facebook.litho.reference.DrawableReference.create;
import static com.facebook.litho.reference.Reference.acquire;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(ComponentsTestRunner.class)
public class DrawableReferenceTest {

  @Test
  public void testAcquire() {
    Drawable drawable = new ColorDrawable();
    ComponentContext context = new ComponentContext(application);

    assertThat(drawable).isEqualTo(acquire(
        context,
        create()
            .drawable(drawable)
            .build()));
  }

}

