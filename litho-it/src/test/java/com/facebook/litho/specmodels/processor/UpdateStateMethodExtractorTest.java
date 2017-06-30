/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import com.facebook.litho.annotations.OnCreateLayout;
import com.facebook.litho.annotations.OnEvent;
import com.facebook.litho.annotations.OnUpdateState;
import com.facebook.litho.annotations.Param;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;
import com.facebook.litho.specmodels.model.UpdateStateMethodModel;
import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import org.junit.Rule;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 * Tests {@link UpdateStateMethodExtractor}
 */
public class UpdateStateMethodExtractorTest {
  @Rule public CompilationRule mCompilationRule = new CompilationRule();

  static class TestClass {

    @OnCreateLayout
    public void ignored() {}

    @OnEvent(Object.class)
    public void alsoIgnored() {}

    @OnUpdateState
    public void testMethod(
        @Prop boolean testProp,
        @State int testState,
        @Param Object testPermittedAnnotation) {
      // Don't do anything.
    }
  }

  @Test
  public void testMethodExtraction() {
    Elements elements = mCompilationRule.getElements();
    TypeElement typeElement = elements.getTypeElement(TestClass.class.getCanonicalName());

    List<Class<? extends Annotation>> permittedParamAnnotations = new ArrayList<>();

    ImmutableList<UpdateStateMethodModel> methods =
        UpdateStateMethodExtractor.getOnUpdateStateMethods(
            typeElement,
            permittedParamAnnotations);

    assertThat(methods).hasSize(1);

    UpdateStateMethodModel updateStateMethod = methods.iterator().next();
    assertThat(updateStateMethod.annotation).isInstanceOf(OnUpdateState.class);

    assertThat(updateStateMethod.modifiers).hasSize(1);
    assertThat(updateStateMethod.modifiers).contains(Modifier.PUBLIC);

    assertThat(updateStateMethod.name.toString()).isEqualTo("testMethod");

    assertThat(updateStateMethod.returnType).isEqualTo(TypeName.VOID);

    assertThat(updateStateMethod.methodParams).hasSize(3);

    assertThat(updateStateMethod.methodParams.get(0).getName()).isEqualTo("arg0");
    assertThat(updateStateMethod.methodParams.get(0).getType()).isEqualTo(TypeName.BOOLEAN);
    assertThat(updateStateMethod.methodParams.get(0).getAnnotations()).hasSize(1);

    assertThat(updateStateMethod.methodParams.get(1).getName()).isEqualTo("arg1");
    assertThat(updateStateMethod.methodParams.get(1).getType()).isEqualTo(TypeName.INT);
    assertThat(updateStateMethod.methodParams.get(1).getAnnotations()).hasSize(1);

    assertThat(updateStateMethod.methodParams.get(2).getName()).isEqualTo("arg2");
    assertThat(updateStateMethod.methodParams.get(2).getType())
        .isEqualTo(ClassName.bestGuess("java.lang.Object"));
    assertThat(updateStateMethod.methodParams.get(2).getAnnotations()).hasSize(1);
  }
}
