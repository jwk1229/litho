/*
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho.dataflow;

/**
 * A ValueNode that will linearly update its value from its "initial" input to its "end" input
 * over the course of the given duration.
 *
 * This node supports the "end" input changing: it will animate to that new end value over the
 * remaining duration meaning that velocity of the value may change.
 *
 * NB: If the end input changes after the end of the duration, this node will just pass through that
 * new value.
 */
public class TimingNode extends ValueNode implements NodeCanFinish {

  public static final String INITIAL_INPUT = "initial";
  public static final String END_INPUT = "end";

  private static final int MS_IN_NANOS = 1000000;

  private final int mDurationMs;
  private long mStartTimeNs = Long.MIN_VALUE;
  private long mExpectedEndTimeNs = Long.MIN_VALUE;
  private long mLastValueTimeNs = Long.MIN_VALUE;
  private float mInitialValue;
  private boolean mAreParentsFinished = false;
  private boolean mIsFinished = false;

  public TimingNode(int durationMs) {
    mDurationMs = durationMs;
  }

  @Override
  public float calculateValue(long frameTimeNanos) {
    if (mLastValueTimeNs == Long.MIN_VALUE) {
      mInitialValue = getInput(INITIAL_INPUT).getValue();
      mStartTimeNs = frameTimeNanos;
      mLastValueTimeNs = frameTimeNanos;
      mExpectedEndTimeNs = mStartTimeNs + (mDurationMs * MS_IN_NANOS);
      return mInitialValue;
    }

    float endValue = getInput(END_INPUT).getValue();
    if (frameTimeNanos >= mExpectedEndTimeNs) {
      mIsFinished = true;
      return endValue;
    }

    float lastValue = getValue();
    float desiredVelocity = (endValue - lastValue) / (mExpectedEndTimeNs - mLastValueTimeNs);
    float increment = desiredVelocity * (frameTimeNanos - mLastValueTimeNs);

    mLastValueTimeNs = frameTimeNanos;

    return lastValue + increment;
  }

  @Override
  public boolean isFinished() {
    return mIsFinished && mAreParentsFinished;
  }

  @Override
  public void onInputsFinished() {
    mAreParentsFinished = true;
    mIsFinished = mLastValueTimeNs >= mExpectedEndTimeNs;
  }
}
