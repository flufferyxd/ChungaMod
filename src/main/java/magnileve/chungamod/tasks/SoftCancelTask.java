package magnileve.chungamod.tasks;

/**
 * Cancels a task when the client has left the server.
 * @author Magnileve
 */
@FunctionalInterface
public interface SoftCancelTask {

/**
 * Cancels this task when the client has left the server.
 */
public void softCancel();

}