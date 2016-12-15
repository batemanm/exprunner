package uk.ac.uclan.cs.erunner;

import java.util.List;
import java.util.ArrayList;

public class Parameters {
int current = 0;
private String tag = null;
private List parameters = new ArrayList ();

public void set (Object... params){
	for (Object o: params) {
		parameters.add (o);
	}
}

public void setParameters (Object... params) {
	set (params);
}

public void addParameter (Object obj) {
	parameters.add (obj);
}

public void setTag (String tag) {
	this.tag = tag;
}

public Parameters (Object... parameters) {
	set (parameters);
}

public Parameters (String tag, String[] parameters) {
	setTag (tag);
	for (int i = 0; i < parameters.length; i ++) {
		this.parameters.add (parameters[i]);
	}
}

public Parameters (String tag, Object... parameters) {
	setTag (tag);
	set (parameters);
}

public String getTag () {
	return (tag);
}

public Object next () {
	Object value = parameters.get (current);
	current ++;
	return (value);
}

public int size () {
	return parameters.size ();
}

public void reset () {
	current = 0;
}

public void increment () {
	current ++;
}

public int getCurrent () {
	return current;
}

public Object get (int i) {
	return parameters.get (i);
}

public boolean hasNext () {
	return (current == (parameters.size ()));
}
}
