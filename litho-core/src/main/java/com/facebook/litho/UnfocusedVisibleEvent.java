/**
 * Copyright (c) 2014-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import com.facebook.litho.annotations.Event;

/**
 * Event triggered when a Component exits the Focused Range. The Focused Range is defined as
 * at least half of the viewport or, if the Component is smaller than half of the viewport,
 * when the it is fully visible.
 */
@Event
public class UnfocusedVisibleEvent {
}
