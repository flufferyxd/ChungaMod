package magnileve.chungamod.gui.values;

import java.util.function.Supplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.PotentialInfoButton;
import magnileve.chungamod.gui.UpdatableDisplay;

/**
 * A button that sets a value to a {@link ValueButton} when left clicked.
 * @param <T> value type
 * @author Magnileve
 */
public class SetterButton<T> extends PotentialInfoButton {

private final ValueButton<T> b;
private final Supplier<T> valueSupplier;

/**
 * Creates a new setter button.
 * @param id button ID
 * @param displayString string to be displayed on this button
 * @param rendererFactory factory to build renderer for this button
 * @param messageDisplayer used to display messages
 * @param b the {@code ValueButton} this button sets a value to
 * @param valueSupplier gets the value set by this button; throws {@link IllegalStateException} if the value cannot be supplied
 */
public SetterButton(int id, String displayString, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, ValueButton<T> b, Supplier<T> valueSupplier) {
	super(id, 0, 0, b.getWidth(), b.getHeight(), displayString, rendererFactory, messageDisplayer);
	this.b = b;
	this.valueSupplier = valueSupplier;
}

@Override
protected void onClick(int mouseButton) {
	if(mouseButton == 0) {
		T newValue;
		try {
			newValue = valueSupplier.get();
		} catch(IllegalStateException e) {
			displayMessage(e.toString());
			return;
		}
		if(b.displayIfChanged(b.processNewValue(newValue))) displayMessage("Set to: " + b.valueToString());
	}
}

}
