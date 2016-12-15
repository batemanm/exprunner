package uk.ac.uclan.cs.erunner;

import java.io.Serializable;

public class LogEvent implements Serializable {
private String command = null;
private int type = 0;
private long time;
private int exitStatus = 0;
private String stdOut = null;
private String stdErr = null;

public static final int STARTED = 1;
public static final int FINISHED = 2;
public static final int ERROR = 3;

public LogEvent (int type, String command, String stdOut, String stdErr) {
	this.type = type;
	this.command = command;
	this.time = System.currentTimeMillis ();
	this.stdOut = stdOut;
	this.stdErr = stdErr;
}

public LogEvent (int type, String command) {
	this (type, command, (String)null, (String)null);
}

public String getCommand () {
	return this.command;
}

public int getType () {
	return this.type;
}

public long getTimestamp () {
	return this.time;
}

public int getExitStatus () {
	return exitStatus;
}

public String getStdOut () {
	return stdOut;
}

public String getStdErr () {
	return stdErr;
}
}
