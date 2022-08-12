package magnileve.chungamod.gui.values;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.PotentialInfoButton;
import magnileve.chungamod.gui.UpdatableDisplay;

/**
 * A button that sets the default value to a {@link ValueButton} when left clicked.
 * @param <T> value type
 * @author Magnileve
 */
public class SetToDefaultButton<T> extends PotentialInfoButton {

/**
 * Default name of a {@code SetToDefaultButton}.
 */
public static final String DEFAULT_BUTTON_NAME = "Reset";

private final ValueButton<T> b;

/**
 * Creates a new default setter button.
 * @param id button ID
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param b the {@code ValueButton} this button sets a value to
 */
public SetToDefaultButton(int id, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, ValueButton<T> b) {
	this(id, DEFAULT_BUTTON_NAME, rendererFactory, messageDisplayer, b);
}

/**
 * Creates a new default setter button.
 * @param id button ID
 * @param displayString string to be displayed on this button
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param b the {@code ValueButton} this button sets a value to
 */
public SetToDefaultButton(int id, String displayString, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, ValueButton<T> b) {
	super(id, 0, 0, b.getWidth(), b.getHeight(), displayString, rendererFactory, messageDisplayer);
	this.b = b;
}

@Override
protected void onClick(int mouseButton) {
	if(mouseButton == 0) {
		T value;
		try {
			value = b.processDefaultValue();
		} catch(IllegalArgumentException e) {
			return;
		}
		if(b.displayIfChanged(value)) displayMessage("Reset to: " + b.valueToString());
	}
}

}