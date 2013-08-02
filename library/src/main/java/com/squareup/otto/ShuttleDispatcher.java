package com.squareup.otto;

import android.os.Handler;
import android.os.Looper;

public abstract class ShuttleDispatcher {
  /** ONLY FOR USE IN TESTS.  SRSLY. */
  public static final ShuttleDispatcher TEST = new ShuttleDispatcher() {
    private final ThreadLocal<Object> threadTag = new ThreadLocal<Object>();
    private final Object creationTag;

    {
      Object tag = threadTag.get();
      if (tag == null) {
        tag = new Object();
        threadTag.set(tag);
      }
      creationTag = tag;
    }

    @Override void enforce() {
      if (threadTag.get() != creationTag) {
        throw new AssertionError(
            "Event bus accessed from illegal thread " + Thread.currentThread());
      }
    }

    @Override void dispatch(Runnable runnable) {
      runnable.run();
    }
  };
  private static ShuttleDispatcher main;

  private ShuttleDispatcher() {
  }

  public static ShuttleDispatcher main() {
    if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
      throw new IllegalStateException(
          "Event bus created from non-main thread " + Thread.currentThread());
    }
    if (main == null) {
      main = new ShuttleDispatcher() {
        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override public void enforce() {
          if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new AssertionError(
                "Event bus accessed from non-main thread " + Thread.currentThread());
          }
        }

        @Override public void dispatch(Runnable runnable) {
          handler.post(runnable);
        }
      };
    }
    return main;
  }

  abstract void enforce();

  abstract void dispatch(Runnable runnable);
}
