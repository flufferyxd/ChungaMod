package magnileve.chungamod.util;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A {@code Permit} can be acquired by one object at a time.
 * When the permit is acquired by an object, the object can be received by other callers.
 * @param <T> the type of controller of this permit
 * @author Magnileve
 */
public class Permit<T> {

private T controller;

/**
 * Attempts to acquire this permit.
 * @param controller the object to acquire this permit
 * @return {@code true} if the given object has acquired this permit; {@code false} otherwise
 */
public boolean acquire(T controller) {
	if(isAvailable()) {
		this.controller = Objects.requireNonNull(controller);
		return true;
	}
	return false;
}

/**
 * Releases this permit if the given object is in control of it.
 * @param controller the object in control of this permit
 * @return {@code true} if the given object was in control of this permit; {@code false} otherwise
 */
public boolean release(T controller) {
	if(!isAvailable() && this.controller == controller) {
		this.controller = null;
		return true;
	}
	return false;
}

/**
 * Indicates if this permit is available to be acquired.
 * @return {@code true} if this permit is available to be acquired; {@code false} otherwise
 */
public boolean isAvailable() {
	return controller == null;
}

/**
 * Returns the current controller of this permit.
 * @return the current controller of this permit
 */
public T getController() {
	return controller;
}

/**
 * If this permit is available, invokes the controller supplier and acquires the permit for the controller.
 * @param <T1> type of controller
 * @param getController supplies the controller if this permit is available
 * @return The new controller of this permit, or {@code null} if this permit was unable to be acquired
 */
public <T1 extends T> T1 getIfAcquired(Supplier<T1> getController) {
	if(isAvailable()) {
		T1 controller = Objects.requireNonNull(getController.get());
		this.controller = controller;
		return controller;
	}
	return null;
}

}