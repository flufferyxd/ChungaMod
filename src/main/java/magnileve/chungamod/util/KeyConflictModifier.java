package magnileve.chungamod.util;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

public class KeyConflictModifier {

public static final IKeyConflictContext ACTIVE_KEYBIND = new IKeyConflictContext() {
	@Override public boolean isActive() {return true;}
	@Override public boolean conflicts(IKeyConflictContext other) {return this == other;}
};
public static final IKeyConflictContext INACTIVE_KEYBIND = new IKeyConflictContext() {
	@Override public boolean isActive() {return false;}
	@Override public boolean conflicts(IKeyConflictContext other) {return this == other;}
};

private final KeyBinding keyBinding;
private final IKeyConflictContext normalContext;

public KeyConflictModifier(KeyBinding keyBinding, IKeyConflictContext context) {
	this.keyBinding = keyBinding;
	normalContext = keyBinding.getKeyConflictContext();
	keyBinding.setKeyConflictContext(context);
}

public void revert() {
	keyBinding.setKeyConflictContext(normalContext);
}

}