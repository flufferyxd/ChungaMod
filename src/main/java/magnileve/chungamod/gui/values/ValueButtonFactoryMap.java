package magnileve.chungamod.gui.values;

import java.util.Map;
import java.util.function.Function;

import magnileve.chungamod.gui.MenuChain;
import magnileve.chungamod.gui.MenuButtonBuilder;

/**
 * Builds value buttons by using internal mappings of value types to instances of {@link ValueButtonTypeFactory}.
 * @author Magnileve
 */
public class ValueButtonFactoryMap implements ValueButtonFactoryLink {

private final Map<Class<?>, ValueButtonTypeFactory<?>> map;
private final ValueButtonFactoryLink defaultFactory;
private final ValueButtonFactoryLink arrayFactory;
private final ValueButtonFactoryLink enumFactory;

/**
 * Creates a new {@code ValueButtonFactoryMap} with the provided internal factories.
 * @param map maps value types to factories for each type
 * @param defaultFactory default factory
 * @param arrayFactory factory for array types
 * @param enumFactory factory for enum types
 */
public ValueButtonFactoryMap(Map<Class<?>, ValueButtonTypeFactory<?>> map, ValueButtonFactoryLink defaultFactory,
		ValueButtonFactoryLink arrayFactory, ValueButtonFactoryLink enumFactory) {
	this.map = map;
	this.defaultFactory = defaultFactory;
	this.arrayFactory = arrayFactory;
	this.enumFactory = enumFactory;
}

/**
 * First, builds from the array factory if the value type is an array type.<br>
 * If not, builds from the type factory map if the value type has a mapping.<br>
 * If not, builds from the enum factory if the value type is an enum type.<br>
 * If not, builds from the default factory.<br>
 * If any factories return {@code null}, the step is skipped.
 * If the default factory returns {@code null}, this method returns {@code null}.
 */
@Override
public <T> ValueButton<T> build(ValueButtonFactory factory, int id, int x, int y, int widthIn, int heightIn, String name,
		T value, ValueProcessor<T> valueProcessor, Function<ValueButton<T>, MenuButtonBuilder> menuButtonBuilder,
		MenuChain menuChain, String description, Class<T> type, boolean allowNull, String limits) {
	if(type.isArray()) {
		ValueButton<T> button = arrayFactory.build(factory, id, x, y, widthIn, heightIn, name, value, valueProcessor, menuButtonBuilder,
				menuChain, description, type, allowNull, limits);
		if(button != null) return button;
	}
	@SuppressWarnings("unchecked")
	ValueButtonTypeFactory<T> typeFactory = (ValueButtonTypeFactory<T>) map.get(type);
	if(typeFactory != null) {
		ValueButton<T> button = typeFactory.build(factory, id, x, y, widthIn, heightIn, name, value, valueProcessor, menuButtonBuilder,
				menuChain, description, type, allowNull, limits);
		if(button != null) return button;
	}
	if(type.isEnum()) {
		ValueButton<T> button = enumFactory.build(factory, id, x, y, widthIn, heightIn, name, value, valueProcessor, menuButtonBuilder,
				menuChain, description, type, allowNull, limits);
		if(button != null) return button;
	}
	return defaultFactory.build(factory, id, x, y, widthIn, heightIn, name, value, valueProcessor, menuButtonBuilder,
			menuChain, description, type, allowNull, limits);
}

/**
 * Maps a type to a {@link ValueButtonTypeFactory}.
 * @param <T> value type
 * @param map map of types to factories
 * @param type value type
 * @param factory factory for this value type
 */
public static <T> void putVF(Map<Class<?>, ValueButtonTypeFactory<?>> map, Class<T> type, ValueButtonTypeFactory<T> factory) {
	map.put(type, factory);
}

}