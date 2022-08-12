package magnileve.chungamod.gui;

/**
 * Displays messages that can be replaced or removed.
 * @author Magnileve
 */
public interface UpdatableDisplay {

/**
 * Displays a message at a given position.
 * @param message the message, {@code null} or empty to hide
 * @param x the x position
 * @param y the y position
 * @param changeNotifier if not null, runs when the message is replaced or removed
 */
public void display(String message, int x, int y, Runnable changeNotifier);

/**
 * Displays a message at a position based on a button.
 * @param message the message, {@code null} or empty to hide
 * @param button the button used to determine the position
 * @param changeNotifier if not null, runs when the message is replaced or removed
 */
public void display(String message, ClickGUIButton button, Runnable changeNotifier);

/**
 * Hides any displayed message.
 * @param changeNotifier if not null, runs when a message is displayed or this method is called again
 */
public void hide(Runnable changeNotifier);

/**
 * Hides any displayed message.
 * This method is equivalent to the following:
 * <blockquote>
 * {@code hide(null)}
 * </blockquote>
 * @see #hide(Runnable)
 */
public default void hide() {
	hide(null);
}

/**
 * Does not display messages, but appropriately runs {@code changeNotifier} Runnables.
 * @author Magnileve
 */
public class EmptyImpl implements UpdatableDisplay {
	private Runnable changeNotifier;
	
	/**
	 * Creates a new {@code EmptyImpl}.
	 * @param changeNotifier if not null, runs the next time {@code display} or {@code hide} is called.
	 */
	public EmptyImpl(Runnable changeNotifier) {this.changeNotifier = changeNotifier;}
	/**
	 * Creates a new {@code EmptyImpl} with no initial {@code changeNotifier}.
	 */
	public EmptyImpl() {}
	
	@Override public void display(String message, int x, int y, Runnable changeNotifier) {update(changeNotifier);}
	@Override public void display(String message, ClickGUIButton button, Runnable changeNotifier) {update(changeNotifier);}
	@Override public void hide(Runnable changeNotifier) {update(changeNotifier);}
	
	/**
	 * Runs the current {@code changeNotifier} if it is not null, and then replaces it with the given {@link Runnable}.
	 * @param changeNotifier the new {@code changeNotifier}, or {@code null} for no new {@code changeNotifier}
	 */
	protected void update(Runnable changeNotifier) {
		if(this.changeNotifier != null) this.changeNotifier.run();
		this.changeNotifier = changeNotifier;
	}
}

}