/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.model;

import com.facebook.litho.annotations.FromPrepare;
import com.facebook.litho.annotations.OnCreateTransition;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.annotations.ShouldUpdate;
import com.facebook.litho.annotations.State;
import com.facebook.litho.specmodels.internal.ImmutableList;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link MethodParamModelFactory}
 */
public class MethodParamModelFactoryTest {

  @Before
  public void ListUp() {
  }

  @Test
  public void testCreateSimpleMethodParamModel() {
    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        mock(ExecutableElement.class),
        TypeName.BOOLEAN,
        "testParam",
        new ArrayList<Annotation>(),
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(),
        null);

    assertThat(methodParamModel).isInstanceOf(SimpleMethodParamModel.class);
  }

  @Test
  public void testCreatePropModel() {
    final List<Annotation> annotations = new ArrayList<>();
    annotations.add(mock(Prop.class));
    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        mock(ExecutableElement.class),
        TypeName.BOOLEAN,
        "testParam",
        annotations,
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(),
        null);

    assertThat(methodParamModel).isInstanceOf(PropModel.class);
  }

  @Test
  public void testCreateStateModel() {
    final List<Annotation> annotations = new ArrayList<>();
    annotations.add(mock(State.class));
    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        mock(ExecutableElement.class),
        TypeName.BOOLEAN,
        "testParam",
        annotations,
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(),
        null);

    assertThat(methodParamModel).isInstanceOf(StateParamModel.class);
  }

  @Test
  public void testCreateInterStageInputModel() {
    final List<Annotation> annotations = new ArrayList<>();
    Annotation fromPrepare= new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return FromPrepare.class;
      }
    };
    annotations.add(fromPrepare);
    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        mock(ExecutableElement.class),
        TypeName.BOOLEAN,
        "testParam",
        annotations,
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(FromPrepare.class),
        null);

    assertThat(methodParamModel).isInstanceOf(InterStageInputParamModel.class);
  }

  @Test
  public void testCreateDiffModel() {
    final List<Annotation> annotations = new ArrayList<>();
    Annotation annotation = new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return OnCreateTransition.class;
      }
    };
    annotations.add(annotation);

    ExecutableElement method = mock(ExecutableElement.class);
    when(method.getAnnotation(ShouldUpdate.class)).thenReturn(null);

    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        method,
        ParameterizedTypeName.get(ClassNames.DIFF, TypeName.INT.box()),
        "testParam",
        annotations,
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(OnCreateTransition.class),
        null);

    assertThat(methodParamModel).isInstanceOf(DiffModel.class);
    assertThat(((DiffModel) methodParamModel).needsRenderInfoInfra()).isTrue();
  }

  @Test
  public void testDontCreateDiffForShouldUpdate() {
    final List<Annotation> annotations = new ArrayList<>();
    Annotation annotation = new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return ShouldUpdate.class;
      }
    };
    annotations.add(annotation);

    ExecutableElement method = mock(ExecutableElement.class);
    when(method.getAnnotation(ShouldUpdate.class)).thenReturn(new ShouldUpdate() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return ShouldUpdate.class;
      }

      @Override
      public boolean onMount() {
        return false;
      }
    });

    MethodParamModel methodParamModel = MethodParamModelFactory.create(
        method,
        ParameterizedTypeName.get(ClassNames.DIFF, TypeName.INT.box()),
        "testParam",
        annotations,
        new ArrayList<AnnotationSpec>(),
        ImmutableList.<Class<? extends Annotation>>of(ShouldUpdate.class),
        null);

    assertThat(methodParamModel).isNotInstanceOf(DiffModel.class);
  }
}
