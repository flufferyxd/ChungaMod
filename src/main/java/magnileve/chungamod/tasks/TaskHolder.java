package magnileve.chungamod.tasks;

/**
 * A {@link Task} built on functional interfaces.
 * @author Magnileve
 */
public class TaskHolder implements Task {

private final CoreTask core;
private final FinishTask finish;
private final SoftCancelTask softCancel;

TaskHolder(CoreTask core, FinishTask finish, SoftCancelTask softCancel) {
	this.core = core;
	this.finish = finish;
	this.softCancel = softCancel;
}

@Override
public void finish() {
	if(finish != null) finish.finish();
}

@Override
public void softCancel() {
	if(softCancel != null) softCancel.softCancel();
}

@Override
public void cancel() {
	core.cancel();
}

@Override
public void run() {
	core.run();
}

}