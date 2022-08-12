package magnileve.chungamod.tasks;

/**
 * Runs when a task has been finished.
 * @author Magnileve
 */
@FunctionalInterface
public interface FinishTask {

/**
 * Runs when this task has been finished.
 */
public void finish();

}