package magnileve.chungamod.tasks;

import magnileve.chungamod.TickListener;
import magnileve.chungamod.Tick;

/**
 * A {@link Task} implementing {@link TickListener} build on functional interfaces.
 * This task finishes when {@link #onTick()} returns a negative number.
 * @author Magnileve
 */
public class TaskByTickHolder extends TaskHolder implements TickListener {

private final TickListener tickListener;
private final TickListener startDelay;
private final Tick tick;

TaskByTickHolder(TickListener tickListener, TickListener startDelay, CoreTask core, FinishTask finish, SoftCancelTask softCancel, Tick tick) {
	super(core, finish, softCancel);
	this.tickListener = tickListener;
	this.startDelay = startDelay;
	this.tick = tick;
}

@Override
public void run() {
	super.run();
	tick.add(this, startDelay.onTick());
}

@Override
public void cancel() {
	super.cancel();
	tick.remove(this);
}

@Override
public void softCancel() {
	super.softCancel();
	tick.remove(this);
}

@Override
public int onTick() {
	int i = tickListener.onTick();
	if(i < 0) finish();
	return i;
}

}