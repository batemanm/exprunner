package uk.ac.uclan.cs.erunner;

public interface RunnerListener {
public void logStarted (String command);
public void logFinished (String command, double percent);
public void logError (String command);
}
