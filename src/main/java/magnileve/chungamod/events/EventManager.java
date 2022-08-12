package magnileve.chungamod.events;

import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.Logger;

import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.Util;

/**
 * The {@code EventManager} allows types to be registered as events and
 * listeners to be registered for posted events.
 * Event types and listeners do not have to inherit any types.
 * Listener methods are identified through annotations and parameter type.
 * @author Magnileve
 * @see OnEvent
 */
public class EventManager {

private final Logger log;
private final ArrayBuildList<Collection<Bucket<Method[], Collection<Object>>>> methods;
private final ArrayBuildList<Class<?>> eventTypes;

/**
 * Creates an {@code EventManager} with no registered events or listeners.
 * @param log trace logger for event posts and listener adding/removing
 */
public EventManager(Logger log) {
	this.log = log;
	methods = new ArrayBuildList<>(Collection.class, 1);
	eventTypes = new ArrayBuildList<>(new Class<?>[1]);
}

/**
 * Adds a listener for an event.
 * @param listener the listener
 * @param eventType the type of event to be listened for
 * @throws IllegalArgumentException if the listener does not listen for the event type, or if the event type is not registered
 */
public void addListener(Object listener, Class<?> eventType) {
	log.trace("Adding event listener {} for {}", listener, eventType);
	getCollection(listener, eventType).add(Objects.requireNonNull(listener));
}

/**
 * Removes a listener for an event.
 * @param listener the listener
 * @param eventType the type of event to no longer be listened for
 * @return {@code true} if the listener was found and removed
 * @throws IllegalArgumentException if the listener does not listen for the event type, or if the event type is not registered
 */
public boolean removeListener(Object listener, Class<?> eventType) {
	log.trace("Removing event listener {} for {}", listener, eventType);
	return getCollection(listener, eventType).remove(listener);
}

/**
 * Registers an event type.
 * @param <T> the event type
 * @param eventType the {@link Class} instance of this event type
 * @return an {@link EventPoster} for this event type
 * @throws IllegalStateException if this event type has already been registered
 */
public <T> EventPoster<T> registerEvent(Class<T> eventType) {
	if(eventTypes.contains(eventType)) throw new IllegalStateException(eventType + " is already registered");
	int index = eventTypes.size();
	eventTypes.add(eventType);
	methods.add(new ArrayBuildList<>(Bucket.class, 1));
	return event -> post(event, index);
}

/**
 * Sends an event to all of its event type's registered listeners.
 * @param event the event
 * @param eventIndex index of event
 * @see #registerEvent(Class)
 */
protected void post(Object event, int eventIndex) {
	log.trace(() -> log.getMessageFactory().newMessage("Posting event " + event + " as " + eventTypes.get(eventIndex)));
	Collection<Bucket<Method[], Collection<Object>>> allListeners = methods.get(eventIndex);
	for(Bucket<Method[], Collection<Object>> group:allListeners) {
		Method[] methods = group.getE1();
		try {
			for(Object obj:group.getE2().toArray()) for(int h = 0; h < methods.length; h++) methods[h].invoke(obj, event);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}

/**
 * Returns the index for an event type.
 * @param eventType the event type
 * @return the event index
 * @throws IllegalArgumentException if the event type is not registered
 */
protected int getIndex(Class<?> eventType) {
	int i = eventTypes.indexOf(eventType);
	if(i < 0) throw new IllegalArgumentException(eventType + " is not registered");
	return i;
}

@Override
public String toString() {
	return "Event manager for types " + eventTypes;
}

/**
 * Gets the collection of similar listeners for an event, or makes one if one does not exist.
 * @param listener a listener
 * @param eventType the event type that is listened for
 * @return a collection of listeners registered for {@code eventClass} that have the same capabilities to listen for any event
 * @throws IllegalArgumentException if the listener does not listen for the event type, or if the event type is not registered
 */
private Collection<Object> getCollection(Object listener, Class<?> eventType) {
	Collection<Bucket<Method[], Collection<Object>>> eventListeners = methods.get(getIndex(eventType));
	Method[] allListenerMethods = listener.getClass().getMethods();
	ArrayBuildList<Method> getMethods = new ArrayBuildList<>(new Method[allListenerMethods.length]);
	for(Method method:allListenerMethods) {
		OnEvent a = method.getAnnotation(OnEvent.class);
		if(a != null) {
			Class<?>[] acceptedEvents = a.value();
			if(acceptedEvents.length == 0) {
				if(method.getParameterCount() == 1) {
					if(method.getParameters()[0].getType().isAssignableFrom(eventType)) getMethods.add(method);
				} else getMethods.add(method); //will result in AnnotationFormatError
			} else if(Util.indexOf(acceptedEvents, eventType) >= 0) getMethods.add(method);
		}
	}
	Method[] listenerMethods = getMethods.toArray();
	if(listenerMethods.length == 0) throw new IllegalArgumentException(listener.getClass() + " does not listen for " + eventType);
	groups:
	for(Bucket<Method[], Collection<Object>> group:eventListeners) {
		Method[] methods = group.getE1();
		if(methods.length == listenerMethods.length) {
			for(int i = 0; i < methods.length; i++) if(!methods[i].equals(listenerMethods[i]) &&
					Util.indexOf(listenerMethods, methods[i]) < 0) continue groups;
			return group.getE2();
		}
	}
	for(Method method:listenerMethods) {
		if(Modifier.isStatic(method.getModifiers())) throw new AnnotationFormatError(method + " must not be static");
		if(method.getParameterCount() != 1 || !method.getParameters()[0].getType().isAssignableFrom(eventType))
			throw new AnnotationFormatError(method + " must take a single parameter acceptable of " + eventType);
	}
	Collection<Object> collection = new ArrayBuildList<>(new Object[1]);
	eventListeners.add(Bucket.of(listenerMethods, collection));
	return collection;
}

/**
 * Gets an array of event types that the given type is able to listen for.
 * @param listenerType the listener type
 * @return an array of event types that the given type can be added as a listener for
 */
public static Class<?>[] getEventTypes(Class<?> listenerType) {
	ArrayBuildList<Class<?>> classes = new ArrayBuildList<>(Util.CLASS_ARRAY_0);
	Set<String> classSet = new HashSet<>();
	for(Method method:listenerType.getMethods()) {
		OnEvent a = method.getAnnotation(OnEvent.class);
		if(a != null) {
			if(Modifier.isStatic(method.getModifiers())) throw new AnnotationFormatError(method + " must not be static");
			Parameter[] params = method.getParameters();
			Class<?>[] eventClasses = a.value();
			if(params.length == 1) {
				if(eventClasses.length == 0) {
					Class<?> eventClass = params[0].getType();
					if(classSet.add(eventClass.getName())) classes.add(eventClass);
				} else for(Class<?> eventClass:eventClasses) {
					if(params[0].getType().isAssignableFrom(eventClass)) {
						if(classSet.add(eventClass.getName())) classes.add(eventClass);
					} else throw new AnnotationFormatError(method + " must take a single parameter acceptable of " + eventClass);
				}
			} else throw new AnnotationFormatError(method + " must take a single parameter" +
					(eventClasses.length == 0 ? "" : " acceptable of the following: " + Arrays.toString(eventClasses)));
		}
	}
	return classes.trim().getArray();
}

}