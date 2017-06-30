/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.dataflow;

/**
 * Multi-input node for unit tests
 */
public class AdditionNode extends ValueNode {

  @Override
  protected float calculateValue(long frameTimeNanos) {
    return getInput("a").getValue() + getInput("b").getValue();
  }
}
