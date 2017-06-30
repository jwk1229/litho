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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;

import static com.facebook.litho.reference.Reference.acquire;
import static com.facebook.litho.reference.Reference.release;
import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(ComponentsTestRunner.class)
public class ReferenceTest {
  private ComponentContext mContext;

  @Before
  public void setup() {
    mContext = new ComponentContext(RuntimeEnvironment.application);
  }

  @Test
  public void testAcquireAndRelease() {
    TestReferenceLifecycle referenceLifecycle = new TestReferenceLifecycle();
    TestReference reference = new TestReference(referenceLifecycle);

    Drawable acquiredDrawable = acquire(mContext, reference);
    assertThat(referenceLifecycle.mAcquired).isTrue();

    release(mContext, acquiredDrawable, reference);
    assertThat(referenceLifecycle.mReleased).isTrue();
  }

  private static class TestReference extends Reference<Drawable> {
    private TestReference(ReferenceLifecycle<Drawable> lifecycle) {
      super(lifecycle);
    }

    @Override
    public String getSimpleName() {
      return "TestReference";
    }
  }

  private static class TestReferenceLifecycle extends ReferenceLifecycle<Drawable> {
    private boolean mAcquired;
    private boolean mReleased;

    @Override
    protected Drawable onAcquire(ComponentContext c, Reference<Drawable> reference) {
      mAcquired = true;
      return new ColorDrawable(0);
    }

    @Override
    protected void onRelease(ComponentContext c, Drawable value, Reference<Drawable> reference) {
      mReleased = true;
    }
  }
}
