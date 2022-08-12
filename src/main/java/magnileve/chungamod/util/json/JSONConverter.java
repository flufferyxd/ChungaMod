package magnileve.chungamod.util.json;

import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONWriter;

/**
 * Serializes and deserializes objects of a Java type to and from JSON.
 * @param <T> type of objects
 * @author Magnileve
 */
public interface JSONConverter<T> {

/**
 * Serializes an object to JSON.
 * @param w writes JSON
 * @param obj an object
 * @throws JSONException if an I/O error occurs
 */
public void serialize(JSONWriter w, T obj) throws JSONException;

/**
 * Deserializes an object from JSON.
 * @param p reads JSON
 * @return an object represented by the read JSON
 * @throws JSONException if a JSON deserializing error occurs, or if an I/O error occurs
 */
public T deserialize(JSONTokener p) throws JSONException;

}