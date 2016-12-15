package uk.ac.uclan.cs.erunner;

import java.util.concurrent.Callable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;

public class RunnerThread implements Callable {
private List <Parameters> parameters;
private Runner runner = null;
private Integer[] options;
private Random random;

public RunnerThread (Runner runner, Integer[] options, List<Parameters> parameters) {
	this.runner = runner;
	this.parameters = parameters;
	this.options = options;
	random = new Random ();
}

public String readStream (InputStream stream) {
	StringBuffer buffer = new StringBuffer ();
	try {
		String line = null;
		BufferedReader br = new BufferedReader (new InputStreamReader (stream));
		while (br.ready ()) {
			line = br.readLine ();
			buffer.append (line);
			buffer.append ("\n");
		}
	} catch (Exception e) {
		e.printStackTrace ();
	}
	return buffer.toString ();
}

public String call () {
	String line = "";
	String command = runner.getCommand ();
	for (int i = 0; i < options.length; i ++) {
		try {
			String tag = parameters.get (i).getTag ();
			int index = options[i];
			Object value = parameters.get (i).get (index);

			command = command.replaceAll (tag, value.toString ());

		} catch (Exception e) {
			e.printStackTrace (); 
		}
	}
	command = command.replaceAll ("%RANDOM%", new Integer (random.nextInt()).toString ());

	if (!runner.isDone (command)) {
		runner.logStarted (command);
		try {
			Process process = Runtime.getRuntime ().exec (command);
			process.waitFor ();
			String stdOut = readStream (process.getInputStream ());
			String stdErr = readStream (process.getErrorStream ());
			process.getInputStream ().close ();
			process.getErrorStream ().close ();
			runner.logFinished (command, stdOut, stdErr);
		} catch (Exception e) {
			runner.logError (command);
			e.printStackTrace ();
		}
	}
	return null;
}
}
