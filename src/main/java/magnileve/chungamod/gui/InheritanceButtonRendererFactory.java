package magnileve.chungamod.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.InheritanceMap;

/**
 * A {@link ButtonRendererFactory} back by instances of {@link InheritanceMap} for building renderers and trimming strings.
 * Also contains two utility methods for creating maps to be used by this class.
 * @param <T> common type extended by buttons
 * @author Magnileve
 */
public class InheritanceButtonRendererFactory<T> implements ButtonRendererFactory<T> {

private final InheritanceMap<Class<? extends T>, T, ButtonRenderer> factoryMap;
private final InheritanceMap<Class<? extends T>, Bucket<T, String>, String> trimStringMap;

/**
 * Creates a new {@code InheritanceButtonRendererFactory} with the given maps.
 * @param factoryMap maps buttons types to renderer factories
 * @param priorityInheritance if not null, maps types to supertypes they should inherit when multiple inheritable supertypes are found
 * @param factoryCache cache of button types mapped to renderer factories
 * @param trimStringMap maps button types to string trimming methods
 * @param trimStringCache cache of button types mapped to string trimming methods
 */
@SuppressWarnings("unchecked")
public InheritanceButtonRendererFactory(Map<Class<? extends T>, Function<? extends T, ButtonRenderer>> factoryMap,
		Map<Class<? extends T>, Class<? extends T>> priorityInheritance,
		Map<Class<? extends T>, ?> factoryCache, Map<Class<? extends T>, Function<Bucket<? extends T, String>, String>> trimStringMap,
		Map<Class<? extends T>, ?> trimStringCache) {
	this.factoryMap = new InheritanceMap<>((Map<Class<? extends T>, Function<T, ButtonRenderer>>) (Map<?, ?>) factoryMap,
			priorityInheritance, factoryCache,
			(Function<Class<? extends T>, Class<? extends T>[]>) (Function<?, ?>) InheritanceMap.GET_INHERITED_TYPES);
	this.trimStringMap = new InheritanceMap<>((Map<Class<? extends T>, Function<Bucket<T, String>, String>>) (Map<?, ?>) trimStringMap,
			priorityInheritance, trimStringCache,
			(Function<Class<? extends T>, Class<? extends T>[]>) (Function<?, ?>) InheritanceMap.GET_INHERITED_TYPES);
}

/**
 * Creates a new {@code InheritanceButtonRendererFactory} with the given maps
 * and creates default {@link HashMap} instances for the caches.
 * @param factoryMap maps buttons types to renderer factories
 * @param priorityInheritance if not null, maps types to supertypes they should inherit when multiple inheritable supertypes are found
 * @param trimStringMap maps button types to string trimming methods
 */
public InheritanceButtonRendererFactory(Map<Class<? extends T>, Function<? extends T, ButtonRenderer>> factoryMap,
		Map<Class<? extends T>, Class<? extends T>> priorityInheritance,
		Map<Class<? extends T>, Function<Bucket<? extends T, String>, String>> trimStringMap) {
	this(factoryMap, priorityInheritance, new HashMap<>(), trimStringMap, new HashMap<>());
}

@SuppressWarnings("unchecked")
@Override
public ButtonRenderer buildRenderer(T b) {
	return factoryMap.apply((Class<? extends T>) b.getClass(), b);
}

@SuppressWarnings("unchecked")
@Override
public String trim(T b, String displayString) {
	return trimStringMap.apply((Class<? extends T>) b.getClass(), Bucket.of(b, displayString));
}

/**
 * Maps a type to a {@link ButtonRenderer} factory.
 * @param <T1> common type extended by buttons
 * @param <T2> type of button this factory should be used for
 * @param map map of types to factories
 * @param type button type
 * @param factory factory for this type
 */
public static <T1, T2 extends T1> void putRF(Map<Class<? extends T1>, Function<? extends T1, ButtonRenderer>> map,
		Class<T2> type, Function<T2, ButtonRenderer> factory) {
	map.put(type, factory);
}

/**
 * Maps a type to a string trimming method.
 * @param <T1> common type extended by buttons
 * @param <T2> type of button this string trimming method should be used for
 * @param map map of types to string trimming methods
 * @param type button type
 * @param stringTrimmer string trimming method for this type
 */
@SuppressWarnings("unchecked")
public static <T1, T2 extends T1> void putST(Map<Class<? extends T1>, Function<Bucket<? extends T1, String>, String>> map,
		Class<T2> type, Function<Bucket<T2, String>, String> stringTrimmer) {
	((Map<Class<T2>, Function<Bucket<T2, String>, String>>) (Map<?, ?>) map).put(type, stringTrimmer);
}

}