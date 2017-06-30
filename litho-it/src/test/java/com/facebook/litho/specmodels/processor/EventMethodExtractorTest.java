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
import com.facebook.litho.specmodels.model.EventMethodModel;
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
 * Tests {@link EventMethodExtractor}
 */
public class EventMethodExtractorTest {
  @Rule public CompilationRule mCompilationRule = new CompilationRule();

  static class TestClass {

    @OnCreateLayout
    public void ignored() {}

    @OnEvent(Object.class)
    public void testMethod(
        @Prop boolean testProp,
        @State int testState,
        @Param Object testPermittedAnnotation) {
      // Don't do anything.
    }

    @OnUpdateState
    public void alsoIgnored() {}
  }

  @Test
  public void testMethodExtraction() {
    Elements elements = mCompilationRule.getElements();
    TypeElement typeElement = elements.getTypeElement(TestClass.class.getCanonicalName());

    List<Class<? extends Annotation>> permittedParamAnnotations = new ArrayList<>();

    ImmutableList<EventMethodModel> methods =
        EventMethodExtractor.getOnEventMethods(
            elements,
            typeElement,
            permittedParamAnnotations);

    assertThat(methods).hasSize(1);

    EventMethodModel eventMethod = methods.iterator().next();
    assertThat(eventMethod.eventType.name).isEqualTo(ClassName.bestGuess("java.lang.Object"));

    assertThat(eventMethod.modifiers).hasSize(1);
    assertThat(eventMethod.modifiers).contains(Modifier.PUBLIC);

    assertThat(eventMethod.name.toString()).isEqualTo("testMethod");

    assertThat(eventMethod.returnType).isEqualTo(TypeName.VOID);

    assertThat(eventMethod.methodParams).hasSize(3);

    assertThat(eventMethod.methodParams.get(0).getName()).isEqualTo("arg0");
    assertThat(eventMethod.methodParams.get(0).getType()).isEqualTo(TypeName.BOOLEAN);
    assertThat(eventMethod.methodParams.get(0).getAnnotations()).hasSize(1);

    assertThat(eventMethod.methodParams.get(1).getName()).isEqualTo("arg1");
    assertThat(eventMethod.methodParams.get(1).getType()).isEqualTo(TypeName.INT);
    assertThat(eventMethod.methodParams.get(1).getAnnotations()).hasSize(1);

    assertThat(eventMethod.methodParams.get(2).getName()).isEqualTo("arg2");
    assertThat(eventMethod.methodParams.get(2).getType())
        .isEqualTo(ClassName.bestGuess("java.lang.Object"));
    assertThat(eventMethod.methodParams.get(2).getAnnotations()).hasSize(1);
  }
}
