package magnileve.chungamod.util.json;

import java.awt.Color;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.json.JSONException;
import org.json.JSONTokener;
import org.json.JSONWriter;

import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.util.ArrayBuildList;
import magnileve.chungamod.util.Bucket;
import magnileve.chungamod.util.ClassHashMap;
import magnileve.chungamod.util.Colors;
import magnileve.chungamod.util.MCUtil;
import magnileve.chungamod.util.math.Vec2i;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;

/**
 * <p>
 * Manages serialization and deserialization of Java objects to and from JSON.  Some specifications are as follows:
 * </p>
 * <p style="margin-left:40px">
 * - Primitives and primitive arrays are not supported.<br>
 * - Arrays of length zero are used instead of {@code null} for array types.<br>
 * - Deserialized arrays cannot contain {@code null} values.<br>
 * - Default values cannot be {@code null}.<br>
 * - Value limiter strings are parsed into identifiers and limits using {@link JSONUtil#parseLimits(String)}.
 * </p>
 * @author Magnileve
 */
@ContainsInit
public class JSONManager {

private static final int DEFAULTS_LENGTH = 9;
private static final Class<?>[] DEFAULT_CLASSES = new Class<?>[DEFAULTS_LENGTH];
private static final JSONProcessors<?>[] DEFAULT_JSON_PROCESSORS = new JSONProcessors<?>[DEFAULTS_LENGTH];
private static final ValueLimiter<?>[][] DEFAULT_VALUE_LIMITERS = new ValueLimiter<?>[DEFAULTS_LENGTH][];
private static final Function<?, ?>[][] DEFAULT_VALUE_LIMITER_DEFAULTS = new Function[DEFAULTS_LENGTH][];
private static final String[][] DEFAULT_VALUE_LIMITER_IDS = new String[DEFAULTS_LENGTH][];

private static Minecraft mc;

private final Map<Class<?>, JSONProcessors<?>> processors;

@Init.PreInit1
public static void init(Minecraft mcIn) {
	mc = mcIn;
}

/**
 * Creates a new {@code JSONManager}.
 */
public JSONManager() {
	processors = new ClassHashMap<>();
}

/**
 * Serializes an object to JSON.
 * @param <T> class of object
 * @param w writes JSON
 * @param asClass the Java type to serialize the object as
 * @param obj the object to be serialized
 * @throws JSONException if the given Java type does not have a registered {@code JSONConverter}, or if an I/O error occurs
 */
public <T> void serialize(JSONWriter w, Class<? super T> asClass, T obj) {
	Class<?> componentType = asClass;
	int dimensions = 0;
	while(componentType.isArray()) {
		componentType = componentType.getComponentType();
		dimensions++;
	}
	serialize(w, componentType, dimensions, obj);
}

/**
 * Serializes an object to JSON.
 * @param <T> class of object
 * @param asClass the Java type to serialize the object as
 * @param obj the object to be serialized
 * @return a {@code String} containing the JSON value of the object
 * @throws JSONException if the given Java type does not have a registered {@code JSONConverter}, or if an I/O error occurs
 */
public <T> String serializeToString(Class<? super T> asClass, T obj) {
	StringBuilder str = new StringBuilder();
	serialize(new JSONWriter(str) {{
		mode = 'o';
	}}, asClass, obj);
	return str.toString();
}

/**
 * Serializes an object to JSON.
 * @param <T> serialization type of object
 * @param w writes JSON
 * @param asClass the Java type to serialize the object as
 * @param obj the object to be serialized
 * @throws JSONException if the given Java type does not have a registered {@code JSONConverter}, or if an I/O error occurs
 * @throws ClassCastException if {@code obj} cannot be casted to {@code asClass}
 */
public final <T> void serializeCast(JSONWriter w, Class<T> asClass, Object obj) {
	T casted = asClass.cast(obj);
	serialize(w, asClass, casted);
}

/**
 * Serializes an object to JSON.
 * @param <T> serialization type of object
 * @param asClass the Java type to serialize the object as
 * @param obj the object to be serialized
 * @return a {@code String} containing the JSON value of the object
 * @throws JSONException if the given Java type does not have a registered {@code JSONConverter}, or if an I/O error occurs
 * @throws ClassCastException if {@code obj} cannot be casted to {@code asClass}
 */
public final <T> String serializeToStringCast(Class<T> asClass, Object obj) {
	T casted = asClass.cast(obj);
	return serializeToString(asClass, casted);
}

/**
 * Serializes an object to JSON.
 * @param <T> type of object
 * @param <C> component type if object is array; otherwise same as {@code T}
 * @param w writes JSON
 * @param componentType component type if object type is array; otherwise type of object
 * @param dimensions array dimensions if object type is array; otherwise zero
 * @param obj the object to be serialized
 * @throws JSONException if the given Java type does not have a registered {@code JSONConverter}, or if an I/O error occurs
 */
private <T, C> void serialize(JSONWriter w, Class<C> componentType, int dimensions, T obj) {
	JSONConverter<? super C> converter = getProcessors(componentType).converter;
	if(dimensions == 0) {
		@SuppressWarnings("unchecked")
		C value = (C) obj;
		serializeValue(w, value, converter);
	} else serializeArray(w, dimensions, converter, (Object[]) obj);
}

/**
 * Serializes an array object to JSON.
 * @param <T> component type
 * @param w writes JSON
 * @param dimensions array dimensions
 * @param converter serializes objects of the component type to JSON
 * @param array the array to be serialized
 */
private <T> void serializeArray(JSONWriter w, int dimensions, JSONConverter<? super T> converter, Object[] array) {
	w.array();
	if(array != null && array.length != 0) {
		int compDimensions = dimensions - 1;
		if(compDimensions == 0) {
			@SuppressWarnings("unchecked")
			T[] baseArray = (T[]) array;
			for(T value:baseArray) serializeValue(w, value, converter);
		} else for(Object[] subArray:(Object[][]) array) serializeArray(w, compDimensions, converter, subArray);
	}
	w.endArray();
}

/**
 * Serializes a non-array object to JSON.
 * @param <T> type of object
 * @param w writes JSON
 * @param obj the object to be serialized
 * @param converter serializes objects of the given type to JSON
 * @throws JSONException if an I/O error occurs
 */
private <T> void serializeValue(JSONWriter w, T obj, JSONConverter<? super T> converter) {
	if(obj == null) w.value(null);
	else converter.serialize(w, obj);
}

/**
 * Deserializes an object from JSON.
 * @param <T> type of object
 * @param p reads JSON
 * @param toClass type of object to be deserialized
 * @param limiterString value limiter string
 * @param allowNull {@code true} if JSON null should map to {@code null}; {@code false} if it should map to default values
 * @return the deserialized object, possibly {@code null} only if {@code allowNull} is {@code true}
 * @throws JSONException if a JSON deserializing error occurs, an I/O error occurs, or a {@link DefaultValueNotSupportedException} is thrown
 * @throws IllegalArgumentException if the deserialized value does not satisfy the limits declared by {@code limiterString}
 */
public <T> T deserialize(JSONTokener p, Class<T> toClass, String limiterString, boolean allowNull) throws JSONException, IllegalArgumentException {
	ArrayBuildList<Class<?>> getTypes = new ArrayBuildList<>(new Class<?>[1]);
	Class<?> componentType = toClass;
	while(componentType.isArray()) {
		getTypes.add(componentType);
		componentType = componentType.getComponentType();
	}
	getTypes.add(componentType);
	int typesSize = getTypes.size();
	Class<?>[] builderArray = getTypes.getArray();
	Class<?>[] componentTypes = new Class<?>[typesSize];
	for(int i = 0, iReverse = typesSize; i < typesSize; i++)
		componentTypes[i] = builderArray[--iReverse];
	return deserialize(p, componentType, componentTypes, limiterString, typesSize - 1, allowNull);
}

/**
 * Deserializes a nullable object from JSON.
 * @param <T> type of object
 * @param p reads JSON
 * @param toClass type of object to be deserialized
 * @param limiterString value limiter string
 * @return the deserialized object, possibly {@code null}
 * @throws JSONException if a JSON deserializing error occurs, an I/O error occurs, or a {@link DefaultValueNotSupportedException} is thrown
 * @throws IllegalArgumentException if the deserialized value does not satisfy the limits declared by {@code limiterString}
 */
public final <T> T deserialize(JSONTokener p, Class<T> toClass, String limiterString) throws JSONException, IllegalArgumentException {
	return deserialize(p, toClass, limiterString, true);
}

/**
 * Deserializes a nullable object from JSON.
 * @param <T> type of object
 * @param p reads JSON
 * @param toClass type of object to be deserialized
 * @return the deserialized object, possibly {@code null}
 * @throws JSONException if a JSON deserializing error occurs, or if an I/O error occurs
 */
public final <T> T deserialize(JSONTokener p, Class<T> toClass) throws JSONException {
	return deserialize(p, toClass, null, true);
}

/**
 * Deserializes an object from JSON.
 * @param <T> type of object
 * @param <C> component type if object is array; otherwise same as {@code T}
 * @param p p reads JSON
 * @param componentType component type if object is array; otherwise type of object
 * @param componentTypes an array of types starting with the component type and
 * followed by array types of each applicable dimension.  This array's length is equal to {@code dimensions + 1}
 * @param limiterString value limiter string
 * @param dimensions array dimensions, or zero if the object type is not an array
 * @param allowNull {@code true} if JSON null should map to {@code null}; {@code false} if it should map to default values
 * @return the deserialized object, possibly {@code null} only if {@code allowNull} is {@code true}
 * @throws JSONException if a JSON deserializing error occurs, an I/O error occurs, or a {@link DefaultValueNotSupportedException} is thrown
 * @throws IllegalArgumentException if the deserialized value does not satisfy the limits declared by {@code limiterString}
 */
private <T, C> T deserialize(JSONTokener p, Class<C> componentType, Class<?>[] componentTypes, String limiterString,
		int dimensions, boolean allowNull) throws JSONException, IllegalArgumentException {
	JSONProcessors<C> processors = getProcessors(componentType);
	ValueLimiter<? super C> limiter;
	String limits;
	Optional<C> defaultValue;
	if(limiterString == null || limiterString.isEmpty()) {
		limiter = null;
		limits = null;
		defaultValue = Optional.of(processors.defaultValue);
	} else {
		String[] array = JSONUtil.parseLimits(limiterString);
		Bucket<ValueLimiter<? super C>, Function<String, C>> bucket = processors.limiters.get(array[0]);
		limiter = bucket.getE1();
		limits = array[1];
		try {
			defaultValue = Optional.of(bucket.getE2().apply(limits));
		} catch(DefaultValueNotSupportedException e) {
			defaultValue = Optional.empty();
		}
	}
	try {
		@SuppressWarnings("unchecked")
		T returnValue = (T) (dimensions == 0 ?
				deserializeValue(p, limits, processors, limiter, defaultValue, processors.allowNull && allowNull) :
				deserializeArray(p, componentTypes, dimensions - 1, limits, processors, limiter, defaultValue));
		return returnValue;
	} catch(DefaultValueNotSupportedException e) {
		if(e.getValueType().equals(void.class)) throw new JSONException(new DefaultValueNotSupportedException(
				componentType, limits == null ? null : JSONUtil.parseLimits(limiterString)[0]));
		throw new JSONException(e);
	}
}

/**
 * Deserializes an array object from JSON.
 * @param <T> component type
 * @param p reads JSON
 * @param dimensionTypes an array of types starting with the component type and followed by array types of each applicable dimension
 * @param compDimensions dimensions of the array to be deserialized minus one
 * @param limits limits of value
 * @param processors JSON processors for the object type
 * @param limiter value limiter
 * @param defaultValue default value, if one exists
 * @return the deserialized array
 * @throws JSONException
 * @throws IllegalArgumentException
 * @throws DefaultValueNotSupportedException
 */
private <T> Object[] deserializeArray(JSONTokener p, Class<?>[] dimensionTypes, int compDimensions, String limits,
		JSONProcessors<T> processors, ValueLimiter<? super T> limiter, Optional<T> defaultValue)
		throws JSONException, IllegalArgumentException, DefaultValueNotSupportedException {
	Class<?> componentType = dimensionTypes[compDimensions];
	if(JSONUtil.nullOrChar(p, '[') || p.nextClean() == ']') return (Object[]) Array.newInstance(componentType, 0);
	p.back();
	ArrayBuildList<Object> builder = new ArrayBuildList<>((Object[]) Array.newInstance(componentType, 1));
	if(compDimensions == 0) {
		do builder.add(deserializeValue(p, limits, processors, limiter, defaultValue, false));
		while(JSONUtil.hasNext(p, ']'));
	} else {
		compDimensions--;
		do builder.add(deserializeArray(p, dimensionTypes, compDimensions, limits, processors, limiter, defaultValue));
		while(JSONUtil.hasNext(p, ']'));
	}
	return builder.trim().getArray();
}

/**
 * Deserializes a non-array object from JSON.
 * @param <T> type of object
 * @param p reads JSON
 * @param limits limits of value
 * @param processors JSON processors for the object type
 * @param limiter value limiter
 * @param defaultValue default value, if one exists
 * @param allowNull {@code true} if JSON null should map to {@code null}; {@code false} if it should map to default values
 * @return the deserialized object, possibly {@code null} only if {@code allowNull} is {@code true}
 * @throws JSONException if a JSON deserializing error occurs, or if an I/O error occurs
 * @throws IllegalArgumentException if the deserialized value does not satisfy the limits declared by {@code limiterString}
 * @throws DefaultValueNotSupportedException if JSON null is read, {@code allowNull} is {@code false}, and {@code defaultValue} is empty
 */
private <T> T deserializeValue(JSONTokener p, String limits, JSONProcessors<T> processors,
		ValueLimiter<? super T> limiter, Optional<T> defaultValue, boolean allowNull)
				throws JSONException, IllegalArgumentException, DefaultValueNotSupportedException {
	if(JSONUtil.checkNull(p))
		return allowNull ? null : defaultValue.orElseThrow(() -> new DefaultValueNotSupportedException(void.class, ""));
	T value = processors.converter.deserialize(p);
	if(limiter != null) try {
		limiter.verify(value, limits);
	} catch(IllegalArgumentException e) {
		throw new IllegalArgumentException(e.getMessage() + p.toString());
	}
	return value;
}

/**
 * Ensures that a value satisfies a limiter.
 * If the given value is an array, all contained values are tested.
 * @param <T> value type
 * @param value a value
 * @param type value type
 * @param limiterString a value limiter string
 * @throws IllegalArgumentException if the given value does not satisfy the given value limiter string
 * @see JSONUtil#parseLimits(String)
 */
public <T> void testLimits(T value, Class<T> type, String limiterString) throws IllegalArgumentException {
	if(limiterString != null && !limiterString.isEmpty()) {
		int dimensions = 0;
		Class<?> componentType = type;
		while(componentType.isArray()) {
			componentType = componentType.getComponentType();
			dimensions++;
		}
		testLimits(componentType, dimensions, value, limiterString);
	}
}

/**
 * Ensures that a value satisfies a limiter.
 * If the given value is an array, all contained values are tested.
 * @param <C> component type if the value is an array; otherwise the value type
 * @param componentType component type if the value is an array; otherwise the value type
 * @param dimensions array dimensions, or zero if the value type is not an array
 * @param value the value to test
 * @param limiterString value limiter string
 * @throws IllegalArgumentException if the given value does not satisfy the limits declared by the given value limiter string
 */
@SuppressWarnings("unchecked")
private <C> void testLimits(Class<C> componentType, int dimensions, Object value, String limiterString) throws IllegalArgumentException {
	String[] array = JSONUtil.parseLimits(limiterString);
	ValueLimiter<? super C> limiter = getProcessors(componentType).limiters.get(array[0]).getE1();
	String limits = array[1];
	if(dimensions == 0) limiter.verify((C) value, limits);
	else testLimitsArray(dimensions, (Object[]) value, limits, limiter);
}

/**
 * Ensures that all values in an array satisfy a limiter.
 * @param <T> component type
 * @param dimensions array dimensions
 * @param array the array of values to test
 * @param limits value limits
 * @param limiter value limiter
 * @throws IllegalArgumentException if any given value in the array does not satisfy the limits declared by the given value limiter string
 */
@SuppressWarnings("unchecked")
private <T> void testLimitsArray(int dimensions, Object[] array, String limits, ValueLimiter<? super T> limiter)
		throws IllegalArgumentException {
	if(array != null && array.length != 0) {
		int compDimensions = dimensions - 1;
		if(compDimensions == 0) for(T value:(T[]) array) limiter.verify(value, limits);
		else for(Object[] subArray:(Object[][]) array) testLimitsArray(compDimensions, subArray, limits, limiter);
	}
}

/**
 * Returns the default value for a type.
 * @param <T> default value type
 * @param forClass default value type
 * @return the default value for the given type
 */
public <T> T getDefault(Class<T> forClass) {
	return getDefault(forClass, null);
}

/**
 * Returns the default value for a type within the given limits.
 * @param <T> default value type
 * @param forClass default value type
 * @param limiterString optional value limiter string
 * @return the default value for the given type within the given limits,
 * or if no value limiter string is provided, the default value for the given type
 * @throws DefaultValueNotSupportedException if {@code limiterString} is not {@code null},
 * and a default value cannot be obtained for the value limiter ID
 */
@SuppressWarnings("unchecked")
public <T> T getDefault(Class<T> forClass, String limiterString) throws DefaultValueNotSupportedException {
	if(forClass.isArray()) return (T) Array.newInstance(forClass.getComponentType(), 0);
	if(limiterString == null || limiterString.isEmpty()) return getProcessors(forClass).defaultValue;
	String[] limitsInfo = JSONUtil.parseLimits(limiterString);
	return getProcessors(forClass).limiters.get(limitsInfo[0]).getE2().apply(limitsInfo[1]);
}

/**
 * Adds a {@link JSONConverter} for a type to this manager.
 * JSON null will always map to default values for this type.
 * @param <T> type of objects to be serialized and deserialized
 * @param forClass type of objects to be serialized and deserialized
 * @param converter the converter
 * @param defaultValue non-null default value for this type
 * @throws IllegalStateException if a {@code JSONConverter} has already been registered for the given type
 */
public <T> void addConverter(Class<T> forClass, JSONConverter<T> converter, T defaultValue) {
	addConverter(forClass, converter, defaultValue, false);
}

/**
 * Adds a {@link JSONConverter} for a type to this manager.
 * @param <T> type of objects to be serialized and deserialized
 * @param forClass type of objects to be serialized and deserialized
 * @param converter the converter
 * @param defaultValue non-null default value for this type
 * @param allowNull {@code true} if {@code null} values are allowed to be deserialized;
 * {@code false} if JSON null should always map to default values
 * @throws IllegalStateException if a {@code JSONConverter} has already been registered for the given type
 */
public <T> void addConverter(Class<T> forClass, JSONConverter<T> converter, T defaultValue, boolean allowNull) {
	addConverter(forClass, new JSONProcessors<>(converter, defaultValue, allowNull));
}

/**
 * Adds a {@link JSONProcessors} instance for a type to this manager.
 * @param <T> type of objects to be serialized and deserialized
 * @param forClass type of objects to be serialized and deserialized
 * @param processors the processors instance
 * @param defaultValue non-null default value for this type
 * @throws IllegalStateException if a {@code JSONProcessors} instance has already been registered for the given type
 */
private <T> void addConverter(Class<?> forClass, JSONProcessors<?> processors) {
	JSONProcessors<?> previousValue = this.processors.put(forClass, processors);
	if(previousValue != null) {
		this.processors.put(forClass, previousValue);
		throw new IllegalStateException("JSONConverter already registered for " + forClass);
	}
}

/**
 * Adds a {@link ValueLimiter} to this manager.
 * @param <T> type of objects to be deserialized
 * @param forClass type of objects to be deserialized
 * @param identifier identifier of value limiter
 * @param limiter the value limiter
 * @param getDefaultValue either gets non-null default values satisfying given limits or throws {@link DefaultValueNotSupportedException}
 * @throws IllegalStateException if a value limiter with the same identifier has already been added to the same type
 */
public <T> void addLimiter(Class<T> forClass, String identifier, ValueLimiter<? super T> limiter, Function<String, T> getDefaultValue) {
	Bucket<ValueLimiter<? super T>, Function<String, T>> previousValue = getProcessors(forClass)
			.limiters.put(identifier, Bucket.of(limiter, getDefaultValue));
	if(previousValue != null) {
		getProcessors(forClass).limiters.put(identifier, previousValue);
		throw new IllegalStateException("ValueLimiter already registered for " + forClass + " with ID " + identifier);
	}
}

/**
 * Adds a {@link ValueLimiter} without support for default values to this manager.
 * @param <T> type of objects to be deserialized
 * @param forClass type of objects to be deserialized
 * @param identifier identifier of value limiter
 * @param limiter the value limiter
 * @throws IllegalStateException if a value limiter with the same identifier has already been added to the same type
 */
public <T> void addLimiter(Class<T> forClass, String identifier, ValueLimiter<? super T> limiter) {
	addLimiter(forClass, identifier, limiter, limits -> {
		throw new DefaultValueNotSupportedException(forClass, identifier);
	});
}

/**
 * Gets the {@link JSONProcessors} for a type.
 * @param <T> type to be processed
 * @param forClass type to be processed
 * @return the {@link JSONProcessors} for the given type
 * @throws JSONException if a {@link JSONConverter} has not been registered for the given type
 */
@SuppressWarnings("unchecked")
private <T> JSONProcessors<T> getProcessors(Class<T> forClass) throws JSONException {
	JSONProcessors<T> result = (JSONProcessors<T>) processors.get(forClass);
	if(result == null) {
		if(forClass.isEnum()) return getEnumProcessors(forClass.getEnumConstants());
		else throw new JSONException("No registered JSONConverter for " + forClass);
	}
	return result;
}

/**
 * Creates a {@link JSONProcessors} instance to process an enum type.
 * @param <T> value type
 * @param constants enum constants
 * @return a {@link JSONProcessors} instance for the given enum constants
 */
private <T> JSONProcessors<T> getEnumProcessors(T[] constants) {
	return new JSONProcessors<>(new JSONConverter<T>() {
		@Override
		public void serialize(JSONWriter w, T obj) {
			w.value(obj.toString());
		}
		
		@Override
		public T deserialize(JSONTokener p) throws JSONException {
			Object value = p.nextValue();
			if(value instanceof String) {
				String name = (String) value;
				for(T enumValue:constants) if(name.equals(enumValue.toString())) return enumValue;
				throw p.syntaxError("Given value: " + name + ", accepted values: " + Arrays.toString(constants));
			}
			if(value instanceof Integer) {
				int i = (int) value;
				if(i < 0 || i >= constants.length)
					throw p.syntaxError("Index " + i + " must be between 0 and " + (constants.length - 1));
				return constants[i];
			}
			throw p.syntaxError("Expected enum name or ordinal and instead saw type: " + value.getClass().getName());
		}
	}, constants[0], false);
}

/**
 * Adds Chungamod's default JSON converters and value limiters to a {@code JSONManager}.
 * @param manager a {@code JSONManager}
 * @throws IllegalStateException if a {@link JSONConverter} has already been registered for the same type as any default converter
 */
public static void addDefaultProcessors(JSONManager manager) {
	for(int i = 0; i < DEFAULTS_LENGTH; i++) {
		Class<?> type = DEFAULT_CLASSES[i];
		manager.addConverter(type, DEFAULT_JSON_PROCESSORS[i]);
		if(DEFAULT_VALUE_LIMITERS[i] != null) {
			ValueLimiter<?>[] valueLimiters = DEFAULT_VALUE_LIMITERS[i];
			String[] valueLimitersIDs = DEFAULT_VALUE_LIMITER_IDS[i];
			Function<?, ?>[] valueLimiterDefaults = DEFAULT_VALUE_LIMITER_DEFAULTS[i];
			for(int h = 0; h < valueLimiters.length; h++)
				addLimiter(manager, type, valueLimitersIDs[h], valueLimiters[h], valueLimiterDefaults[h]);
		}
	}
}

/**
 * Adds a {@link ValueLimiter} to a JSON manager.
 * @param <T> type of objects to be deserialized
 * @param m a JSON manager
 * @param forClass type of objects to be deserialized
 * @param identifier identifier of value limiter
 * @param limiter the value limiter
 * @param getDefaultValue if not null, gets non-null default values satisfying given limits
 */
@SuppressWarnings("unchecked")
private static <T> void addLimiter(JSONManager m, Class<T> forClass, String identifier, ValueLimiter<?> limiter, Function<?, ?> getDefaultValue) {
	if(getDefaultValue == null) m.addLimiter(forClass, identifier, (ValueLimiter<? super T>) limiter);
	else m.addLimiter(forClass, identifier, (ValueLimiter<? super T>) limiter, (Function<String, T>) getDefaultValue);
}

/**
 * Contains a {@link JSONConverter},
 * map of limiter identifiers to {@link ValueLimiter} instances,
 * non-null default value,
 * and {@code boolean} to allow {@code null} values to be deserialized
 * @param <T> type of values to be processed
 * @author Magnileve
 */
private static class JSONProcessors<T> {
	private final JSONConverter<T> converter;
	private final Map<String, Bucket<ValueLimiter<? super T>, Function<String, T>>> limiters;
	private final T defaultValue;
	private final boolean allowNull;
	
	private JSONProcessors(JSONConverter<T> converter, T defaultValue, boolean allowNull) {
		this.converter = converter;
		limiters = new HashMap<>(4);
		this.defaultValue = Objects.requireNonNull(defaultValue);
		this.allowNull = allowNull;
	}
}

static {
	@SuppressWarnings("unchecked")
	ValueLimiter<Number>[] numberLimiters = new ValueLimiter[] {
		(ValueLimiter<Number>) (value, limits) -> {
			String[] splitLimits = limits.split(",");
			int min;
			int max;
			try {
				if(splitLimits.length != 2) throw new NumberFormatException();
				min = Integer.parseInt(splitLimits[0]);
				max = Integer.parseInt(splitLimits[1]);
			} catch(NumberFormatException e) {
				throw ValueLimiter.syntaxError(limits, "<min>,<max>");
			}
			int v = value.intValue();
			if(v < min || v > max) throw new IllegalArgumentException("Value " + v + " must be between " + min + " and " + max);
		}
	};
	String[] numberLimiterIDs = {"range"};
	
	DEFAULT_CLASSES[0] = Integer.class;
	DEFAULT_JSON_PROCESSORS[0] = new JSONProcessors<>(new JSONConverter<Integer>() {
		@Override
		public void serialize(JSONWriter writer, Integer obj) {
			writer.value(obj);
		}
		
		@Override
		public Integer deserialize(JSONTokener p) {
			return JSONUtil.nextValue(p, Integer.class);
		}
	}, Integer.valueOf(0), false);
	DEFAULT_VALUE_LIMITER_DEFAULTS[0] = new Function[] {
		(Function<String, Integer>) limits -> Integer.valueOf(limits.split(",", 2)[0])
	};
	
	DEFAULT_CLASSES[1] = Byte.class;
	DEFAULT_JSON_PROCESSORS[1] = new JSONProcessors<>(new JSONConverter<Byte>() {
		@Override
		public void serialize(JSONWriter writer, Byte obj) {
			writer.value(obj);
		}
		
		@Override
		public Byte deserialize(JSONTokener p) {
			int value = JSONUtil.nextValue(p, Integer.class);
			if(value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) throw p.syntaxError("Expected byte and instead saw integer");
			return (byte) value;
		}
	}, Byte.valueOf((byte) 0), false);
	DEFAULT_VALUE_LIMITER_DEFAULTS[1] = new Function[] {
		(Function<String, Byte>) limits -> Byte.valueOf(limits.split(",", 2)[0])
	};
	
	DEFAULT_CLASSES[2] = Short.class;
	DEFAULT_JSON_PROCESSORS[2] = new JSONProcessors<>(new JSONConverter<Short>() {
		@Override
		public void serialize(JSONWriter writer, Short obj) {
			writer.value(obj);
		}
		
		@Override
		public Short deserialize(JSONTokener p) {
			int value = JSONUtil.nextValue(p, Integer.class);
			if(value < Short.MIN_VALUE || value > Short.MAX_VALUE) throw p.syntaxError("Expected short and instead saw integer");
			return (short) value;
		}
	}, Short.valueOf((short) 0), false);
	DEFAULT_VALUE_LIMITER_DEFAULTS[2] = new Function[] {
		(Function<String, Short>) limits -> Short.valueOf(limits.split(",", 2)[0])
	};
	
	for(int i = 0; i < 3; i++) {
		DEFAULT_VALUE_LIMITERS[i] = numberLimiters;
		DEFAULT_VALUE_LIMITER_IDS[i] = numberLimiterIDs;
	}
	
	DEFAULT_CLASSES[3] = String.class;
	DEFAULT_JSON_PROCESSORS[3] = new JSONProcessors<>(new JSONConverter<String>() {
		@Override
		public void serialize(JSONWriter writer, String obj) {
			writer.value(obj);
		}
		
		@Override
		public String deserialize(JSONTokener p) {
			return JSONUtil.nextValue(p, String.class);
		}
	}, "", false);
	DEFAULT_VALUE_LIMITERS[3] = new ValueLimiter[] {
			(ValueLimiter<String>) (value, limits) -> {
				if(!value.matches(limits)) throw new IllegalArgumentException(
						"Value \"" + value + "\" must match regex \"" + limits + "\"");
			},
			(ValueLimiter<String>) (value, limits) -> {
				String[] values = limits.split(",", -1);
				for(int i = 0; i < values.length; i++) if(values[i].equals(value)) return;
				throw new IllegalArgumentException("Given value: " + value + ", accepted values: " + limits.replace(",", ", "));
			}
	};
	DEFAULT_VALUE_LIMITER_IDS[3] = new String[] {"matches", "values"};
	DEFAULT_VALUE_LIMITER_DEFAULTS[3] = new Function[] {
			null,
			(Function<String, String>) limits -> limits.split(",", 2)[0]
	};
	
	DEFAULT_CLASSES[4] = Boolean.class;
	DEFAULT_JSON_PROCESSORS[4] = new JSONProcessors<>(new JSONConverter<Boolean>() {
		@Override
		public void serialize(JSONWriter writer, Boolean obj) {
			writer.value(obj);
		}
		
		@Override
		public Boolean deserialize(JSONTokener p) {
			return JSONUtil.nextValue(p, Boolean.class);
		}
	}, Boolean.FALSE, false);
	
	DEFAULT_CLASSES[5] = BlockPos.class;
	DEFAULT_JSON_PROCESSORS[5] = new JSONProcessors<>(new JSONConverter<BlockPos>() {
		@Override
		public void serialize(JSONWriter writer, BlockPos obj) {
			if(obj == null) writer.value(null);
			else writer.array()
					.value(obj.getX())
					.value(obj.getY())
					.value(obj.getZ())
					.endArray();
		}
		
		@Override
		public BlockPos deserialize(JSONTokener p) {
			Entity camera = mc.getRenderViewEntity();
			if(camera != null) {
				char c = p.nextClean();
				switch(c) {
				case '.':
					return MCUtil.getPos(camera);
				case '*':
					RayTraceResult rayTrace = camera.rayTrace(512D, 1.0F);
					if(rayTrace.typeOfHit == RayTraceResult.Type.MISS) throw new JSONException("Not looking at a block");
					if(p.next() == '*') return rayTrace.getBlockPos().offset(rayTrace.sideHit);
					p.back();
					return rayTrace.getBlockPos();
				case '[':
					int[] coords = new int[3];
					for(int i = 0; i < 3; i++) {
						if(p.nextClean() == '~') {
							int baseCoord = MathHelper.floor(i == 0 ? camera.posX : i == 1 ? camera.posY : camera.posZ);
							char c1 = p.nextClean();
							if(c1 == ',' || c1 == ']') {
								p.back();
								coords[i] = baseCoord;
							} else {
								p.back();
								coords[i] = baseCoord + JSONUtil.nextValue(p, Integer.class);
							}
						} else {
							p.back();
							coords[i] = JSONUtil.nextValue(p, Integer.class);
						}
						JSONUtil.next(p, i == 2 ? ']' : ',');
					}
					return new BlockPos(coords[0], coords[1], coords[2]);
				default:
					throw p.syntaxError(
							"Expected \"null\", \".\" (current position), \"*\" (highlighted block), " +
							"\"**\" (in front of highlighted block), or \"[<x>,<y>,<z>]\" (coordinates array)");
				}
			}
			JSONUtil.next(p, '[');
			int x = JSONUtil.nextValue(p, Integer.class);
			JSONUtil.next(p, ',');
			int y = JSONUtil.nextValue(p, Integer.class);
			JSONUtil.next(p, ',');
			int z = JSONUtil.nextValue(p, Integer.class);
			JSONUtil.next(p, ']');
			return new BlockPos(x, y, z);
		}
	}, BlockPos.ORIGIN, true);
	
	DEFAULT_CLASSES[6] = Color.class;
	DEFAULT_JSON_PROCESSORS[6] = new JSONProcessors<>(new JSONConverter<Color>() {
		@Override
		public void serialize(JSONWriter w, Color obj) {
			w.value(Colors.toString(obj));
		}
		
		@Override
		public Color deserialize(JSONTokener p) throws JSONException {
			char quote = p.nextClean();
			if(quote != '\"' && quote != '\'') throw p.syntaxError("Expected '\"' or ''' and instead saw " + quote);
			if(p.next() != '#') p.back();
			String str = p.nextString(quote);
			int intValue;
			try {
				intValue = Integer.parseUnsignedInt(str, 16);
			} catch(NumberFormatException e) {
				throw p.syntaxError("Expected hex code " + e.getMessage());
			}
			return new Color(intValue, true);
		}
	}, Color.black, false);
	
	DEFAULT_CLASSES[7] = BigDecimal.class;
	DEFAULT_JSON_PROCESSORS[7] = new JSONProcessors<>(new JSONConverter<BigDecimal>() {
		@Override
		public void serialize(JSONWriter w, BigDecimal obj) {
			w.value(obj);
		}
		
		@Override
		public BigDecimal deserialize(JSONTokener p) throws JSONException {
			Object value = p.nextValue();
			if(value instanceof BigDecimal) return (BigDecimal) value;
			if(value instanceof Integer) return new BigDecimal((Integer) value);
			if(value instanceof Number) return new BigDecimal(((Number) value).toString());
			throw p.syntaxError("Expected number and instead saw type: " + value.getClass().getName());
		}
	}, new BigDecimal(0), false);
	DEFAULT_VALUE_LIMITERS[7] = new ValueLimiter[] {
			(ValueLimiter<BigDecimal>) (value, limits) -> {
				String[] splitLimits = limits.split(",");
				BigDecimal min;
				BigDecimal max;
				try {
					if(splitLimits.length != 2) throw new NumberFormatException();
					min = new BigDecimal(splitLimits[0]);
					max = new BigDecimal(splitLimits[1]);
				} catch(NumberFormatException e) {
					throw ValueLimiter.syntaxError(limits, "<min>,<max>");
				}
				if(value.compareTo(min) < 0 || value.compareTo(max) > 0)
					throw new IllegalArgumentException("Value " + value + " must be between " + min + " and " + max);
			}
	};
	DEFAULT_VALUE_LIMITER_IDS[7] = numberLimiterIDs;
	DEFAULT_VALUE_LIMITER_DEFAULTS[7] = new Function[] {
			(Function<String, BigDecimal>) limits -> new BigDecimal(limits.split(",", 2)[0])
	};
	
	DEFAULT_CLASSES[8] = Vec2i.class;
	DEFAULT_JSON_PROCESSORS[8] = new JSONProcessors<>(new JSONConverter<Vec2i>() {
		@Override
		public void serialize(JSONWriter w, Vec2i obj) {
			w.array().value(obj.getX()).value(obj.getY()).endArray();
		}
		
		@Override
		public Vec2i deserialize(JSONTokener p) throws JSONException {
			if(JSONUtil.nullOrChar(p, '[')) return null;
			int x = JSONUtil.nextValue(p, Integer.class);
			JSONUtil.next(p, ',');
			int y = JSONUtil.nextValue(p, Integer.class);
			JSONUtil.next(p, ']');
			return new Vec2i(x, y);
		}
	}, Vec2i.ORIGIN, true);
	ValueLimiter<Number> rangeLimiter = numberLimiters[0];
	DEFAULT_VALUE_LIMITERS[8] = new ValueLimiter[] {
			(ValueLimiter<Vec2i>) (value, limits) -> {
				String[] intLimits = limits.split(";");
				try {
					if(intLimits.length != 2) throw new ValueLimiterSyntaxException();
					rangeLimiter.verify(value.getX(), intLimits[0]);
					rangeLimiter.verify(value.getY(), intLimits[1]);
				} catch(ValueLimiterSyntaxException e) {
					throw ValueLimiter.syntaxError(limits, "<minX>,<maxX>;<minY>,<maxY>");
				}
			}
	};
	DEFAULT_VALUE_LIMITER_IDS[8] = numberLimiterIDs;
	DEFAULT_VALUE_LIMITER_DEFAULTS[8] = new Function[] {
			(Function<String, Vec2i>) limits -> {
				String[] intLimits = limits.split(";");
				return new Vec2i(Integer.parseInt(intLimits[0].split(",", 2)[0]), Integer.parseInt(intLimits[1].split(",", 2)[0]));
			}
	};
}

}