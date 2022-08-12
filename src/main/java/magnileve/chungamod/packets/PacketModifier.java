package magnileve.chungamod.packets;

import magnileve.chungamod.packets.PacketListener.Priority;
import magnileve.chungamod.packets.PacketListener.Type;

/**
 * A packet modifier implementing {@link PacketModifierCore} and additionally containing a {@link Priority} and {@link Type}.
 * @author Magnileve
 */
public abstract class PacketModifier implements PacketModifierCore {

private final Priority priority;
private final Type type;

/**
 * Creates a new {@code PacketModifier} with a priority and type.
 * @param priority the priority
 * @param type the packet type
 */
public PacketModifier(Priority priority, Type type) {
	this.priority = priority;
	this.type = type;
}

/**
 * Gets this modifier's priority.
 * @return this modifier's priority
 */
public Priority getPriority() {
	return priority;
}

/**
 * Gets this modifier's packet type.
 * @return this modifier's packet type
 */
public Type getType() {
	return type;
}

@Override
public String toString() {
	return "PacketModifier with priority: " + priority + ", type: " + type;
}

}