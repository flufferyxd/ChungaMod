package magnileve.chungamod.tasks;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.function.BooleanSupplier;

import magnileve.chungamod.TickListener;

/**
 * A {@link Task} backed by a queue of tasks.
 * This task runs by running each task in the queue, waiting until the head task finishes before running the next.
 * This task finishes when the last task in the queue is finished.
 * @author Magnileve
 */
public class TaskQueue implements Queue<Task>, Task, PauseTask {

private final Queue<Task> queue;
private final FinishTask finishTask = () -> nextTask();
private final FinishTask finish;

private Task current;
private boolean active = true;

/**
 * Creates a new {@code TaskQueue} that runs {@code finish} when finished.
 * @param finish if not null, runs when this task finishes.
 */
public TaskQueue(FinishTask finish) {
	queue = new ArrayDeque<Task>();
	this.finish = finish;
}

/**
 * Creates a new {@code TaskQueue}.
 */
public TaskQueue() {
	this(null);
}

/**
 * Creates a new {@code TaskQueue} backed by the given queue that runs {@code finish} when finished.
 * @param wrapQueue used as the internal queue of tasks
 * @param finish if not null, runs when this task finishes.
 */
public TaskQueue(Queue<Task> wrapQueue, FinishTask finish) {
	queue = wrapQueue;
	this.finish = finish;
}

@Override
public void finish() {
	if(finish != null) finish.finish();
}

@Override
public void softCancel() {
	if(current != null) current.softCancel();
}

@Override
public void cancel() {
	if(current != null) current.cancel();
}

@Override
public void run() {
	if(current != null) current.cancel();
	nextTask();
}

private void nextTask() {
	if(active) {
		current = poll();
		if(current == null) finish();
		else current.run();
	}
}

/**
 * Gets the {@link FinishTask} that all tasks added to this queue through {@link #offer(Task)} should call when finishing.
 * @return a {@link FinishTask} that starts the next task in this queue
 */
public FinishTask getFinishTask() {
	return finishTask;
}

/**
 * Modifies a task finisher to start the next task in this queue.
 * This method should be used while creating a task to be added through {@link #offer(Task)}.
 * @return a {@link FinishTask} that runs the given finisher and starts the next task in this queue
 */
public FinishTask getFinishTask(FinishTask taskFinish) {
	return () -> {
		taskFinish.finish();
		nextTask();
	};
}

@Override
public void pause() {
	if(!active) throw new IllegalStateException();
	active = false;
	if(current instanceof PauseTask) ((PauseTask) current).pause();
}

@Override
public void resume() {
	if(active) throw new IllegalStateException();
	active = true;
	if(current instanceof PauseTask) ((PauseTask) current).resume();
	else nextTask();
}

/**
 * Creates a task for this queue and offers it to this queue.
 * @param core starts and cancels task
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish runs when the task finishes
 * @return {@code true} if the task was added to this queue; {@code false} otherwise
 * @see Tasks#newTask(CoreTask, SoftCancelTask, FinishTask)
 */
public boolean offer(CoreTask core, SoftCancelTask softCancel, FinishTask finish) {
	return offer(Tasks.newTask(core, softCancel, () -> {
		finish.finish();
		nextTask();
	}));
}

/**
 * Creates a task for this queue and offers it to this queue.
 * @param core starts and cancels task
 * @param tickListener task's internal tick listener
 * @param startDelay determines the amount of future ticks the task should initially be scheduled
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish runs when the task finishes
 * @return {@code true} if the task was added to this queue; {@code false} otherwise
 * @see Tasks#newTask(CoreTask, TickListener, TickListener, SoftCancelTask, FinishTask)
 */
public boolean offer(CoreTask core, TickListener tickListener, TickListener startDelay, SoftCancelTask softCancel, FinishTask finish) {
	return offer(Tasks.newTask(core, tickListener, startDelay, softCancel, finish == null ? finishTask : () -> {
		finish.finish();
		nextTask();
	}));
}

/**
 * Creates a task for this queue and offers it to this queue.
 * @param core starts and cancels task
 * @param tickListener task's internal tick listener
 * @param startDelay determines the amount of future ticks the task should initially be scheduled
 * @param softCancel if not null, cancels task when client disconnects from server
 * @param finish runs when the task finishes
 * @param completeCondition called every time the tick listener is notified, and if {@code true} is returned, this the finishes
 * @return {@code true} if the task was added to this queue; {@code false} otherwise
 * @see Tasks#newTask(CoreTask, TickListener, TickListener, SoftCancelTask, FinishTask, BooleanSupplier)
 */
public boolean offer(CoreTask core, TickListener tickListener, TickListener startDelay, SoftCancelTask softCancel, FinishTask finish, BooleanSupplier completeCondition) {
	return offer(Tasks.newTask(core, tickListener, startDelay, softCancel, finish == null ? finishTask : () -> {
		finish.finish();
		nextTask();
	}, completeCondition));
}

@Override
public int size() {
	return queue.size();
}

@Override
public boolean isEmpty() {
	return queue.isEmpty();
}

@Override
public boolean contains(Object o) {
	return queue.contains(o);
}

@Override
public Iterator<Task> iterator() {
	return queue.iterator();
}

@Override
public Object[] toArray() {
	return queue.toArray();
}

@Override
public <T> T[] toArray(T[] a) {
	return queue.toArray(a);
}

@Override
public boolean remove(Object o) {
	return queue.remove(o);
}

@Override
public boolean containsAll(Collection<?> c) {
	return queue.containsAll(c);
}

@Override
public boolean addAll(Collection<? extends Task> c) {
	return queue.addAll(c);
}

@Override
public boolean removeAll(Collection<?> c) {
	return queue.removeAll(c);
}

@Override
public boolean retainAll(Collection<?> c) {
	return queue.retainAll(c);
}

@Override
public void clear() {
	queue.clear();
}

@Override
public boolean add(Task e) {
	return queue.add(e);
}

@Override
public boolean offer(Task e) {
	return queue.offer(e);
}

@Override
public Task remove() {
	return queue.remove();
}

@Override
public Task poll() {
	return queue.poll();
}

@Override
public Task element() {
	return queue.element();
}

@Override
public Task peek() {
	return queue.peek();
}

}