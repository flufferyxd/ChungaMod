package magnileve.chungamod.util.json;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * A {@link JSONWriter} that creates newlines and indentations.
 * @author Magnileve
 */
public class GoodLookingJSONWriter extends JSONWriter {

private final int indentSpace;

private int currentIndent;
private boolean comma;

/**
 * Make a fresh GoodLookingJSONWriter. It can be used to build one JSON text.
 * @param w an appendable object
 * @param indentSpace size of each indentation
 * @throws IllegalArgumentException if {@code indentSpace} is negative
 */
public GoodLookingJSONWriter(Appendable w, int indentSpace) {
	super(new Appendable() {
		@Override
		public Appendable append(CharSequence csq) throws IOException {
			w.append(csq);
			return this;
		}
		
		@Override
		public Appendable append(CharSequence csq, int start, int end) throws IOException {
			w.append(csq, start, end);
			return this;
		}
		
		@Override
		public Appendable append(char c) throws IOException {
			if(c != ',') w.append(c);
			return this;
		}
	});
	if(indentSpace < 0) throw new IllegalArgumentException("Indent space must not be negative");
	this.indentSpace = indentSpace;
}

/**
 * Creates a new line, writing the comma before the newline if necessary.
 */
private void newLine() {
	try {
		if(comma) {
			writer.append(",");
			comma = false;
		}
		writer.append('\n');
		for(int i = 0; i < currentIndent; i++) writer.append(' ');
	} catch (IOException e) {
		throw new JSONException(e);
	}
}

/**
 * Called before appending a value, writing a comma if needed.
 * @param setComma
 */
private void appendingValue(boolean setComma) {
	try {
		if(comma && mode == 'a') writer.append(", ");
		comma = setComma;
	} catch (IOException e) {
		throw new JSONException(e);
	}
}

@Override
public JSONWriter endObject() throws JSONException {
	currentIndent -= indentSpace;
	comma = false;
	newLine();
	comma = true;
    return super.endObject();
}

@Override
public JSONWriter endArray() throws JSONException {
	comma = true;
	return super.endArray();
}

@Override
public JSONWriter key(String string) throws JSONException {
	newLine();
	super.key(string);
	try {
		writer.append(' ');
	} catch (IOException e) {
		throw new JSONException(e);
	}
	return this;
}

@Override
public JSONWriter array() throws JSONException {
	appendingValue(false);
	return super.array();
}

@Override
public JSONWriter object() throws JSONException {
	appendingValue(false);
	currentIndent += indentSpace;
	return super.object();
}

@Override
public JSONWriter value(boolean b) throws JSONException {
	appendingValue(true);
    return super.value(b);
}

@Override
public JSONWriter value(long l) throws JSONException {
	appendingValue(true);
    return super.value(l);
}

@Override
public JSONWriter value(Object object) throws JSONException {
	appendingValue(true);
    return super.value(object);
}

}