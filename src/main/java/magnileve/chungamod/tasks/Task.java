package magnileve.chungamod.tasks;

/**
 * A {@code Task} can be started, cancelled, and finished.
 * @author Magnileve
 */
public interface Task extends FinishTask, SoftCancelTask, CoreTask {}