package uk.ac.uclan.cs.erunner;

import java.util.TimerTask;

public class RunnerShutdown extends Thread {
private Runner runner = null;
private String fileName = null;

public RunnerShutdown (Runner runner) {
	this.runner = runner;
}

public synchronized void run () {
	runner.save ();
}
}
