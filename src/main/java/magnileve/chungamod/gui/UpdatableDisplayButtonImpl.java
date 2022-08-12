package magnileve.chungamod.gui;

/**
 * Basic implementation of {@link UpdatableDisplayButton} inheriting {@link ClickGUIButtonImpl}.
 * @author Magnileve
 */
public class UpdatableDisplayButtonImpl extends DisplayButton implements UpdatableDisplayButton {

private final int dividerSize;

private Runnable changeNotifier;

/**
 * Creates a new updatable display button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param displayString initial string to be displayed
 * @param rendererFactory factory to build renderer for this button
 * @param dividerSize standard space between buttons
 * @param startVisible if this button should initially display the given {@code displayString}
 * @param changeNotifier if not null, runs the next time {@code display} or {@code hide} is called.
 */
public UpdatableDisplayButtonImpl(int id, int x, int y, int widthIn, int heightIn,
		String displayString, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		int dividerSize, boolean startVisible, Runnable changeNotifier) {
	super(id, x, y, widthIn, heightIn, displayString, rendererFactory);
	this.dividerSize = dividerSize;
	this.changeNotifier = changeNotifier;
	visible = startVisible;
}

@Override
public UpdatableDisplayButtonImpl init() {
	super.init();
	return this;
}

@Override
public void display(String message, int x, int y, Runnable changeNotifier) {
	if(message == null || message.isEmpty()) hide(changeNotifier);
	else {
		setPos(x, y);
		visible = true;
		setDisplayString(message);
		update(changeNotifier);
	}
}

@Override
public void display(String message, ClickGUIButton button, Runnable changeNotifier) {
	display(message, button.getX() + button.getWidth() + dividerSize / 2, button.getY(), changeNotifier);
}

@Override
public void hide(Runnable changeNotifier) {
	visible = false;
	updateRenderer();
	update(changeNotifier);
}

/**
 * Runs the current {@code changeNotifier} if it is not null, and then replaces it with the given {@link Runnable}.
 * @param changeNotifier the new {@code changeNotifier}, or {@code null} for no new {@code changeNotifier}
 */
protected void update(Runnable changeNotifier) {
	if(this.changeNotifier != null) this.changeNotifier.run();
	this.changeNotifier = changeNotifier;
}

@Override
public boolean isVisible() {
	return visible;
}

}