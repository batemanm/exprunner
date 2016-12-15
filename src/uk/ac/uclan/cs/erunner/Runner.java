package uk.ac.uclan.cs.erunner;

import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.io.File;

import java.util.concurrent.ArrayBlockingQueue;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

import java.util.concurrent.*;
import java.util.Collections;

import javax.script.*;

public class Runner extends Thread {
private List<Parameters> parameters = new ArrayList<Parameters> ();
private ExecutorService es = null;
private String command = null;
private String logFileName = null;
private ObjectOutputStream oos = null;
private List<LogEvent> log = null;
private double completed = 0;
private double total = 0;
private int threads = 0;
private List<RunnerListener> listeners = new ArrayList<RunnerListener> ();
private ArrayBlockingQueue<Integer[]> pqueue = null;
private File tempFile = null;
private ObjectOutputStream paramsToTemp = null;
private List<Future> taskReferences = new ArrayList<Future> ();

public void createWebServer (int port) {
	WebServer ws = new WebServer (port);
	addListener (ws);
}

public void addnumbers (Integer[] numbers) {
	try {
		pqueue.put (numbers);
	} catch (Exception e) {
		e.printStackTrace ();
	}
}

public void addListener (RunnerListener listener) {
	listeners.add (listener);
}

public String getLogFileName () {
	return logFileName;
}

public void newParameters (String tag, String parameters) {
        String p[] = parameters.split (" ");
        addParameters (new Parameters (tag, p));
}


public void addParameters (Parameters o) {
	parameters.add (o);
}

public boolean isDone (String command) {
	for (LogEvent le: log) {
		if (command.equals (le.getCommand ())) {
			return true;
		}
	}
	return false;
}

public String getCommand () {
	return (command);
}

public synchronized void save () {
	try {
		ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (logFileName));
		oos.writeObject (log);
		oos.close ();
	} catch (Exception e) {
		e.printStackTrace ();
	}
}

public synchronized void logStarted (String command) {
	LogEvent l = new LogEvent (LogEvent.STARTED, command);	
	log.add (l);
	for (RunnerListener listener: listeners) {
		listener.logStarted (command);
	}
}

public synchronized void logFinished (String command, String stdOut, String stdErr) {
	LogEvent l = new LogEvent (LogEvent.FINISHED, command, stdOut, stdErr);	
	log.add (l);
	completed ++;
//	System.out.println ("logFinished Completed " + completed + " " + total);
	double percent = (completed / total) * 100;
	for (RunnerListener listener: listeners) {
		listener.logFinished (command, percent);
	}
}

public synchronized void logError (String command) {
	LogEvent l = new LogEvent (LogEvent.ERROR, command);	
	log.add (l);
	for (RunnerListener listener: listeners) {
		listener.logError (command);
	}
}

public void setLogFileName (String logFileName) {
	this.logFileName = logFileName;
	try {
		ObjectInputStream ois = new ObjectInputStream (new FileInputStream (logFileName));
		Object o = ois.readObject ();
		if (o != null) {
			log = Collections.synchronizedList ((List)o);
		} else {
			log = Collections.synchronizedList(new ArrayList<LogEvent>());
		}
		ois.close ();
	} catch (FileNotFoundException e) {
		log = Collections.synchronizedList(new ArrayList<LogEvent>());
	} catch (Exception e) { 
		e.printStackTrace ();
	}
}

public void setThreads (int threads) {
	this.threads = threads;
}

public void setCommand (String command) {
		this.command = command;
}

public void setParameters (Parameters... params) {
	for (Parameters p: params) {
		parameters.add (p);
	}
}

private void setup (String logFileName, int threads, String command, Parameters... params) {
	setLogFileName (logFileName);
	setThreads (threads);
	setCommand (command);
	setParameters (params);
}

public Runner () {}

public Runner (String logFileName, int threads, String command, Parameters... params) {
	setup (logFileName, threads, command, params);
}

public Runner (String logFileName, String command, Parameters... params) {
	setup (logFileName, Runtime.getRuntime ().availableProcessors (), command, params);
}

public void doIt () {
	total = 1;
	for (Parameters p: parameters) {
		total = total * p.size ();
	}

	if (logFileName == null) {
		try {
			File temp = File.createTempFile ("erun", ".log", new File ("."));
			String tempName = temp.getName ();
			temp.delete ();
			setLogFileName (tempName);
		} catch (Exception e) {}
	}

	if (threads == 0) {
		threads = Runtime.getRuntime ().availableProcessors ();
	}
	pqueue = new ArrayBlockingQueue<Integer[]> (threads * 2, true);

	es = Executors.newFixedThreadPool (threads);

	RunnerShutdown rs = new RunnerShutdown (this);
	Runtime.getRuntime ().addShutdownHook (rs);
	Timer timer = new Timer ();
	timer.schedule (new RunnerSaver (this), 10 * 1000);

	int count = 0;
	String script = "";

	for (int i = 0; i < parameters.size (); i ++) {
		script = script + "var o" + i + " = null;\n";
	}

	for (Parameters p: parameters) {
		script = script + "for (var i"+count + " = 0; i"+ count + 
		" < params.get (" + count +").size (); i"+count + "++) {\n";
		script = script + "o"+count + " = params.get ("+count+").get (i"+count+");\n";
		count ++;
	}

	script = script + "var p = java.lang.reflect.Array.newInstance (java.lang.Integer, " + parameters.size ()+ ");\n";

	for (int i = 0; i < count; i++) {
		script = script + "p[" + i + "] = i" + i + ";\n";		
	}
	script = script + "runner.addnumbers (p);\n";
	for (Parameters p: parameters) {
		script = script + "}\n";
	}


	new Thread (this).start ();

	try {
		ScriptEngineManager manager = new ScriptEngineManager ();
		ScriptEngine engine = manager.getEngineByName ("js");
		engine.put ("params", parameters);
		engine.put ("runner", this);
		engine.eval (script);
	} catch (Exception e) {
		e.printStackTrace ();
	}
}

public void run () {
	int i = 0;

	for (i = 0; i < total; i ++) {
		try {
			Integer[] ints = pqueue.take ();
			Future future = es.submit (new RunnerThread (this, ints, parameters));
			taskReferences.add (future);
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

	es.shutdown ();
	try {
		// wait for 100 years for the process to finish
		es.awaitTermination (100 * 365, TimeUnit.DAYS);
	} catch (Exception e) {
		e.printStackTrace ();
	}

	for (Future future: taskReferences) {
		if (future.isDone () == false) {
			System.out.println ("Task is reporting that it has not finished.");
		}
	}

	System.exit (0);
}

}
