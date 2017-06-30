/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import com.facebook.litho.annotations.LayoutSpec;
import com.facebook.litho.specmodels.model.DependencyInjectionHelper;
import com.facebook.litho.specmodels.model.LayoutSpecModel;

import org.junit.Before;
import org.junit.Test;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.facebook.litho.specmodels.processor.LayoutSpecModelFactory.create;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link LayoutSpecModelFactory}
 */
public class LayoutSpecModelFactoryTest {
  private static final String TEST_QUALIFIED_SPEC_NAME = "com.facebook.litho.TestSpec";
  private static final String TEST_QUALIFIED_COMPONENT_NAME = "com.facebook.litho.Test";

  Elements mElements = mock(Elements.class);
  TypeElement mTypeElement = mock(TypeElement.class);
  DependencyInjectionHelper mDependencyInjectionHelper =
      mock(DependencyInjectionHelper.class);
  LayoutSpec mLayoutSpec = mock(LayoutSpec.class);

  @Before
  public void setUp() {
    when(mElements.getDocComment(mTypeElement)).thenReturn("");
    when(mTypeElement.getQualifiedName()).thenReturn(new MockName(TEST_QUALIFIED_SPEC_NAME));
    when(mTypeElement.getAnnotation(LayoutSpec.class)).thenReturn(mLayoutSpec);
  }

  @Test
  public void testCreate() {
    LayoutSpecModel layoutSpecModel =
        create(mElements, mTypeElement, mDependencyInjectionHelper);

    assertThat(layoutSpecModel.getSpecName()).isEqualTo("TestSpec");
    assertThat(layoutSpecModel.getComponentName()).isEqualTo("Test");
    assertThat(layoutSpecModel.getSpecTypeName().toString()).isEqualTo(TEST_QUALIFIED_SPEC_NAME);
    assertThat(layoutSpecModel.getComponentTypeName().toString())
        .isEqualTo(TEST_QUALIFIED_COMPONENT_NAME);

    assertThat(layoutSpecModel.getDelegateMethods()).isEmpty();

    assertThat(layoutSpecModel.getProps()).isEmpty();

    assertThat(layoutSpecModel.hasInjectedDependencies()).isTrue();
    assertThat(layoutSpecModel.getDependencyInjectionHelper())
        .isSameAs(mDependencyInjectionHelper);
  }

  @Test
  public void testCreateWithSpecifiedName() {
    when(mLayoutSpec.value()).thenReturn("TestComponentName");
    LayoutSpecModel layoutSpecModel =
        LayoutSpecModelFactory.create(mElements, mTypeElement, mDependencyInjectionHelper);

    assertThat(layoutSpecModel.getSpecName()).isEqualTo("TestSpec");
    assertThat(layoutSpecModel.getComponentName()).isEqualTo("TestComponentName");
    assertThat(layoutSpecModel.getSpecTypeName().toString()).isEqualTo(TEST_QUALIFIED_SPEC_NAME);
    assertThat(layoutSpecModel.getComponentTypeName().toString())
        .isEqualTo("com.facebook.litho.TestComponentName");
  }

  private static class MockName implements Name {

    private final CharSequence mName;

    public MockName(final CharSequence name) {
      mName = name;
    }

    @Override
    public boolean contentEquals(CharSequence cs) {
      return mName.equals(cs);
    }

    @Override
    public int length() {
      return mName.length();
    }

    @Override
    public char charAt(int index) {
      return mName.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
      return mName.subSequence(start, end);
    }

    @Override
    public String toString() {
      return mName.toString();
    }
  }
}
