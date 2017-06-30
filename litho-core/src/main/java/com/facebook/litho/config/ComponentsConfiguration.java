/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.config;

import com.facebook.litho.BuildConfig;
import com.facebook.yoga.YogaLogger;

/**
 * Configuration for the Components library.
 */
public class ComponentsConfiguration {

  public static YogaLogger YOGA_LOGGER;

  /**
   * Indicates whether this is an internal build.
   * Note that the implementation of {@link BuildConfig} that this class is compiled against may not
   * be the one that is included in the APK. See: <a
   * href="http://facebook.github.io/buck/rule/android_build_config.html">android_build_config</a>.
   */
  public static final boolean IS_INTERNAL_BUILD = BuildConfig.IS_INTERNAL_BUILD;

  /**
   * Option to enabled debug mode. This will save extra data asscociated with each node and allow
   * more info about the hierarchy to be retrieved. Used to enable stetho integration.
   * It is highly discouraged to enable this in production builds. Due to how the Litho releases
   * are distributed in open source IS_INTERNAL_BUILD will always be false. It is therefore required
   * to override this value using your own application build configs. Recommended place for this is
   * in a Application subclass onCreate() method.
   */
  public static boolean isDebugModeEnabled = IS_INTERNAL_BUILD;

  /**
   * Debug option to highlight interactive areas in mounted components.
   */
  public static boolean debugHighlightInteractiveBounds = false;

  /**
   * Debug option to highlight mount bounds of mounted components.
   */
  public static boolean debugHighlightMountBounds = false;

  /**
   * Populates additional metadata to find mounted components at runtime. Defaults to the presence
   * of an <pre>IS_TESTING</pre> system property at startup but can be overridden at runtime.
   */
  public static boolean isEndToEndTestRun = System.getProperty("IS_TESTING") != null;

  /**
   * Use the new bootstrap ranges code instead of initializing all the items when the binder view
   * is measured (t12986103).
   */
  public static boolean bootstrapBinderItems = false;

  /**
   * Whether to use Object pooling via {@link com.facebook.litho.ComponentsPools}. This is switch
   * beacuse we are experimenting with turning off pooling to get a sense of what its impact is
   * in production.
   */
  public static volatile boolean usePooling = true;

  /**
   * Whether to enable incremental mount that operates directly from LithoView's methods.
   */
  public static boolean isIncrementalMountOnOffsetOrTranslationChangeEnabled = true;

  /**
   * Fixes an important perf bug in LayoutState output collection. We're gating it to better
   * understand the impact and implications of how perf move metrics and where bottlenecks are.
   */
  public static boolean collectResultFix = false;

  /**
   * Force all section component prop updates to be async
   */
  public static boolean sectionComponentsAsyncPropUpdates = false;

  /**
   * Force all section component state updates to be async
   */
  public static boolean sectionComponentsAsyncStateUpdates = false;
}
