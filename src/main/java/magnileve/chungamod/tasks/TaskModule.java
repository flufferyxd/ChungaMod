package magnileve.chungamod.tasks;

import magnileve.chungamod.modules.Module;

/**
 * A {@link Task} that can be registered as a {@link Module}.
 * @author Magnileve
 */
public interface TaskModule extends Module, Task {

@Override
public default void finish() {
	selfDisable();
}

@Override
public default void disable() {
	cancel();
}

@Override
public default void softDisable() {
	softCancel();
}

}