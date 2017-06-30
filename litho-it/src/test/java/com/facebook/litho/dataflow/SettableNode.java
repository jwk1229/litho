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
 * Test node that allows setting of its next value.
 */
public class SettableNode extends ValueNode implements NodeCanFinish {

  private float mValue;

  public void setValue(float value) {
    mValue = value;
  }

  @Override
  protected float calculateValue(long frameTimeNanos) {
    return mValue;
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public void onInputsFinished() {
  }
}
