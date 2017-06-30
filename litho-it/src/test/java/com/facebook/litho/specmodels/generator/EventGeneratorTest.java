/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */


package com.facebook.litho.specmodels.generator;

import com.facebook.litho.annotations.FromEvent;
import com.facebook.litho.annotations.OnEvent;
import com.facebook.litho.annotations.Param;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.PropDefault;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;
import com.facebook.litho.specmodels.model.ClassNames;
import com.facebook.litho.specmodels.model.EventDeclarationModel;
import com.facebook.litho.specmodels.model.SpecModel;
import com.facebook.litho.specmodels.processor.LayoutSpecModelFactory;
import com.google.testing.compile.CompilationRule;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link EventGenerator}
 */
public class EventGeneratorTest {
  @Rule public CompilationRule mCompilationRule = new CompilationRule();

  static class TestSpec<T extends CharSequence> {
    @PropDefault protected static boolean arg0 = true;

    @OnEvent(Object.class)
    public void testEventMethod1(
        @Prop boolean arg0,
        @State int arg1,
        @Param Object arg2,
        @Param T arg3,
        @FromEvent long arg4) {}

    @OnEvent(Object.class)
    public void testEventMethod2(
        @Prop boolean arg0,
        @State int arg1) {}
  }

  private SpecModel mSpecModel;
  private final SpecModel mMockSpecModel = mock(SpecModel.class);

  @Before
  public void setUp() {
    Elements elements = mCompilationRule.getElements();
    TypeElement typeElement = elements.getTypeElement(TestSpec.class.getCanonicalName());
    mSpecModel = LayoutSpecModelFactory.create(elements, typeElement, null);
    EventDeclarationModel eventDeclarationModel = new EventDeclarationModel(
        ClassName.OBJECT,
        ClassName.OBJECT,
        ImmutableList.of(
            new EventDeclarationModel.FieldModel(
                FieldSpec.builder(TypeName.INT, "field1", Modifier.PUBLIC).build(),
                new Object()),
            new EventDeclarationModel.FieldModel(
                FieldSpec.builder(TypeName.INT, "field2", Modifier.PUBLIC).build(),
                new Object())),
        new Object());
    when(mMockSpecModel.getEventDeclarations())
        .thenReturn(ImmutableList.of(eventDeclarationModel));
    when(mMockSpecModel.getContextClass()).thenReturn(ClassNames.COMPONENT_CONTEXT);
    when(mMockSpecModel.getComponentName()).thenReturn("Test");
  }

  @Test
  public void testGenerateEventMethods() {
    TypeSpecDataHolder dataHolder = EventGenerator.generateEventMethods(mSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(2);

    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "private void testEventMethod1(com.facebook.litho.HasEventDispatcher _abstractImpl,\n" +
            "    java.lang.Object arg2, T arg3) {\n" +
            "  TestImpl _impl = (TestImpl) _abstractImpl;\n" +
            "  TestSpec.testEventMethod1(\n" +
            "    (boolean) _impl.arg0,\n" +
            "    (int) _impl.mStateContainerImpl.arg1,\n" +
            "    arg2,\n" +
            "    arg3,\n" +
            "    (long) _impl.arg4);\n" +
            "}\n");

    assertThat(dataHolder.getMethodSpecs().get(1).toString())
        .isEqualTo(
            "private void testEventMethod2(com.facebook.litho.HasEventDispatcher _abstractImpl) {\n" +
            "  TestImpl _impl = (TestImpl) _abstractImpl;\n" +
            "  TestSpec.testEventMethod2(\n" +
            "    (boolean) _impl.arg0,\n" +
            "    (int) _impl.mStateContainerImpl.arg1);\n" +
            "}\n");
  }

  @Test
  public void testGenerateEventHandlerFactories() {
    TypeSpecDataHolder dataHolder = EventGenerator.generateEventHandlerFactories(mSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(4);

    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "public static <T extends java.lang.CharSequence> com.facebook.litho.EventHandler<java.lang.Object> testEventMethod1(com.facebook.litho.ComponentContext c,\n" +
            "    java.lang.Object arg2, T arg3) {\n" +
            "  return newEventHandler(c, -1400079064, new Object[] {\n" +
            "        c,\n" +
            "        arg2,\n" +
            "        arg3,\n" +
            "      });\n" +
            "}\n");

    assertThat(dataHolder.getMethodSpecs().get(1).toString())
        .isEqualTo(
            "public static <T extends java.lang.CharSequence> com.facebook.litho.EventHandler<java.lang.Object> testEventMethod1(com.facebook.litho.Component c,\n" +
            "    java.lang.Object arg2, T arg3) {\n" +
            "  return newEventHandler(c, -1400079064, new Object[] {\n" +
            "        c,\n" +
            "        arg2,\n" +
            "        arg3,\n" +
            "      });\n" +
            "}\n");

    assertThat(dataHolder.getMethodSpecs().get(2).toString())
        .isEqualTo(
            "public static com.facebook.litho.EventHandler<java.lang.Object> testEventMethod2(com.facebook.litho.ComponentContext c) {\n" +
            "  return newEventHandler(c, -1400079063, new Object[] {\n" +
            "        c,\n" +
            "      });\n" +
            "}\n");

    assertThat(dataHolder.getMethodSpecs().get(3).toString())
        .isEqualTo(
            "public static com.facebook.litho.EventHandler<java.lang.Object> testEventMethod2(com.facebook.litho.Component c) {\n" +
            "  return newEventHandler(c, -1400079063, new Object[] {\n" +
            "        c,\n" +
            "      });\n" +
            "}\n");
  }

  @Test
  public void testGenerateDispatchOnEvent() {
    assertThat(EventGenerator.generateDispatchOnEvent(mSpecModel).toString())
        .isEqualTo(
            "@java.lang.Override\n" +
            "public java.lang.Object dispatchOnEvent(final com.facebook.litho.EventHandler eventHandler,\n" +
            "    final java.lang.Object eventState) {\n" +
            "  int id = eventHandler.id;\n" +
            "  switch(id) {\n" +
            "    case -1400079064: {\n" +
            "      java.lang.Object _event = (java.lang.Object) eventState;\n" +
            "      testEventMethod1(\n" +
            "            eventHandler.mHasEventDispatcher,\n" +
            "            (java.lang.Object) eventHandler.params[0],\n" +
            "            (T) eventHandler.params[1]);\n" +
            "      return null;\n" +
            "    }\n" +
            "    case -1400079063: {\n" +
            "      java.lang.Object _event = (java.lang.Object) eventState;\n" +
            "      testEventMethod2(\n" +
            "            eventHandler.mHasEventDispatcher);\n" +
            "      return null;\n" +
            "    }\n" +
            "    default:\n" +
            "        return null;\n" +
            "  }\n" +
            "}\n");
  }

  @Test
  public void testGetEventHandlerMethods() {
    TypeSpecDataHolder dataHolder = EventGenerator.generateGetEventHandlerMethods(mMockSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(1);
    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "public static com.facebook.litho.EventHandler getObjectHandler(com.facebook.litho.ComponentContext context) {\n" +
            "  if (context.getComponentScope() == null) {\n" +
            "    return null;\n" +
            "  }\n" +
            "  return ((Test.TestImpl) context.getComponentScope()).objectHandler;\n" +
            "}\n");
  }

  @Test
  public void testGenerateEventDispatchers() {
    TypeSpecDataHolder dataHolder = EventGenerator.generateEventDispatchers(mMockSpecModel);

    assertThat(dataHolder.getMethodSpecs()).hasSize(1);
    assertThat(dataHolder.getMethodSpecs().get(0).toString())
        .isEqualTo(
            "static java.lang.Object dispatchObject(com.facebook.litho.EventHandler _eventHandler, int field1,\n" +
            "    int field2) {\n" +
            "  java.lang.Object _eventState = new java.lang.Object();\n" +
            "  _eventState.field1 = field1;\n" +
            "  _eventState.field2 = field2;\n" +
            "  com.facebook.litho.EventDispatcher _lifecycle = _eventHandler.mHasEventDispatcher.getEventDispatcher();\n" +
            "  return (java.lang.Object) _lifecycle.dispatchOnEvent(_eventHandler, _eventState);\n" +
            "}\n");
  }
}
