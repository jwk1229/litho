/**
 * Copyright (c) 2017-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.litho;

import android.view.View;

import static com.facebook.litho.EventDispatcherUtils.dispatchOnClick;

/**
 * Click listener that triggers its underlying event handler.
 */
class ComponentClickListener implements View.OnClickListener {

  private EventHandler<ClickEvent> mEventHandler;

  @Override
  public void onClick(View view) {
    if (mEventHandler != null) {
      dispatchOnClick(mEventHandler, view);
    }
  }

  EventHandler<ClickEvent> getEventHandler() {
    return mEventHandler;
  }

  void setEventHandler(EventHandler<ClickEvent> eventHandler) {
    mEventHandler = eventHandler;
  }
}
