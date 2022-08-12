package magnileve.chungamod.gui.values;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.function.Function;
import java.util.function.IntSupplier;

import org.lwjgl.input.Keyboard;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.PotentialInfoButton;
import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.ClickGUIButtonImpl;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Permit;

/**
 * A button with a value represented by a {@code String}.
 * @param <T> value type
 * @author Magnileve
 */
public abstract class StringValueButton<T> extends ValueButtonImpl<T> {

/**
 * Permit for handling keyboard activity.
 */
protected final Permit<Runnable> keyboardPermit;

private Runnable keyboardHandler;

/**
 * Creates a new value button.
 * @param id button ID
 * @param x x position
 * @param y y position
 * @param widthIn width
 * @param heightIn height
 * @param name key for this button's value
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param value initial value
 * @param valueProcessor processes input values
 * @param menuButtonBuilder if not null, is applied to this button and adds buttons for a menu being built
 * @param menuChain menu chain link of this button's menu
 * @param buttonIDSupplier generates button IDs
 * @param description if not null, is displayed when this button is hovered over
 * @param keyboardPermit permit for handling keyboard activity
 */
public StringValueButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, T value, ValueProcessor<T> valueProcessor,
		Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier, String description,
		Permit<Runnable> keyboardPermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description);
	this.keyboardPermit = keyboardPermit;
}

/**
 * Parses a value from a {@code String}.
 * @param valueString a {@code String} representation of a value
 * @return a value represented by the given {@code String}
 * @throws IllegalArgumentException if the given {@code String} is not able to be parsed
 */
public abstract T stringToValue(String valueString) throws IllegalArgumentException;

/**
 * Indicates if this button is currently handling keyboard input.
 * @return {@code true} if this button is currently handling keyboard input
 */
protected boolean isHandlingKeyboard() {
	return keyboardHandler != null;
}

@Override
public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, boolean alreadyProcessed) {
	boolean hovered = super.mouseClicked(mouseX, mouseY, mouseButton, alreadyProcessed);
	if(!hovered) onOutsideClick(mouseButton);
	return hovered;
}

/**
 * Called when a click has happened, but this button has not been clicked.
 * @param mouseButton the mouse button clicked
 */
protected void onOutsideClick(int mouseButton) {
	if(isHandlingKeyboard() && (mouseButton == 0 || mouseButton == 1)) {
		keyboardPermit.release(keyboardHandler);
		keyboardHandler = null;
		setDisplayString(valueToString());
	}
}

@Override
protected void onClick(int mouseButton) {
	if(mouseButton == 0 && (keyboardHandler = keyboardPermit.getIfAcquired(() -> new Runnable() {
		private StringBuilder inputBuilder = new StringBuilder(8).append(" |");
		private int inputLength = 0;
		
		@Override
		public void run() {
			if(Keyboard.getEventKeyState()) {
				int keyCode = Keyboard.getEventKey();
				switch(keyCode) {
				case Keyboard.KEY_ESCAPE:
				case Keyboard.KEY_RETURN:
					keyboardPermit.release(keyboardHandler);
					keyboardHandler = null;
					hideDisplayedMessage();
					if(keyCode == Keyboard.KEY_RETURN) {
						T newValue;
						try {
							newValue = processNewValue(stringToValue(inputBuilder.substring(0, inputLength)));
						} catch(IllegalArgumentException e) {
							displayMessage(e.getMessage());
							setDisplayString(valueToString());
							return;
						}
						if(!displayIfChanged(newValue)) setDisplayString(valueToString());
					} else setDisplayString(valueToString());
					return;
				case Keyboard.KEY_BACK:
					if(inputLength != 0) {
						inputBuilder.setCharAt(inputLength, '|');
						inputBuilder.setCharAt(inputLength - 1, ' ');
						inputBuilder.setLength(inputLength + 1);
						inputLength--;
					}
					break;
				default:
					char c = Keyboard.getEventCharacter();
					if(c >= ' ') {
						inputBuilder.setCharAt(inputLength++, c);
						inputBuilder.setCharAt(inputLength, ' ');
						inputBuilder.append('|');
					}
				}
				String display = inputBuilder.toString();
				setDisplayString(inputBuilder.toString());
				displayMessage(display);
			}
		}
	})) != null) {
		setDisplayString(" |");
		displayMessage(" |");
	}
}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	builder.add(new ClickGUIButtonImpl(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Copy", rendererFactory) {
		@Override
		protected void onClick(int mouseButton) {
			try {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(valueToString()), null);
			} catch(IllegalStateException e) {}
		}
	});
	builder.add(new PotentialInfoButton(buttonIDSupplier.getAsInt(), 0, 0, getWidth(), getHeight(), "Paste", rendererFactory,
			messageDisplayer) {
		@Override
		protected void onClick(int mouseButton) {
			String newValue;
			try {
				newValue = (String) Toolkit.getDefaultToolkit()
						.getSystemClipboard()
						.getContents(null)
						.getTransferData(DataFlavor.stringFlavor);
			} catch(UnsupportedFlavorException | IOException | IllegalStateException e) {
				return;
			}
			T valueObj;
			try {
				valueObj = processNewValue(stringToValue(newValue));
			} catch(IllegalArgumentException e) {
				displayMessage(e.getMessage());
				return;
			}
			displayIfChanged(valueObj);
		}
	});
	super.addMenuEntries(builder, menu, menuChain);
}

@Override
protected void updateDisplay() {
	super.updateDisplay();
	setDisplayString(valueToString());
}

@Override
public void setDisplayString(String displayString) {
	super.setDisplayString(isHandlingKeyboard() ? displayString : getName() + ": " + displayString);
}

@Override
protected void onHover() {
	if(!isHandlingKeyboard()) super.onHover();
}

}