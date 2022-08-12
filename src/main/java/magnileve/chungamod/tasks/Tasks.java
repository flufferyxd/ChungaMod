package magnileve.chungamod.tasks;

import java.util.function.BooleanSupplier;

import magnileve.chungamod.Tick;
import magnileve.chungamod.TickListener;

/**
 * Contains static methods to create {@link Task} instances.
 * @author Magnileve
 */
public class Tasks {

/**
 * Creates a new {@link Task} with the given components.
 * @param core starts and cancels task
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish if not null, runs when the task finishes
 * @return a new {@code Task} backed by the given components
 */
public static Task newTask(CoreTask core, SoftCancelTask softCancel, FinishTask finish) {
	return new TaskHolder(core, finish, softCancel);
}

/**
 * Creates a new {@link Task} implementing {@link TickListener} that registers itself to {@link Tick#MAIN} with the given components.
 * This task finishes when {@link TickListener#onTick()} returns a negative number.
 * @param core starts and cancels task
 * @param tickListener internal tick listener
 * @param startDelay determines the amount of future ticks this task should initially be scheduled
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish if not null, runs when the task finishes
 * @return a new {@code Task} backed by the given components
 */
public static Task newTask(CoreTask core, TickListener tickListener, TickListener startDelay, SoftCancelTask softCancel, FinishTask finish) {
	return new TaskByTickHolder(tickListener, startDelay, core, finish, softCancel, Tick.MAIN);
}

/**
 * Creates a new {@link Task} implementing {@link TickListener} that registers itself to {@link Tick#MAIN} with the given components.
 * This task finishes when {@link TickListener#onTick()} returns a negative number.
 * @param core starts and cancels task
 * @param tickListener internal tick listener
 * @param startDelay determines the amount of future ticks this task should initially be scheduled
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish if not null, runs when the task finishes
 * @param completeCondition called every time this tick listener is notified, and if {@code true} is returned, this task finishes
 * @return a new {@code Task} backed by the given components
 */
public static Task newTask(CoreTask core, TickListener tickListener, TickListener startDelay, SoftCancelTask softCancel, FinishTask finish, BooleanSupplier completeCondition) {
	return new TaskByTickHolder(tickListener, startDelay, core, finish, softCancel, Tick.MAIN) {
		@Override
		public int onTick() {
			if(completeCondition.getAsBoolean()) {
				finish();
				return -1;
			}
			return super.onTick();
		}
	};
}

/**
 * Creates a {@link Task} that waits for an amount of client ticks, then finishes.
 * @param ticks amount of client ticks to wait
 * @param finish called when this task finishes
 * @return a new {@code Task}
 */
public static Task wait(int ticks, FinishTask finish) {
	class TickListenerTask implements Task, TickListener {
		@Override
		public int onTick() {
			finish();
			return -1;
		}
		
		@Override
		public void finish() {
			finish.finish();
		}
		
		@Override
		public void softCancel() {
			Tick.MAIN.remove(this);
		}
		
		@Override
		public void cancel() {
			Tick.MAIN.remove(this);
		}
		
		@Override
		public void run() {
			if(ticks < 0) finish();
			else Tick.MAIN.add(this, ticks);
		}
	}
	return new TickListenerTask();
}

/**
 * Adds a {@link Task} to a queue that waits for an amount of client ticks, then finishes.
 * @param ticks amount of client ticks to wait
 * @param queue the queue
 */
public static void queueWait(int ticks, TaskQueue queue) {
	queue.offer(wait(ticks, queue.getFinishTask()));
}

/**
 * Adds a {@link Task} to a queue that waits for an amount of client ticks, then finishes.
 * @param ticks amount of client ticks to wait
 * @param finish called when this task finishes
 * @param queue the queue
 */
public static void queueWait(int ticks, TaskQueue queue, FinishTask finish) {
	queue.offer(wait(ticks, queue.getFinishTask(finish)));
}

/**
 * Creates a {@code Task} that runs the given runnable when started.
 * @param run a runnable
 * @return a new {@code Task}
 */
public static Task ofRunnable(Runnable run) {
	return new TaskHolder(new CoreTask() {
		@Override
		public void run() {
			run.run();
		}
		
		@Override
		public void cancel() {}
	}, null, null);
}

/**
 * Adds a {@code Task} to a queue that runs the given runnable.
 * @param run a runnable
 * @param queue the queue
 */
public static void queueRunnable(Runnable run, TaskQueue queue) {
	queue.offer(ofRunnable(() -> {
		run.run();
		queue.getFinishTask().finish();
	}));
}

}