/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.model;

import com.facebook.litho.specmodels.internal.ImmutableList;
import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;

import javax.annotation.concurrent.Immutable;
import javax.lang.model.element.Modifier;

/**
 * Model that is an abstract representation of a method annotated with
 * {@link com.facebook.litho.annotations.OnUpdateState}.
 */
@Immutable
public final class UpdateStateMethodModel {
  public final Annotation annotation;
  public final ImmutableList<Modifier> modifiers;
  public final CharSequence name;
  public final TypeName returnType;
  public final ImmutableList<MethodParamModel> methodParams;
  public final Object representedObject;

  public UpdateStateMethodModel(
      Annotation annotation,
      ImmutableList<Modifier> modifiers,
      CharSequence name,
      TypeName returnType,
      ImmutableList<MethodParamModel> methodParams,
      Object representedObject) {
    this.annotation = annotation;
    this.modifiers = modifiers;
    this.name = name;
    this.returnType = returnType;
    this.methodParams = methodParams;
    this.representedObject = representedObject;
  }
}
