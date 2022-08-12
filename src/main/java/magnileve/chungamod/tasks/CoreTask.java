package magnileve.chungamod.tasks;

/**
 * A task that can be started and cancelled.
 * @author Magnileve
 */
public interface CoreTask extends Runnable {

/**
 * Does nothing when started or cancelled.
 */
public static final CoreTask EMPTY = new CoreTask() {
	@Override public void run() {}
	@Override public void cancel() {}
};

/**
 * Starts this task.
 */
@Override
public void run();

/**
 * Cancels this task.
 */
public void cancel();

}