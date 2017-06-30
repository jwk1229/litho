/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.specmodels.processor;

import java.util.List;

import javax.annotation.processing.Messager;

public class MultiPrintableException extends PrintableException {
  private final List<PrintableException> exceptions;

  public MultiPrintableException(List<PrintableException> exceptions) {
    this.exceptions = exceptions;
  }

  public void print(Messager messager) {
    for (PrintableException e : exceptions) {
      e.print(messager);
    }
  }
}
