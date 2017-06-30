/**
 * Copyright 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE-examples file in the root directory of this source tree.
 */

package com.facebook.samples.litho.lithography;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.litho.ComponentContext;
import com.facebook.litho.ComponentLayout;
import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.PropDefault;
import com.facebook.litho.fresco.FrescoImage;

@LayoutSpec
public class SingleImageComponentSpec {
  @PropDefault
  protected static final float aspectRatio = 1f;

  @OnCreateLayout
  static ComponentLayout onCreateLayout(
      ComponentContext c,
      @Prop String image,
      @Prop(optional = true) float aspectRatio) {
    final DraweeController controller = Fresco.newDraweeControllerBuilder()
        .setUri(image)
        .build();
    return FrescoImage.create(c)
        .controller(controller)
        .aspectRatio(aspectRatio)
        .buildWithLayout();
  }
}
