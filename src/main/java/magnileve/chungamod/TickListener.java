package magnileve.chungamod;

/**
 * Listener to be called on a scheduled tick.
 * @author Magnileve
 * @see Tick
 */
@FunctionalInterface
public interface TickListener {

/**
 * Called on a scheduled tick.
 * @return the next tick for the listener to be called on, or a negative number for the listener not to be called again.
 * Returning zero will result in the listener being called again during the same iteration.
 */
public int onTick();

/**
 * Returns zero.<br>
 * <strong>WARNING:</strong> Do NOT pass this listener into an instance of {@link Tick}, or an infinite loop will happen.
 * This value is intended to be used to supply the tick another listener should be scheduled for.
 */
public static TickListener CURRENT = () -> 0;

}