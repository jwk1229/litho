/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.testing.assertj;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.facebook.litho.LithoView;
import com.facebook.litho.LithoViewTestHelper;
import com.facebook.litho.TestItem;
import com.facebook.litho.testing.viewtree.ViewTree;
import com.facebook.litho.testing.viewtree.ViewTreeAssert;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Java6Assertions;

import java.util.Deque;
import java.util.Locale;

/**
 * Assertion methods for {@link LithoView}s.
 *
 * <p> To create an instance of this class, invoke
 * <code>{@link LithoViewAssert#assertThat(LithoView)}</code>.
 */
public class LithoViewAssert extends AbstractAssert<LithoViewAssert, LithoView> {

  public static LithoViewAssert assertThat(LithoView actual) {
    return new LithoViewAssert(actual);
  }

  LithoViewAssert(LithoView actual) {
    super(actual, LithoViewAssert.class);
  }

  public LithoViewAssert containsTestKey(String testKey) {
    return containsTestKey(testKey, once());
  }

  public LithoViewAssert containsTestKey(String testKey, OccurrenceCount count) {
    final Deque<TestItem> testItems = LithoViewTestHelper.findTestItems(actual, testKey);
    Java6Assertions.assertThat(testItems)
        .hasSize(count.times)
        .overridingErrorMessage(
            "Expected to find test key <%s> in LithoView <%s> %s, but %s.",
            testKey,
            actual,
            count,
            testItems.isEmpty() ?
                "couldn't find it" :
                String.format(Locale.ROOT, "saw it %d times instead", testItems.size()))
        .isNotNull();

    return this;
  }

  @SuppressWarnings("VisibleForTests")
  public LithoViewAssert doesNotContainTestKey(String testKey) {
    final TestItem testItem = LithoViewTestHelper.findTestItem(actual, testKey);
    final Rect bounds = testItem == null ? null : testItem.getBounds();

    Java6Assertions.assertThat(testItem)
        .overridingErrorMessage(
            "Expected not to find test key <%s> in LithoView <%s>, but it was present at " +
                "bounds %s.",
            testKey,
            actual,
            bounds)
        .isNull();

    return this;
  }

  private ViewTreeAssert assertThatViewTree() {
    return ViewTreeAssert.assertThat(ViewTree.of(actual));
  }

  /**
   * Assert that any view in the given Component has the provided content
   * description.
   */
  public LithoViewAssert hasContentDescription(String contentDescription) {
    assertThatViewTree().hasContentDescription(contentDescription);

    return this;
  }

  /**
   * Assert that the given component contains the drawable identified by the provided drawable
   * resource id.
   */
  public LithoViewAssert hasVisibleDrawable(@DrawableRes int drawableRes) {
    assertThatViewTree().hasVisibleDrawable(drawableRes);

    return this;
  }

  /**
   * Assert that the given component contains the drawable provided.
   */
  public LithoViewAssert hasVisibleDrawable(Drawable drawable) {
    assertThatViewTree().hasVisibleDrawable(drawable);

    return this;
  }

  /**
   * Inverse of {@link #hasVisibleDrawable(Drawable)}
   */
  public LithoViewAssert doesNotHaveVisibleDrawable(Drawable drawable) {
    assertThatViewTree().doesNotHaveVisibleDrawable(drawable);

    return this;
  }

  /**
   * Inverse of {@link #hasVisibleDrawable(int)}
   */
  public LithoViewAssert doesNotHaveVisibleDrawable(@DrawableRes int drawableRes) {
    assertThatViewTree().doesNotHaveVisibleDrawable(drawableRes);

    return this;
  }

  /**
   * Assert that the given component has the exact text provided.
   */
  public LithoViewAssert hasVisibleText(String text) {
    assertThatViewTree().hasVisibleText(text);

    return this;
  }

  /**
   * Assert that the view tag is present for the given index.
   * @param tagId Index of the view tag.
   * @param tagValue View tag value.
   */
  public LithoViewAssert hasViewTag(int tagId, Object tagValue) {
    assertThatViewTree().hasViewTag(tagId, tagValue);

    return this;
  }

  public static OccurrenceCount times(int i) {
    return new OccurrenceCount(i, i + " times");
  }

  public static OccurrenceCount once() {
    return new OccurrenceCount(1, "once");
  }

  public static class OccurrenceCount {
    final int times;
    final String shortName;

    OccurrenceCount(int times, String shortName) {
      this.times = times;
      this.shortName = shortName;
    }

    public String toString() {
      return shortName;
    }
  }
}
