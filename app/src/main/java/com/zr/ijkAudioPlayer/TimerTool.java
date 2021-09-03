package com.zr.ijkAudioPlayer;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTool {
    private Timer timer;
    private TimerTask timerTask;
    private Task task;

    public void startTaskWithDelayAndPeriod(long delay, long period) {
        timer = new Timer();
        timerTask = new TimerTask(){
            @Override
            public void run() {
                task.execute();
            }
        };

        if (period > 0) {
            timer.schedule(timerTask, delay, period);
        } else {
            timer.schedule(timerTask, delay);
        }
    }

    public void cancelTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public void setTask(Task task) {
        this.task = task;
    }
    public TimerTask getTask(){
        return timerTask;
    }
    public interface Task {
        void execute();
    }
}
