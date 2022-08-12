package magnileve.chungamod.util.json;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.json.JSONException;
import org.json.JSONTokener;

import magnileve.chungamod.util.Util;

/**
 * Contains static utility methods for reading JSON.
 * @author Magnileve
 */
public class JSONUtil {

private JSONUtil() {}

/**
 * Parses a value limiter string into a limiter identifier and limits.
 * A value limiter string is of the following format:
 * <blockquote>
 * {@code identifier:limits}
 * </blockquote>
 * @param limiterString a value limiter string
 * @return an array containing a limiter identifier at index {@code 0} and limits at index {@code 1}
 */
public static String[] parseLimits(String limiterString) {
	String[] array = limiterString.split(":", 2);
	return array.length == 2 ? array : new String[] {array[0], ""};
}

/**
 * Gets the next character in the string, skipping whitespace,
 * and checks that it matches a specified character.
 * @param p reads JSON
 * @param c the character to match
 * @return the character
 * @throws JSONException if the character does not match, or if there is an error reading the source string
 */
public static char next(JSONTokener p, char c) throws JSONException {
    char n = p.nextClean();
    if (n != c) {
        if(n > 0) {
            throw p.syntaxError("Expected '" + c + "' and instead saw '" +
            		n + "'");
        }
        throw p.syntaxError("Expected '" + c + "' and instead saw ''");
    }
    return n;
}

/**
 * Get the next value, and ensures that it is of a specified type. The value can be a Boolean, Double, Integer,
 * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
 * @param <T> type of next value
 * @param p reads JSON
 * @param asClass type of next value
 * @return an object of the given type
 * @throws JSONException if the next value is not of the given type, or if there is a syntax error
 */
@SuppressWarnings("unchecked")
public static <T> T nextValue(JSONTokener p, Class<T> asClass) throws JSONException {
	Object value = p.nextValue();
	if(asClass.isInstance(value)) return (T) value;
	throw p.syntaxError("Expected type " + asClass.getName() + " and instead saw type: " + value.getClass().getName());
}

/**
 * Determines if there is another value in a JSON object or JSON array,
 * and positions the {@code JSONTokener} after the comma or {@code endChar}.
 * @param p reads JSON
 * @param endChar <code>'}'</code> for JSON object; <code>']'</code> for JSON array
 * @return {@code true} if there is another value in this object/array; {@code false} if this object/array has ended
 * @throws JSONException if neither a comma nor {@code endChar} was read
 */
public static boolean hasNext(JSONTokener p, char endChar) throws JSONException {
	char c = p.nextClean();
	if(c == ',') return true;
	if(c == endChar) return false;
	else throw p.syntaxError("Expected ',' or " + endChar + " and instead saw '" +
			c + "'");
}

/**
 * Verifies that two characters match.
 * @param p used to create an exception if the characters do not match
 * @param given a read character
 * @param expected an expected character
 * @throws JSONException if {@code give} and {@code expected} are not the same character
 */
public static void verify(JSONTokener p, char given, char expected) throws JSONException {
	if(expected != given) throw p.syntaxError("Expected '" + expected + "' and instead saw '" +
            given + "'");
}

/**
 * Attempts to read the value {@code "null"}.  If {@code "null"} is not read, the {@code JSONTokener} is positioned before the value.
 * @param p reads JSON
 * @return {@code true} if {@code "null"} was read; {@code false} if there is a different value to be read
 * @throws JSONException if a syntax error occurs
 */
public static boolean checkNull(JSONTokener p) throws JSONException {
	if(p.nextClean() == 'n') {
		if(p.next() == 'u' && p.next() == 'l' && p.next() == 'l') return true;
		throw p.syntaxError("Invalid value format");
	}
	p.back();
	return false;
}

/**
 * Attempts to finish reading the value {@code "null"} with a {@code JSONTokener} positioned after the first character in the value.
 * If {@code "null"} is not read, the {@code JSONTokener} does not change position.
 * @param p reads JSON
 * @param c the first character in the value
 * @return {@code true} if {@code "null"} was read; {@code false} if there is a different value to be read
 * @throws JSONException if a syntax error occurs
 */
public static boolean checkNull(JSONTokener p, char c) throws JSONException {
	if(c == 'n') {
		if(p.next() == 'u' && p.next() == 'l' && p.next() == 'l') return true;
		throw p.syntaxError("Invalid value format");
	}
	return false;
}

/**
 * Attempts to read the value {@code "null"} or an expected character.  If {@code "null"} is not read,
 * the {@code JSONTokener} is positioned after the first character in the value.
 * @param p reads JSON
 * @param c an expected character
 * @return {@code true} if {@code "null"} was read; {@code false} if there is a different value to be read starting with the expected character
 * @throws JSONException if the next value neither is null nor starts with the expected character, or if a syntax error occurs
 */
public static boolean nullOrChar(JSONTokener p, char c) throws JSONException {
	char c1 = p.nextClean();
	if(checkNull(p, c1)) return true;
	verify(p, c1, c);
	return false;
}

/**
 * Returns an {@link Iterable} that when called, creates an {@code Iterator} that reads a JSON Object, returning each key in the object.
 * After every call to {@code next()}, the {@code JSONTokener} will be positioned before the value of the returned key.
 * The value must then be read so that the {@code JSONTokener} is positioned after it
 * before the next call to {@code hasNext()} or {@code next()}
 * @param p reads JSON
 * @return an {@code Iterator} that reads a JSON Object, returning each key in the object and
 * leaving the caller to read values after each returned key
 */
public static Iterable<String> iterateJSONObject(JSONTokener p) {
	return new Iterable<String>() {
		@Override
		public Iterator<String> iterator() {
			next(p, '{');
			if(p.nextClean() == '}') return Util.<String>emptyIterable().iterator();
			p.back();
			Iterator<String> iter = new Iterator<String>() {
				boolean hasNext = true;
				String nextKey = nextValue(p, String.class);
				
				@Override
				public boolean hasNext() {
					if(nextKey == null && hasNext) getNext();
					return hasNext;
				}
				
				@Override
				public String next() {
					if(hasNext) {
						if(nextKey == null) {
							getNext();
							return next();
						}
						String key = nextKey;
						nextKey = null;
						return key;
					}
					throw new NoSuchElementException();
				}
				
				private void getNext() {
					hasNext = JSONUtil.hasNext(p, '}');
					if(hasNext) {
						nextKey = nextValue(p, String.class);
						JSONUtil.next(p, ':');
					}
				}
			};
			next(p, ':');
			return iter;
		}
	};
}

}