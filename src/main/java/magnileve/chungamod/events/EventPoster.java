package magnileve.chungamod.events;

/**
 * Posts events of a certain type to an {@link EventManager}.
 * @param <T> the type of event
 * @author Magnileve
 */
@FunctionalInterface
public interface EventPoster<T> {

/**
 * Sends an event to all of its event type's registered listeners.
 * @param event the event
 */
public void post(T event);

}