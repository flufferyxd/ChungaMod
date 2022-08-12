package magnileve.chungamod.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * An {@code InheritanceMap} provides a function that takes in a key and a type to produce a value
 * where they key is used to determine the implementation of the function, and the type is used as the input.
 * A constant function is contained by this map to map a key to other keys it can inherit function implementations from.
 * This class allows inheritance similar to Java inheritance but declared at runtime.
 * @param <K> inheritable key type
 * @param <T> function input type
 * @param <V> function output value type
 * @author Magnileve
 */
public class InheritanceMap<K, T, V> {

/**
 * Maps a {@link Class} to the superclass and interfaces it inherits
 * as given by {@link Class#getSuperclass()} and {@link Class#getInterfaces()}.
 */
public static final Function<Class<?>, Class<?>[]> GET_INHERITED_TYPES = type -> {
	Class<?> superclass = type.getSuperclass();
	Class<?>[] interfaces = type.getInterfaces();
	if(superclass == null) return interfaces;
	Class<?>[] allTypes = new Class<?>[interfaces.length + 1];
	allTypes[0] = superclass;
	for(int i = 0; i < interfaces.length; i++) allTypes[i + 1] = interfaces[i];
	return allTypes;
};

private final Map<K, Function<T, V>> map;
private final Map<K, Node<K, T, V>> cache;
private final Map<K, K> priorityKeys;
private final Function<K, K[]> getInheritedKeys;

/**
 * Creates a new {@code InheritanceMap} containing the following maps.
 * {@link #clearCache()} should always be called directly after {@code map} has been modified.
 * {@code cache} will contain internal nodes and should not have elements added from outside of this {@code InheritanceMap}.
 * @param map map of keys to functions without inheritance
 * @param priorityKeys maps a key to the key it should inherit when there are conflicting inheritable keys
 * @param cache caches mappings of keys to their inherited keys and functions
 * @param getInheritedKeys returns an array of keys that a given key is able to inherit
 */
@SuppressWarnings("unchecked")
public InheritanceMap(Map<K, Function<T, V>> map, Map<K, K> priorityKeys, Map<K, ?> cache, Function<K, K[]> getInheritedKeys) {
	this.map = map;
	this.priorityKeys = priorityKeys == null ? Collections.EMPTY_MAP : priorityKeys;
	if(!cache.isEmpty()) cache.clear();
	this.cache = (Map<K, Node<K, T, V>>) cache;
	this.getInheritedKeys = getInheritedKeys;
}

/**
 * Creates a new {@code InheritanceMap} containing the following maps.
 * {@link #clearCache()} should always be called directly after {@code map} has been modified.
 * @param map map of keys to functions without inheritance
 * @param priorityKeys maps a key to the key it should inherit when there are conflicting inheritable keys
 * @param getInheritedKeys returns an array of keys that a given key is able to inherit
 */
@SuppressWarnings("unchecked")
public InheritanceMap(Map<K, Function<T, V>> map, Map<K, K> priorityKeys, Function<K, K[]> getInheritedKeys) {
	this(map, Collections.EMPTY_MAP, new HashMap<>(), getInheritedKeys);
}

/**
 * Applies {@code input} to the function inherited by {@code key}, and returns the result.
 * If the function returns {@code null}, until no higher inherited function exists, each is called until one does not return {@code null}.
 * If all return {@code null}, this method returns {@code null}.
 * @param key the key to the inheritable function
 * @param input the input to the function
 * @return the result of the first inherited function to not return {@code null}, or {@code null} if none do
 * @throws NullPointerException if {@code key} is {@code null}
 */
public V apply(K key, T input) {
	Node<K, T, V> node = cache.computeIfAbsent(key, this::newNode);
	return node == null ? null : node.apply(key, input);
}

/**
 * Clears this inheritance map's cache.
 * This method should always be called directly after the map of keys to functions has been modified.
 */
public void clearCache() {
	cache.clear();
}

/**
 * Creates a new node for the given key.
 * @param key a key
 * @return a node representing the key, or {@code null} if this key does not inherit any functions
 * @throws NullPointerException if {@code key} is {@code null}
 */
private Node<K, T, V> newNode(K key) {
	Objects.requireNonNull(key);
	//create nodes for inherited keys and add them to cache
	K[] inheritedKeys = getInheritedKeys.apply(key);
	ArrayBuildList<Node<K, T, V>> inheritedNodes = new ArrayBuildList<>(Node.class, 1);
	for(int i = 0; i < inheritedKeys.length; i++) Util.addIfNotNull(inheritedNodes, cache.computeIfAbsent(inheritedKeys[i], this::newNode));
	//reduce inherited keys to find inherited function, checking priorityKeys if multiple functions are inherited
	Optional<Node<K, T, V>> inherited;
	try {
		inherited = inheritedNodes.stream().reduce((node1, node2) -> {
			if(node1.overridesOrSameAs(node2)) return node1;
			if(node2.overridesOrSameAs(node1)) return node2;
			throw new AmbiguousInheritanceException(key + " inherited:\n" + node1 + ",\n" + node2);
		});
	} catch(AmbiguousInheritanceException e) {
		K priority = priorityKeys.get(key);
		if(priority == null) throw e;
		Optional<Node<K, T, V>> node = inheritedNodes.stream().filter(checkNode -> checkNode.key.equals(priority)).findAny();
		if(node.isPresent()) inherited = Optional.of(node.get());
		else throw e;
	}
	//find this key's declared function
	Function<T, V> value = map.get(key);
	//create a node containing this key's declared function and/or inherited function
	return value == null ? inherited.isPresent() ? new Node<>(key, inherited.get(), null) : null :
			new Node<>(key, inherited.orElse(null), value);
}

/**
 * A {@code Node} contains three components: the key it represents, the node it inherits, and the function declared for its key.
 * Only one of the last two components may be {@code null}.
 * @param <K> inheritable key type
 * @param <T> function input type
 * @param <V> function output value type
 * @author Magnileve
 */
private static class Node<K, T, V> {
	private final K key;
	private final Node<K, T, V> inherited;
	private final Function<T, V> value;
	
	private Node(K key, Node<K, T, V> inheritedNode, Function<T, V> value) {
		this.key = key;
		this.inherited = inheritedNode;
		this.value = value;
	}
	
	/**
	 * Applies {@code input} to the function inherited by {@code key}, and returns the result.
	 * If the function returns {@code null}, until no higher inherited function exists, each is called until one does not return {@code null}.
	 * If all return {@code null}, this method returns {@code null}.
	 * @param key the key to the inheritable function
	 * @param input the input to the function
	 * @return the result of the first inherited function to not return {@code null}, or {@code null} if none do
	 */
	private V apply(K key, T input) {
		if(value != null) {
			V result = value.apply(input);
			if(result != null) return result;
		}
		return inherited == null ? null : inherited.apply(key, input);
	}
	
	/**
	 * Gets the node declaring the function that this node inherits.
	 * If this node declares a function, this node is returned.
	 * @return the node declaring the function that this node inherits
	 */
	private Node<K, T, V> valueNode() {
		Node<K, T, V> node = this;
		while(node.value == null) node = node.inherited;
		return node;
	}
	
	/**
	 * Determines if this node's function is equal to or overrides a given node's function.
	 * @param node a node
	 * @return {@code true} if this node's function is equal to or overrides a given node's function; {@code false} otherwise
	 */
	private boolean overridesOrSameAs(Node<K, T, V> node) {
		node = node.valueNode();
		Node<K, T, V> current = valueNode();
		if(current.equals(node)) return true;
		while(current.inherited != null) {
			current = current.inherited.valueNode();
			if(current.equals(node)) return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Node ? key.equals(((Node<?, ?, ?>) obj).key) : false;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public String toString() {
		return internalToString() + ']';
	}
	
	private String internalToString() {
		return inherited == null ? "[" : inherited + " -> " + value == null ? '(' + key.toString() + ')' : "Value: " + key;
	}
}

}