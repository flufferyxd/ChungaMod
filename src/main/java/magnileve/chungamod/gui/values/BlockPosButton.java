package magnileve.chungamod.gui.values;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;

import magnileve.chungamod.gui.ButtonRendererFactory;
import magnileve.chungamod.gui.ClickGUIButton;
import magnileve.chungamod.gui.Menu;
import magnileve.chungamod.gui.MenuButtonBuilder;
import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.UpdatableDisplay;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.MCUtil;
import magnileve.chungamod.util.Permit;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

/**
 * A button with a {@code BlockPos} value.
 * @author Magnileve
 */
@ContainsInit
public class BlockPosButton extends StringValueButton<BlockPos> {

private static Minecraft mc;

@Init
private static void init(Minecraft mcIn) {
	mc = mcIn;
}

/**
 * Creates a new {@code BlockPos} value button.
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
public BlockPosButton(int id, int x, int y, int widthIn, int heightIn, String name, ButtonRendererFactory<ClickGUIButton> rendererFactory,
		UpdatableDisplay messageDisplayer, BlockPos value, ValueProcessor<BlockPos> valueProcessor,
		Function<ValueButton<BlockPos>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, IntSupplier buttonIDSupplier,
		String description, Permit<Runnable> keyboardPermit) {
	super(id, x, y, widthIn, heightIn, name, rendererFactory, messageDisplayer,
			value, valueProcessor, menuButtonBuilder, menuChain, buttonIDSupplier, description, keyboardPermit);
}

@Override
public BlockPos stringToValue(String valueString) throws IllegalArgumentException {
	return MCUtil.parseCoords(valueString.split(" "));
}

@Override
public String valueToString() {
	BlockPos value = getValue();
	return value == null ? "null" : value.getX() + " " + value.getY() + " " + value.getZ();
}

@Override
public boolean equals(BlockPos value1, BlockPos value2) {
	return Objects.equals(value1, value2);
}

@Override
public void addMenuEntries(ArrayBuildList<ClickGUIButton> builder, Menu menu, MenuChain menuChain) {
	Entity camera = mc.getRenderViewEntity();
	if(camera != null) {
		builder.add(new SetterButton<BlockPos>(buttonIDSupplier.getAsInt(), "Current", rendererFactory, messageDisplayer,
				this, () -> new BlockPos(camera.posX, camera.posY, camera.posZ)));
		builder.add(new SetterButton<BlockPos>(buttonIDSupplier.getAsInt(), "Highlighed", rendererFactory, messageDisplayer, this, () -> {
			RayTraceResult rayTrace = camera.rayTrace(512D, 1.0F);
			if(rayTrace.typeOfHit == RayTraceResult.Type.MISS) throw new IllegalStateException("Not looking at a block");
			return rayTrace.getBlockPos();
		}));
		builder.add(new SetterButton<BlockPos>(buttonIDSupplier.getAsInt(), "Before Highlighed", rendererFactory, messageDisplayer, this, () -> {
			RayTraceResult rayTrace = camera.rayTrace(512D, 1.0F);
			if(rayTrace.typeOfHit == RayTraceResult.Type.MISS) throw new IllegalStateException("Not looking at a block");
			return rayTrace.getBlockPos().offset(rayTrace.sideHit);
		}));
	}
	super.addMenuEntries(builder, menu, menuChain);
}

}