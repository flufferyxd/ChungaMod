package magnileve.chungamod.gui;

import static magnileve.chungamod.gui.InheritanceButtonRendererFactory.putRF;
import static magnileve.chungamod.gui.InheritanceButtonRendererFactory.putST;
import static magnileve.chungamod.gui.values.ValueButtonFactoryMap.putVF;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import org.lwjgl.opengl.GL11;

import magnileve.chungamod.gui.values.ArrayButton;
import magnileve.chungamod.gui.values.BlockPosButton;
import magnileve.chungamod.gui.values.BooleanButton;
import magnileve.chungamod.gui.values.ColorButton;
import magnileve.chungamod.gui.values.ColorPicker;
import magnileve.chungamod.gui.values.ColorComponentButton;
import magnileve.chungamod.gui.values.DecimalRangeButton;
import magnileve.chungamod.gui.values.EnumButton;
import magnileve.chungamod.gui.values.IntRangeButton;
import magnileve.chungamod.gui.values.JSONButton;
import magnileve.chungamod.gui.values.RangeButton;
import magnileve.chungamod.gui.values.StringButton;
import magnileve.chungamod.gui.values.ValueButton;
import magnileve.chungamod.gui.values.ValueButtonFactory;
import magnileve.chungamod.gui.values.ValueButtonFactoryLink;
import magnileve.chungamod.gui.values.ValueButtonFactoryMap;
import magnileve.chungamod.gui.values.ValueButtonTypeFactory;
import magnileve.chungamod.gui.values.ValueProcessor;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.ClassHashMap;
import magnileve.chungamod.util.Corner;
import magnileve.chungamod.util.json.JSONManager;
import magnileve.chungamod.util.json.JSONUtil;
import magnileve.chungamod.util.math.Vec2i;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * Contains utility methods for buttons, mostly to build provided implementations of button factories.
 * @author Magnileve
 */
public class Buttons {

private Buttons() {}

/**
 * Creates a {@link ValueButtonFactory} from a {@link ValueButtonFactoryLink}.
 * @param factory internal factory
 * @param clickGUI GUI of this factory
 * @param buttonIDSupplier generates button IDs
 * @param json manages JSON
 * @param rendererFactory builds renderers for buttons
 * @return a new {@link ValueButtonFactory}
 */
public static ValueButtonFactory completeValueButtonFactory(ValueButtonFactoryLink factory,
		ClickGUI clickGUI, IntSupplier buttonIDSupplier, JSONManager json, ButtonRendererFactory<ClickGUIButton> rendererFactory) {
	return new ValueButtonFactory() {
		@Override
		public <T> ValueButton<T> build(ValueButtonFactory factory1, int id, int x, int y, int widthIn, int heightIn,
				String name, T value, ValueProcessor<T> valueProcessor,
				Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder, MenuChain menuChain, String description,
				Class<T> type, boolean allowNull, String limits) {
			return factory.build(factory1, id, x, y, widthIn, heightIn, name,
					value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits);
		}
		@Override public ClickGUI getClickGUI() {return clickGUI;}
		@Override public IntSupplier getButtonIDSupplier() {return buttonIDSupplier;}
		@Override public JSONManager getJSONManager() {return json;}
		@Override public ButtonRendererFactory<ClickGUIButton> getRendererFactory() {return rendererFactory;}
	};
}

/**
 * Builds the default {@link ValueButtonFactory}.
 * @param clickGUI GUI of this factory
 * @param buttonIDSupplier generates button IDs
 * @param json manages JSON
 * @param rendererFactory builds renderers for buttons
 * @return a new {@link ValueButtonFactory}
 */
public static ValueButtonFactory defaultValueButtonFactory(ClickGUI clickGUI,
		IntSupplier buttonIDSupplier, JSONManager json, ButtonRendererFactory<ClickGUIButton> rendererFactory) {
	return completeValueButtonFactory(new ValueButtonFactoryMap(DEFAULT_VALUE_BUTTON_FACTORIES, JSON_FACTORY, ARRAY_FACTORY, ENUM_FACTORY),
			clickGUI, buttonIDSupplier, json, rendererFactory);
}

/**
 * A {@link ValueButtonFactoryLink} that creates a {@link JSONButton} from any type.
 */
public static final ValueButtonFactoryLink JSON_FACTORY = new ValueButtonFactoryLink() {
	@Override
	public <T> ValueButton<T> build(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn, String name,
			T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits) {
		ClickGUI clickGUI = factory.getClickGUI();
		return new JSONButton<T>(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), clickGUI.getDisplayer(),
				value, valueProcessor, menuButtonBuilder, menuChain, factory.getButtonIDSupplier(), description,
				clickGUI.getKeyboardPermit(), type, factory.getJSONManager(), allowNull);
	}
};

/**
 * A {@link ValueButtonFactoryLink} that creates an {@link ArrayButton} from any array type extending {@code Object[]}.
 * If a non-array type is passed into this factory, it returns {@code null}.
 */
public static final ValueButtonFactoryLink ARRAY_FACTORY = new ValueButtonFactoryLink() {
	@SuppressWarnings("unchecked")
	@Override
	public <T> ValueButton<T> build(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn, String name,
			T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits) {
		return type.isArray() ? (ValueButton<T>) makeArrayButton(factory, id, x, y, widthIn, heightIn, name,
				value, valueProcessor, menuButtonBuilder, menuChain, description, type.getComponentType(), allowNull, limits) : null;
	}
	
	@SuppressWarnings("unchecked")
	private <C> ArrayButton<C> makeArrayButton(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn,
			String name, Object value, ValueProcessor<?> valueProcessor, Function<?, ?> menuButtonBuilder,
			MenuChain menuChain, String description, Class<C> componentType, boolean allowNull, String limits) {
		ClickGUI clickGUI = factory.getClickGUI();
		return new ArrayButton<>(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), clickGUI.getDisplayer(),
				(C[]) value, (ValueProcessor<C[]>) valueProcessor,
				(Function<ValueButton<C[]>, MenuButtonBuilder>) menuButtonBuilder, menuChain, factory.getButtonIDSupplier(),
				description, componentType, clickGUI, factory.getJSONManager(), limits, factory);
	}
};

/**
 * A {@link ValueButtonFactoryLink} that creates an {@link EnumButton} from any {@code enum} type.
 * If a non-enum type is passed into this factory, it returns {@code null}.
 */
public static final ValueButtonFactoryLink ENUM_FACTORY = new ValueButtonFactoryLink() {
	@Override
	public <T> ValueButton<T> build(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn, String name,
			T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
			MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits) {
		ClickGUI clickGUI = factory.getClickGUI();
		return type.isEnum() ? new EnumButton<>(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(),
				clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder, menuChain,
				factory.getButtonIDSupplier(), description, clickGUI.getMousePermit(), type.getEnumConstants()) : null;
	}
};

/**
 * Maps types to default factories for value buttons containing those types.
 */
public static final Map<Class<?>, ValueButtonTypeFactory<?>> DEFAULT_VALUE_BUTTON_FACTORIES;

static {
	Map<Class<?>, ValueButtonTypeFactory<?>> map = new ClassHashMap<>();
	DEFAULT_VALUE_BUTTON_FACTORIES = Collections.unmodifiableMap(map);
	putVF(map, Boolean.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) ->
			new BooleanButton(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), factory.getClickGUI().getDisplayer(),
					value, valueProcessor, menuButtonBuilder, menuChain, factory.getButtonIDSupplier(), description));
	putVF(map, String.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				String[] limitArray = JSONUtil.parseLimits(limits);
				ClickGUI clickGUI = factory.getClickGUI();
				
				if(limitArray[0].equals("values")) return new EnumButton<>(id, x, y, widthIn, heightIn, name,
						factory.getRendererFactory(), clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder,
						menuChain, factory.getButtonIDSupplier(), description, clickGUI.getMousePermit(), limitArray[1].split(","));
				
				return new StringButton(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), clickGUI.getDisplayer(),
						value, valueProcessor, menuButtonBuilder, menuChain, factory.getButtonIDSupplier(),
						description, clickGUI.getKeyboardPermit());
			});
	putVF(map, Integer.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				Vec2i range = IntRangeButton.parseRange(limits);
				ClickGUI clickGUI = factory.getClickGUI();
				return range == null ? null : new IntRangeButton.IntegerButton(id, x, y, widthIn, heightIn, name,
						factory.getRendererFactory(), clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder,
						menuChain, factory.getButtonIDSupplier(), description, clickGUI.getMousePermit(), range.getX(), range.getY());
			});
	putVF(map, Byte.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				Vec2i range = IntRangeButton.parseRange(limits);
				ClickGUI clickGUI = factory.getClickGUI();
				return range == null ? null : new IntRangeButton.ByteButton(id, x, y, widthIn, heightIn, name,
						factory.getRendererFactory(), clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder,
						menuChain, factory.getButtonIDSupplier(), description, clickGUI.getMousePermit(), range.getX(), range.getY());
			});
	putVF(map, Short.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				Vec2i range = IntRangeButton.parseRange(limits);
				ClickGUI clickGUI = factory.getClickGUI();
				return range == null ? null : new IntRangeButton.ShortButton(id, x, y, widthIn, heightIn, name,
						factory.getRendererFactory(), clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder,
						menuChain, factory.getButtonIDSupplier(), description, clickGUI.getMousePermit(), range.getX(), range.getY());
			});
	putVF(map, BigDecimal.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				Bucket<BigDecimal, BigDecimal> range = DecimalRangeButton.parseRange(limits);
				if(range == null) return null;
				ClickGUI clickGUI = factory.getClickGUI();
				return new DecimalRangeButton(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), clickGUI.getDisplayer(),
						value, valueProcessor, menuButtonBuilder, menuChain, factory.getButtonIDSupplier(),
						description, clickGUI.getMousePermit(), range.getE1(), range.getE2());
			});
	putVF(map, BlockPos.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				ClickGUI clickGUI = factory.getClickGUI();
				return new BlockPosButton(id, x, y, widthIn, heightIn, name, factory.getRendererFactory(), clickGUI.getDisplayer(),
						value, valueProcessor, menuButtonBuilder, menuChain, factory.getButtonIDSupplier(),
						description, clickGUI.getKeyboardPermit());
			});
	putVF(map, Color.class, (factory, id, x, y, widthIn, heightIn, name,
			value, valueProcessor, menuButtonBuilder, menuChain, description, type, allowNull, limits) -> {
				ClickGUI clickGUI = factory.getClickGUI();
				return new ColorButton(id, x, y, widthIn, heightIn, name,
						factory.getRendererFactory(), clickGUI.getDisplayer(), value, valueProcessor, menuButtonBuilder,
						menuChain, factory.getButtonIDSupplier(), description, clickGUI.getKeyboardPermit(),
						factory.getJSONManager(), clickGUI.getMousePermit());
			});
}

/**
 * Builds a {@link ButtonRenderer} factory for a basic {@link ClickGUIButton}.
 * @param fontRenderer font renderer
 * @param prop properties of buttons
 * @return a {@link Function} that takes in a {@code ClickGUIButton} and produces a {@code ButtonRenderer}
 */
public static Function<ClickGUIButton, ButtonRenderer> simpleRenderer(FontRenderer fontRenderer, ButtonProperties prop) {
	float zLevel = 0F;
	return b -> {
		int redfill, greenfill, bluefill;
		if(b.isHovered()) {
			redfill = prop.brighter(prop.redfill);
			greenfill = prop.brighter(prop.greenfill);
			bluefill = prop.brighter(prop.bluefill);
		} else {
			redfill = prop.redfill;
			greenfill = prop.greenfill;
			bluefill = prop.bluefill;
		}
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	};
}

/**
 * Builds a string trimmer for a basic {@link ClickGUIButton}.
 * @param fontRenderer font renderer
 * @param prop properties of buttons
 * @return a {@link Function} that takes in a {@code ClickGUIButton} and {@code String} and produces a {@code String}
 */
public static Function<Bucket<ClickGUIButton, String>, String> simpleStringTrimmer(FontRenderer fontRenderer, int ellipsisWidth) {
	return b -> trim(b.getE2(), fontRenderer, b.getE1().getWidth(), ellipsisWidth);
}

/**
 * Trims a {@code String} to fit a given width.
 * @param displayString the string to trim
 * @param fontRenderer font renderer
 * @param width maximum width of the trimmed string
 * @param ellipsisWidth width of {@code "..."}
 * @return {@code displayString} or a shortened version of it
 */
public static String trim(String displayString, FontRenderer fontRenderer, int width, int ellipsisWidth) {
	return fontRenderer.getStringWidth(displayString) > width ?
			fontRenderer.trimStringToWidth(displayString, width - ellipsisWidth) + "..." :
			displayString;
}

/**
 * Builds the default {@link ButtonRendererFactory}.
 * @param fontRenderer font renderer
 * @param prop properties of buttons
 * @return a new {@link ButtonRendererFactory} containing all default renderer factories and string trimmers.
 */
public static ButtonRendererFactory<ClickGUIButton> rendererFactory(FontRenderer fontRenderer, ButtonProperties prop) {
	Map<Class<? extends ClickGUIButton>, Class<? extends ClickGUIButton>> priorityInheritance =
			new ClassHashMap<>(4);
	return new InheritanceButtonRendererFactory<>(rendererFactoryMap(fontRenderer, prop, priorityInheritance, new ClassHashMap<>()),
			priorityInheritance, new ClassHashMap<>(64, 0.5F),
			stringTrimmerMap(fontRenderer, prop, priorityInheritance, new ClassHashMap<>()), new ClassHashMap<>(8, 0.5F));
}

/**
 * Adds all default {@link ButtonRenderer} factories to a map.
 * @param fontRenderer font renderer
 * @param prop properties of buttons
 * @param priorityInheritance map to add prioritized inherited types to
 * @param map map to add factories to
 * @return {@code map}
 */
public static Map<Class<? extends ClickGUIButton>, Function<? extends ClickGUIButton, ButtonRenderer>> rendererFactoryMap(
		FontRenderer fontRenderer, ButtonProperties prop,
		Map<Class<? extends ClickGUIButton>, Class<? extends ClickGUIButton>> priorityInheritance,
		Map<Class<? extends ClickGUIButton>, Function<? extends ClickGUIButton, ButtonRenderer>> map) {
	float zLevel = 0F;
	
	putRF(map, ClickGUIButton.class, simpleRenderer(fontRenderer, prop));
	putRF(map, DisplayButton.class, b -> {
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, Menu.class, b -> {
		int redfill = prop.redfill, greenfill = prop.greenfill, bluefill = prop.bluefill,
				x = b.getX(), y = b.getY(), height = b.getHeight(), y2 = y + height;
		
		if(b.isBeingScrolled()) {
			int padding = b.getDividerSize(), x2 = x + (padding < 2 ? b.getWidth() + 2 - padding : b.getWidth()),
					redfill1 = prop.brighter(prop.redfill), greenfill1 = prop.brighter(prop.greenfill), bluefill1 = prop.brighter(prop.bluefill),
					scrollBarX = x2 - (padding < 2 ? 2 : padding), scrollableHeight = b.getScrollableHeight(),
					scrollBarY = y + b.getScrollHeight() * height / scrollableHeight,
					scrollBarY2 = scrollBarY + (height * height - 1) / scrollableHeight + 1;
			
			return new ButtonRenderer() {
				@Override
				public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					//draw button
					buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					//draw scroll bar
					buffer.pos(x2, scrollBarY, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
					buffer.pos(scrollBarX, scrollBarY, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
					buffer.pos(scrollBarX, scrollBarY2, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
					buffer.pos(x2, scrollBarY2, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
					tessellator.draw();
					if(prop.buttonBorderWidth != 0F) {
						buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
						GlStateManager.glLineWidth(prop.buttonBorderWidth);
						//draw borders
						buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						tessellator.draw();
					}
				}
				
				@Override public void drawText() {}
			};
		} else {
			int x2 = x + b.getWidth();
			return new ButtonRenderer() {
				@Override
				public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					//draw button
					buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
					tessellator.draw();
					if(prop.buttonBorderWidth != 0F) {
						buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
						GlStateManager.glLineWidth(prop.buttonBorderWidth);
						//draw borders
						buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						tessellator.draw();
					}
				}
				
				@Override public void drawText() {}
			};
		}
	});
	priorityInheritance.put(MenuImpl.class, Menu.class);
	putRF(map, MenuImpl.Header.class, b -> {
		float textX = b.getX() + (b.getWidth() - fontRenderer.getStringWidth(b.getDisplayString())) / 2,
				textY = b.getY() + (b.getHeight() - fontRenderer.FONT_HEIGHT) / 2 + 1;
		String displayString = b.getDisplayString();
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, UpdatableDisplayButton.class, b -> {
		return b.isVisible() ? null : ButtonRenderer.BLANK;
	});
	priorityInheritance.put(UpdatableDisplayButtonImpl.class, UpdatableDisplayButton.class);
	putRF(map, ResizableDisplayButton.class, b -> {
		if(!b.isVisible()) return ButtonRenderer.BLANK;
		int x = b.getX(), y = b.getY(), x2 = x + b.getWidth(), y2 = y + b.getHeight();
		String[] displayLines = b.getDisplayLines().toArray(new String[b.getDisplayLines().size()]);
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				int lineY = y + (b.getLineHeight() - fontRenderer.FONT_HEIGHT) / 2 + 1;
				for(String line:displayLines) {
					fontRenderer.drawString(line, x, lineY, prop.buttonTextColor, false);
					lineY += b.getLineHeight();
				}
			}
		};
	});
	putRF(map, CornerDisplayButton.class, b -> {
		if(b.isVisible()) {
			if(b.getY() == -1) return null;
			Corner corner = b.getCorner();
			int x = b.getX(), y = b.getY(), height = b.getHeight(), x2 = x + b.getWidth(), y2 = y + height, lineHeight = b.getLineHeight(),
					cornersX[] = CornerDisplayButton.getVisibleCornersX(corner, x, x2),
					cornersY[] = CornerDisplayButton.getVisibleCornersY(corner, y, y2);
			String[] displayLines = b.getDisplayLines().toArray(new String[b.getDisplayLines().size()]);
			
			class CornerButtonRenderer implements ButtonRenderer, IntConsumer {
				int y, y2, cornersY[];
				
				@Override
				public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
					//draw button
					buffer.pos(x2, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
					tessellator.draw();
					if(prop.buttonBorderWidth != 0F) {
						buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
						GlStateManager.glLineWidth(prop.buttonBorderWidth);
						//draw visible borders
						for(int i = 0; i < 3; i++) buffer.pos(cornersX[i], cornersY[i], zLevel)
								.color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
						tessellator.draw();
					}
				}
				
				@Override
				public void drawText() {
					int lineY = y + (lineHeight - fontRenderer.FONT_HEIGHT) / 2 + 1;
					for(String line:displayLines) {
						fontRenderer.drawString(line, x, lineY, prop.buttonTextColor, false);
						lineY += lineHeight;
					}
				}
				
				@Override
				public void accept(int value) {
					y = value;
					y2 = y + height;
					cornersY = CornerDisplayButton.getVisibleCornersY(corner, y, y2);
				}
			}
			
			CornerButtonRenderer renderer = new CornerButtonRenderer();
			renderer.y = y;
			renderer.y2 = y2;
			renderer.cornersY = cornersY;
			b.setYValueConsumer(renderer);
			return renderer;
		} else {
			b.setYValueConsumer(null);
			return ButtonRenderer.BLANK;
		}
	});
	putRF(map, ConfigButton.class, b -> {
		int redfill, greenfill, bluefill;
		if(b.isEnabled() || b.isHovered()) {
			redfill = prop.brighter(prop.redfill);
			greenfill = prop.brighter(prop.greenfill);
			bluefill = prop.brighter(prop.bluefill);
		} else {
			redfill = prop.redfill;
			greenfill = prop.greenfill;
			bluefill = prop.bluefill;
		}
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, BooleanButton.class, b -> {
		int redfill, greenfill, bluefill;
		if(b.getValue() || b.isHovered()) {
			redfill = prop.brighter(prop.redfill);
			greenfill = prop.brighter(prop.greenfill);
			bluefill = prop.brighter(prop.bluefill);
		} else {
			redfill = prop.redfill;
			greenfill = prop.greenfill;
			bluefill = prop.bluefill;
		}
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, RangeButton.class, b -> {
		int redfill = prop.brighter(prop.redfill), greenfill = prop.brighter(prop.greenfill), bluefill = prop.brighter(prop.bluefill),
				redfill1 = prop.redfill, greenfill1 = prop.greenfill, bluefill1 = prop.bluefill,
				x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(),
				x2 = x + b.getHighlightWidth(), y2 = y + height, x3 = x + width;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw highlighted part of button
				buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				//draw rest of button
				buffer.pos(x3, y, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
				buffer.pos(x2, y, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
				buffer.pos(x3, y2, zLevel).color(redfill1, greenfill1, bluefill1, prop.alphafill).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x3, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x3, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, ColorButton.class, b -> {
		int redfill, greenfill, bluefill;
		if(b.isHovered()) {
			redfill = prop.brighter(prop.redfill);
			greenfill = prop.brighter(prop.greenfill);
			bluefill = prop.brighter(prop.bluefill);
		} else {
			redfill = prop.redfill;
			greenfill = prop.greenfill;
			bluefill = prop.bluefill;
		}
		Color value = b.getValue();
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), renderColorStart = width * 8 / 9,
				x2 = x + renderColorStart, y2 = y + height, x3 = x + width, redcolor = value.getRed(),
				greencolor = value.getGreen(), bluecolor = value.getBlue(), alphacolor = value.getAlpha();
		String displayString = b.getDisplayString();
		float textX = x + (renderColorStart - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill, greenfill, bluefill, prop.alphafill).endVertex();
				//draw selected color
				buffer.pos(x3, y, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x2, y, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x2, y2, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x3, y2, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x3, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x3, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, ColorPicker.class, b -> {
		Color value = b.getValue();
		Vec3i saturatedRGB = b.getSaturatedRGB();
		int x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height,
				triStartHeight = b.getTriStartHeight(), triX = b.getTriX(), triX2 = triX + b.getTriWidth(), triXMid = (triX + triX2) / 2,
				triY = b.getTriY(), triY2 = triY + b.getTriHeight(), redcolor = value.getRed(), greencolor = value.getGreen(),
				bluecolor = value.getBlue(), alphacolor = value.getAlpha(),
				redS = saturatedRGB.getX(), greenS = saturatedRGB.getY(), blueS = saturatedRGB.getZ();
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (triStartHeight - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x2, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				buffer.pos(x2, y2, zLevel).color(prop.redfill, prop.greenfill, prop.bluefill, prop.alphafill).endVertex();
				//draw selected color
				buffer.pos(x + triStartHeight, y, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x, y, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x, y + triStartHeight, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				buffer.pos(x + triStartHeight, y + triStartHeight, zLevel).color(redcolor, greencolor, bluecolor, alphacolor).endVertex();
				tessellator.draw();
				buffer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
				//draw color triangle
				buffer.pos(triXMid, triY, zLevel).color(0x00, 0x00, 0x00, 0xFF).endVertex();
				buffer.pos(triX, triY2, zLevel).color(0xFF, 0xFF, 0xFF, 0xFF).endVertex();
				buffer.pos(triX2, triY2, zLevel).color(redS, greenS, blueS, 0xFF).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, ColorComponentButton.class, b -> {
		Color minColor = b.getMinColor(), maxColor = b.getMaxColor();
		int redfill = minColor.getRed(), greenfill = minColor.getGreen(), bluefill = minColor.getBlue(),
				redfill1 = maxColor.getRed(), greenfill1 = maxColor.getGreen(), bluefill1 = maxColor.getBlue(),
				minAlpha = b.renderAlpha() ? minColor.getAlpha() : ColorComponentButton.MAX_INT,
				maxAlpha = b.renderAlpha() ? maxColor.getAlpha() : ColorComponentButton.MAX_INT,
				x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width, y2 = y + height;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw button
				buffer.pos(x + width, y, zLevel).color(redfill1, greenfill1, bluefill1, maxAlpha).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, minAlpha).endVertex();
				buffer.pos(x, y + height, zLevel).color(redfill, greenfill, bluefill, minAlpha).endVertex();
				buffer.pos(x + width, y + height, zLevel).color(redfill1, greenfill1, bluefill1, maxAlpha).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x2, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x2, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	putRF(map, ColorComponentButton.HueButton.class, b -> {
		Color minColor = b.getMinColor(), oneThirdColor = b.getOneThirdColor(),
				twoThirdsColor = b.getTwoThirdsColor(), maxColor = b.getMaxColor();
		int redfill = minColor.getRed(), greenfill = minColor.getGreen(), bluefill = minColor.getBlue(),
				redfill1 = oneThirdColor.getRed(), greenfill1 = oneThirdColor.getGreen(), bluefill1 = oneThirdColor.getBlue(),
				redfill2 = twoThirdsColor.getRed(), greenfill2 = twoThirdsColor.getGreen(), bluefill2 = twoThirdsColor.getBlue(),
				redfill3 = maxColor.getRed(), greenfill3 = maxColor.getGreen(), bluefill3 = maxColor.getBlue(),
				x = b.getX(), y = b.getY(), width = b.getWidth(), height = b.getHeight(), x2 = x + width / 3, y2 = y + height,
						x3 = x + width * 2 / 3, x4 = x + width;
		String displayString = b.getDisplayString();
		float textX = x + (width - fontRenderer.getStringWidth(displayString)) / 2,
				textY = y + (height - fontRenderer.FONT_HEIGHT) / 2 + 1;
		
		return new ButtonRenderer() {
			@Override
			public void drawButton(Tessellator tessellator, BufferBuilder buffer) {
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
				//draw red-green
				buffer.pos(x2, y, zLevel).color(redfill1, greenfill1, bluefill1, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x, y, zLevel).color(redfill, greenfill, bluefill, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x, y2, zLevel).color(redfill, greenfill, bluefill, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill1, greenfill1, bluefill1, ColorComponentButton.MAX_INT).endVertex();
				//draw green-blue
				buffer.pos(x3, y, zLevel).color(redfill2, greenfill2, bluefill2, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x2, y, zLevel).color(redfill1, greenfill1, bluefill1, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x2, y2, zLevel).color(redfill1, greenfill1, bluefill1, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x3, y2, zLevel).color(redfill2, greenfill2, bluefill2, ColorComponentButton.MAX_INT).endVertex();
				//draw blue-red
				buffer.pos(x4, y, zLevel).color(redfill3, greenfill3, bluefill3, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x3, y, zLevel).color(redfill2, greenfill2, bluefill2, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x3, y2, zLevel).color(redfill2, greenfill2, bluefill2, ColorComponentButton.MAX_INT).endVertex();
				buffer.pos(x4, y2, zLevel).color(redfill3, greenfill3, bluefill3, ColorComponentButton.MAX_INT).endVertex();
				tessellator.draw();
				if(prop.buttonBorderWidth != 0F) {
					buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
					GlStateManager.glLineWidth(prop.buttonBorderWidth);
					//draw borders
					buffer.pos(x4, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					buffer.pos(x4, y2, zLevel).color(prop.redborder, prop.greenborder, prop.blueborder, prop.alphaborder).endVertex();
					tessellator.draw();
				}
			}
			
			@Override
			public void drawText() {
				fontRenderer.drawString(displayString, textX, textY, prop.buttonTextColor, false);
			}
		};
	});
	return map;
}

/**
 * Adds all default string trimmers to a map.
 * @param fontRenderer font renderer
 * @param prop properties of buttons
 * @param priorityInheritance map to add prioritized inherited types to
 * @param map map to add string trimmers to
 * @return {@code map}
 */
public static Map<Class<? extends ClickGUIButton>, Function<Bucket<? extends ClickGUIButton, String>, String>> stringTrimmerMap(
		FontRenderer fontRenderer, ButtonProperties prop,
		Map<Class<? extends ClickGUIButton>, Class<? extends ClickGUIButton>> priorityInheritance,
		Map<Class<? extends ClickGUIButton>, Function<Bucket<? extends ClickGUIButton, String>, String>> map) {
	int ellipsisWidth = fontRenderer.getStringWidth("...");
	putST(map, ClickGUIButton.class, simpleStringTrimmer(fontRenderer, ellipsisWidth));
	putST(map, ColorButton.class, b -> trim(b.getE2(), fontRenderer, ColorButton.getTextWidth(b.getE1().getWidth()), ellipsisWidth));
	return map;
}

/**
 * Contains several property values for rendering buttons.
 * @author Magnileve
 */
public static class ButtonProperties {
	public final int redfill;
	public final int greenfill;
	public final int bluefill;
	public final int alphafill;
	
	public final int redborder;
	public final int greenborder;
	public final int blueborder;
	public final int alphaborder;
	
	public final int buttonTextColor;
	
	public final float buttonBorderWidth;
	
	private final double brightenFactor;
	
	public ButtonProperties(double brightenFactor, int redfill, int greenfill, int bluefill, int alphafill,
			int redborder, int greenborder, int blueborder, int alphaborder, int buttonTextColor, float buttonBorderWidth) {
		this.brightenFactor = brightenFactor;
		this.redfill = redfill;
		this.greenfill = greenfill;
		this.bluefill = bluefill;
		this.alphafill = alphafill;
		this.redborder = redborder;
		this.greenborder = greenborder;
		this.blueborder = blueborder;
		this.alphaborder = alphaborder;
		this.buttonTextColor = buttonTextColor;
		this.buttonBorderWidth = buttonBorderWidth;
	}
	
	/**
	 * Increases the brightness of an RGB byte.
	 * @param brightness a red, blue, or green value from 0-255
	 * @return a brighter red, blue, or green value from 0-255
	 */
	public int brighter(int brightness) {
		int i = (int) (1D / (1D - brightenFactor));
		if(brightness < i) brightness = i;
		return Math.min((int) (brightness / brightenFactor), 255);
	}
}

}