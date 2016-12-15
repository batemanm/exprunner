package uk.ac.uclan.cs.erunner;

import java.util.TimerTask;

public class RunnerSaver extends TimerTask {
private Runner runner = null;
private String fileName = null;

public RunnerSaver (Runner runner) {
        this.runner = runner;
}

public synchronized void run () {
        runner.save ();
}
}

