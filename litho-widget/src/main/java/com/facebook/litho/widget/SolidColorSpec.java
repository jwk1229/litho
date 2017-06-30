/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.widget;

import android.graphics.drawable.ColorDrawable;

import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.Prop;

import static android.widget.ImageView.ScaleType.FIT_XY;
import static com.facebook.litho.annotations.ResType.COLOR;

/**
 * A component that renders a solid color.
 *
 * @uidocs
 * @prop color Color to be shown.
 */
@LayoutSpec
class SolidColorSpec {

  @OnCreateLayout
  static ComponentLayout onCreateLayout(
      ComponentContext c,
      @Prop(resType = COLOR) int color) {
    return Image.create(c)
        .scaleType(FIT_XY)
        .drawable(new ColorDrawable(color))
        .buildWithLayout();
  }
}
