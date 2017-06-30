/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.generator;

import com.facebook.litho.annotations.OnCreateTreeProp;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;
import com.facebook.litho.specmodels.model.ClassNames;
import com.facebook.litho.specmodels.model.DelegateMethodModel;
import com.facebook.litho.specmodels.model.MethodParamModelFactory;
import com.facebook.litho.specmodels.model.SpecModel;
import com.facebook.litho.specmodels.model.TreePropModel;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link TreePropGenerator}
 */
public class TreePropGeneratorTest {
  private SpecModel mSpecModel = mock(SpecModel.class);
  private TreePropModel mTreeProp = mock(TreePropModel.class);
  private DelegateMethodModel mOnCreateTreePropMethodModel;

  @Before
  public void setUp() {
    mOnCreateTreePropMethodModel = new DelegateMethodModel(
        ImmutableList.<Annotation>of(new OnCreateTreeProp() {

          @Override
          public Class<? extends Annotation> annotationType() {
            return OnCreateTreeProp.class;
          }

        }),
        ImmutableList.of(Modifier.PROTECTED),
        "onCreateTreeProp",
        TypeName.BOOLEAN,
        ImmutableList.of(
            MethodParamModelFactory.create(
                mock(ExecutableElement.class),
                ClassNames.COMPONENT_CONTEXT,
                "componentContext",
                new ArrayList<Annotation>(),
                new ArrayList<AnnotationSpec>(),
                new ArrayList<Class<? extends Annotation>>(),
                null),
            MethodParamModelFactory.create(
                mock(ExecutableElement.class),
                TypeName.BOOLEAN,
                "prop",
                ImmutableList.of(createAnnotation(Prop.class)),
                new ArrayList<AnnotationSpec>(),
                new ArrayList<Class<? extends Annotation>>(),
                null),
            MethodParamModelFactory.create(
                mock(ExecutableElement.class),
                TypeName.INT,
                "state",
                ImmutableList.of(createAnnotation(State.class)),
                new ArrayList<AnnotationSpec>(),
                new ArrayList<Class<? extends Annotation>>(),
                null)),
        null);

    when(mTreeProp.getName()).thenReturn("treeProp");
    when(mTreeProp.getType()).thenReturn(TypeName.INT);

    when(mSpecModel.getContextClass()).thenReturn(ClassNames.COMPONENT_CONTEXT);
    when(mSpecModel.getComponentClass()).thenReturn(ClassNames.COMPONENT);
    when(mSpecModel.getComponentName()).thenReturn("Test");
    when(mSpecModel.getSpecName()).thenReturn("TestSpec");
    when(mSpecModel.getDelegateMethods())
        .thenReturn(ImmutableList.of(mOnCreateTreePropMethodModel));
    when(mSpecModel.getTreeProps()).thenReturn(ImmutableList.of(mTreeProp));
  }

  @Test
  public void testGenerate() {
    TypeSpecDataHolder typeSpecDataHolder =
        TreePropGenerator.generate(mSpecModel);

    assertThat(typeSpecDataHolder.getFieldSpecs()).isEmpty();
    assertThat(typeSpecDataHolder.getMethodSpecs()).hasSize(2);
    assertThat(typeSpecDataHolder.getTypeSpecs()).isEmpty();

    assertThat(typeSpecDataHolder.getMethodSpecs().get(0).toString()).isEqualTo(
        "@java.lang.Override\n" +
            "protected void populateTreeProps(com.facebook.litho.Component _abstractImpl,\n" +
            "    com.facebook.litho.TreeProps treeProps) {\n" +
            "  if (treeProps == null) {\n" +
            "    return;\n" +
            "  }\n" +
            "  final TestImpl _impl = (TestImpl) _abstractImpl;\n" +
            "  _impl.treeProp = treeProps.get(int.class);\n" +
            "}\n");

    assertThat(typeSpecDataHolder.getMethodSpecs().get(1).toString()).isEqualTo(
        "@java.lang.Override\n" +
        "protected com.facebook.litho.TreeProps getTreePropsForChildren(com.facebook.litho.ComponentContext c,\n" +
        "    com.facebook.litho.Component _abstractImpl, com.facebook.litho.TreeProps parentTreeProps) {\n" +
        "  final TestImpl _impl = (TestImpl) _abstractImpl;\n" +
        "  final com.facebook.litho.TreeProps childTreeProps = com.facebook.litho.TreeProps.copy(parentTreeProps);\n" +
        "  childTreeProps.put(boolean.class, TestSpec.onCreateTreeProp(\n" +
        "      (com.facebook.litho.ComponentContext) c,\n" +
        "      (boolean) _impl.prop,\n" +
        "      (int) _impl.state));\n" +
        "  return childTreeProps;\n" +
        "}\n");
  }

  private static Annotation createAnnotation(final Class<? extends Annotation> annotationClass) {
    return new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return annotationClass;
      }
    };
  }
}
