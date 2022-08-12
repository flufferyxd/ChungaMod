package magnileve.chungamod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.apache.logging.log4j.Logger;

/**
 * Contains support for interacting with a properties file unaffected by loaded configs and saved to the Chungamod directory.
 * Also contains support for adding and removing Runnables in the Chungamod shutdown hook.
 * @author Magnileve
 * @see Chung#SYSTEM
 */
public class ChungamodSystem {

private static final String INSTANCE_PROPERTIES_FILE = "instance.properties";

private final Logger log;
private final Properties properties;
private final Properties propertyDefaults;
private final Map<String, Collection<Consumer<String>>> propertyListeners;
private final Collection<Runnable> shutdownRunnables;

ChungamodSystem(Logger log) {
	this.log = log;
	propertyDefaults = new Properties();
	properties = new Properties(propertyDefaults);
	propertyListeners = new HashMap<>(1);
	shutdownRunnables = new ArrayList<>(0);
}

void init() {
	Path path = Paths.get(Chung.CHUNGAMOD_DIRECTORY, INSTANCE_PROPERTIES_FILE);
	try(Reader read = Files.newBufferedReader(path)) {
		properties.load(read);
	} catch(IOException e) {
		if(e instanceof NoSuchFileException) saveProperties();
		else log.error("Unable to load instance properties", e);
	}
	Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		synchronized(shutdownRunnables) {
			for(Runnable run:shutdownRunnables) run.run();
		}
	}, "Chungamod shutdown hook"));
}

private void saveProperties() {
	try(Writer write = Files.newBufferedWriter(Paths.get(Chung.CHUNGAMOD_DIRECTORY, INSTANCE_PROPERTIES_FILE))) {
		properties.store(write, null);
	} catch (IOException e) {
		log.error("Unable to save instance properties", e);
	}
}

/**
 * Set a property key/value pair to Chungamod properties.
 *
 * @param key the key to be placed into this property list
 * @param value the value corresponding to {@code key}
 */
public void setProperty(String key, String value) {
	if(value == null) {
		properties.remove(key);
		value = properties.getProperty(key);
	} else properties.setProperty(key, value);
	saveProperties();
	for(Consumer<String> listener:propertyListeners.get(key)) listener.accept(value);
}

/**
 * Searches for the property with the specified key in Chungamod properties.
 * If the key is not found in this property list, the default properties
 * are then checked. The method returns
 * {@code null} if the property is not found.
 *
 * @param   key the property key
 * @return  the value in Chungamod properties with the specified key value
 */
public String getProperty(String key) {
	return properties.getProperty(key);
}

/**
 * Searches for the property with the specified key in Chungamod properties.
 * If the key is not found in this property list, the default properties
 * are then checked. The method returns
 * {@code defaultValue} if the property is not found.
 *
 * @param   key the property key
 * @param   defaultValue a default value
 * @return  the value in Chungamod properties with the specified key value
 * @see #setPropertyDefault(String, String)
 */
public String getProperty(String key, String defaultValue) {
	return properties.getProperty(key, defaultValue);
}

/**
 * Adds a listener for a property.
 * @param key the property key
 * @param listener the listener for {@code key}
 */
public void addPropertyListener(String key, Consumer<String> listener) {
	propertyListeners.computeIfAbsent(key, k -> new ArrayList<>(1)).add(listener);
}

/**
 * Removes a listener for a property.
 * @param key the property key
 * @param listener the listener for {@code key}
 */
public void removePropertyListener(String key, Consumer<String> listener) {
	propertyListeners.get(key).remove(listener);
}

/**
 * Set a default key/value pair for a Chungamod property.
 *
 * @param key the key to be placed into the default property list
 * @param value the value corresponding to {@code key}
 */
public void setPropertyDefault(String key, String value) {
	propertyDefaults.setProperty(key, value);
}

/**
 * Set a default key/value pair for a Chungamod property,
 * then adds a listener for the property,
 * then notifies the listener for the current property value.
 * @param key the property key
 * @param defaultValue the default value of the property
 * @param listener the listener to be added and notified
 * @see #setPropertyDefault(String, String)
 * @see #addPropertyListener(String, Consumer)
 */
public void addProperty(String key, String defaultValue, Consumer<String> listener) {
	setPropertyDefault(key, defaultValue);
	addPropertyListener(key, listener);
	listener.accept(getProperty(key));
}

/**
 * Adds a {@link Runnable} to the Chungamod shutdown hook.
 * @param run the {@code Runnable} to be added
 * @see #removeShutdownHook(Runnable)
 */
public void addShutdownHook(Runnable run) {
	synchronized(shutdownRunnables) {
		shutdownRunnables.add(run);
	}
}

/**
 * Removes a {@link Runnable} from the Chungamod shutdown hook.
 * @param run the {@code Runnable} to be removed
 * @return {@code true} if a {@code Runnable} was removed as a result of this call
 * @see #addShutdownHook(Runnable)
 */
public boolean removeShutdownHook(Runnable run) {
	synchronized(shutdownRunnables) {
		return shutdownRunnables.remove(run);
	}
}

}
