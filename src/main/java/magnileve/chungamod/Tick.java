package magnileve.chungamod;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.Logger;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Allows simple scheduling of listeners for a tick.  A tick refers to one loop in a cycle.
 * @author Magnileve
 * @see #MAIN
 * @see TickListener
 */
@Mod.EventBusSubscriber(modid=Chung.MODID)
public class Tick {

/**
 * The main tick is the beginning of the Mincerfat client tick.
 * @see #CLIENT_POST
 * @see #RENDER_POST
 */
public static final Tick MAIN = new Tick();
/**
 * The end of the Mincerfat client tick.
 * @see #MAIN
 */
public static final Tick CLIENT_POST = new Tick();
/**
 * The beginning of the Mincerfat render tick.
 * @see #RENDER_POST
 */
public static final Tick RENDER_PRE = new Tick();
/**
 * The end of the Mincerfat render tick.
 * @see #RENDER_PRE
 */
public static final Tick RENDER_POST = new Tick();

private final Queue<ListenerContainer> tickMap;
private final Queue<Runnable> incomingRunnables;

private int tick;
private static Logger log;

static void init(Logger logIn) {
	log = logIn;
}

private Tick() {
	tickMap = new PriorityQueue<>();
	incomingRunnables = new ConcurrentLinkedQueue<>();
}

@SubscribeEvent
@SideOnly(value = Side.CLIENT)
public static void onTick(ClientTickEvent event) {
	if(event.phase == Phase.START) MAIN.onTick();
	else CLIENT_POST.onTick();
}

@SubscribeEvent
@SideOnly(value = Side.CLIENT)
public static void onTick(RenderTickEvent event) {
	if(event.phase == Phase.START) RENDER_PRE.onTick();
	else RENDER_POST.onTick();
}

/**
 * Runs incoming Runnables, then notifies the tick listeners.
 * If any listeners are scheduled for the current tick during iteration, they will be notified until none are.
 * @see #add(TickListener, int)
 * @see #run(Runnable)
 */
private void onTick() {
	tick++;
	for(Runnable run = incomingRunnables.poll(); run != null; run = incomingRunnables.poll()) run.run();
	ListenerContainer listener;
	while((listener = tickMap.peek()) != null && listener.tick <= tick) {
		tickMap.poll();
		nextTick(listener.listener, listener.listener.onTick());
	}
}

/**
 * Schedules a listener for a future tick.
 * @param listener the listener
 * @param tickIn the number of ticks in the future to call the listener
 */
private void nextTick(TickListener listener, int tickIn) {
	if(tickIn >= 0) tickMap.add(new ListenerContainer(listener, tick + tickIn));
	else log.trace("Removing tick listener " + listener);
}

/**
 * Schedules a listener for a future tick.
 * @param listener the listener
 * @param futureTicks the number of ticks in the future to call the listener
 */
public void add(TickListener listener, int tick) {
	log.trace("Adding tick listener " + listener);
	nextTick(listener, tick);
}

/**
 * Schedules a listener to be called during the next tick, or if listeners are currently being called, during this tick.
 * @param listener the listener
 */
public void add(TickListener listener) {
	add(listener, 0);
}

/**
 * Removes a listener if it is scheduled for a future tick.
 * @param listener listener to be removed
 */
public void remove(TickListener listener) {
	log.trace("Removing tick listener {}", listener);
	Iterator<ListenerContainer> iter = tickMap.iterator();
	while(iter.hasNext()) if(iter.next().listener.equals(listener)) {
		iter.remove();
		return;
	}
	log.trace("Listener not found");
}

/**
 * Schedules a {@link Runnable} to be called on the next tick.  This method is thread-safe.
 * @param run
 */
public void run(Runnable run) {
	incomingRunnables.offer(run);
}

/**
 * Gets the number of ticks since initialization.
 * @return the number of ticks since initialization
 */
public int current() {
	return tick;
}

/**
 * Contains a listener and the tick it is scheduled for.
 * @author Magnileve
 */
protected static class ListenerContainer implements Comparable<ListenerContainer> {

private final TickListener listener;
private final int tick;

public ListenerContainer(TickListener listener, int tick) {
	this.listener = listener;
	this.tick = tick;
}

/**
 * @return positive if this listener is scheduled for a later tick than {@code o},
 * zero if the listeners are scheduled for the same tick,
 * or negative if this listener is scheduled for a sooner tick than {@code o}
 */
@Override
public int compareTo(ListenerContainer o) {
	return tick - o.tick;
}

}

}