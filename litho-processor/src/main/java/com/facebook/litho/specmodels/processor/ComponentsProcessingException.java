/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public class ComponentsProcessingException extends PrintableException {
  private final Element element;

  public ComponentsProcessingException(String message) {
    this(null, message);
  }

  public ComponentsProcessingException(Element element, String message) {
    super(message);
    this.element = element;
  }

  public void print(Messager messager) {
    messager.printMessage(Diagnostic.Kind.ERROR, getMessage(), element);
  }
}
