/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.model;

public class SpecModelValidationError {
  public final Object element;
  public final String message;

  public SpecModelValidationError(Object element, String message) {
    this.element = element;
    this.message = message;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " { " + message + " }";
  }
}
