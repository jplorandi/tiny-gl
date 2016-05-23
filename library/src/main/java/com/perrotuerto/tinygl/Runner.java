package com.perrotuerto.tinygl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jp.lorandi@cfyar.com Date: 10/3/15 Time: 6:55 PM
 */
public class Runner {
  private List<Callable> runOnGlThreadQueueA = new LinkedList<Callable>();
  private List<Callable> runOnGlThreadQueueB = new LinkedList<Callable>();
  private List<Callable> runOnGlThreadQueueActive = runOnGlThreadQueueA;


  public void runOnGlThread(Callable callable) {
    this.runOnGlThreadQueueActive.add(callable);
  }

  public synchronized void processQueue() {
    List<Callable> pending = runOnGlThreadQueueActive;
    if (pending == runOnGlThreadQueueA) {
      runOnGlThreadQueueActive = runOnGlThreadQueueB;
    } else {
      runOnGlThreadQueueActive = runOnGlThreadQueueA;
    }
    for (Callable callable : pending) {
      try {
        callable.call();
      } catch (Exception ignored) {
      }
    }

    pending.clear();
  }
}
