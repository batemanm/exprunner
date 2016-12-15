package uk.ac.uclan.cs.erunner;

import javax.script.*;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main implements RunnerListener {

public synchronized void logStarted (String command) {}
public synchronized void logFinished (String command, double percent) {
        int d = (int)(percent/10);
        System.out.print ("[");
        for (int i = 0; i<d; i ++) {
                System.out.print ("#");
        }
        for (int i = d; i < 10; i ++) {
                System.out.print (".");
        }
	long free = Runtime.getRuntime ().freeMemory ();
	free = free / 1024 / 1024;

	long max = Runtime.getRuntime ().maxMemory ();
	max = max / 1024 / 1024;

	double pfree = free/max * 100;

        System.out.print ("] " + ((int)percent) + "%\r");
        System.out.flush ();
        if (percent == 100) {
                System.out.println ("");
        }
}
public synchronized void logError (String command) {}


public String loadScript (String fileName) {
	StringBuffer sb = new StringBuffer ();

	try {
		BufferedReader br = new BufferedReader (new FileReader (fileName));
		while (br.ready ()){
			sb.append ((String)br.readLine ());
		}
	} catch (Exception e) {
		e.printStackTrace ();
	}

	return sb.toString ();
}

public Parameters newParameters () {
	return new Parameters ();
}

public Parameters newParameters (String tag, String parameters) {
	String p[] = parameters.split (" ");
	return new Parameters (tag, p);	
}

private Main (String scriptName) {
	try {
		Runner runner = new Runner ();
		String s = loadScript (scriptName);

		ScriptEngineManager manager = new ScriptEngineManager ();

		ScriptEngine engine = manager.getEngineByName ("js");
		engine.put ("runner", runner);
//		engine.put ("er", this);
		engine.eval (s);
		runner.addListener (this);
		runner.doIt ();
		System.out.println ("Log file name is " + runner.getLogFileName ());
//		Runtime.getRuntime ().exit (0);

        } catch (Exception e) {
                e.printStackTrace ();
        }

}

public static void main (String[] argv) {
	if (argv.length == 1) {
		new Main (argv[0]);
	} else {
		System.out.println ("You must supply a configuration script");
	}
}
}
