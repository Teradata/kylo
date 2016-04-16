package com.thinkbiganalytics.scheduler.support;

import com.thinkbiganalytics.scheduler.TriggerInfo;

import java.util.Collection;

/**
 * Created by sr186054 on 4/14/16.
 */
public class ScheduledJobState {


  public static boolean isRunning(Collection<TriggerInfo> triggerInfos) {
    boolean running = false;
    if (triggerInfos != null) {
      for (TriggerInfo triggerInfo : triggerInfos) {
        running = triggerInfo.getState().equals(TriggerInfo.TriggerState.BLOCKED) || triggerInfo.isSimpleTrigger();
        if (running) {
          break;
        }
      }
    }
    return running;
  }

  public static boolean isPaused(Collection<TriggerInfo> triggerInfos) {
    boolean paused = false;
    if (triggerInfos != null) {
      for (TriggerInfo triggerInfo : triggerInfos) {
        paused = triggerInfo.getState().equals(TriggerInfo.TriggerState.PAUSED);
        if (paused) {
          break;
        }
      }
    }
    return paused;
  }

  public static boolean isScheduled(Collection<TriggerInfo> triggerInfos) {
    boolean scheduled = false;
    if (triggerInfos != null) {
      for (TriggerInfo triggerInfo : triggerInfos) {
        if (triggerInfo.isScheduled()) {
          scheduled = true;
          break;
        }
      }
    }
    return scheduled;
  }
}
