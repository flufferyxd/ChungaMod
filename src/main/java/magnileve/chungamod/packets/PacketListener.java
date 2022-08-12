package magnileve.chungamod.packets;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

/**
 * Manages packet modifiers for the packet pipeline.
 * @author Magnileve
 */
public class PacketListener extends ChannelDuplexHandler {

private static PacketListener instance;
private static Logger log;

private final List<PacketModifier> cPacketModifiers;
private final List<PacketModifier> sPacketModifiers;

public static void init(Logger logIn) {
	log = logIn;
}

/**
 * Initializes the packet listener.
 * @param network the new server connection
 */
public static void onConnect(NetworkManager network) {
	if(instance == null) {
		log.info("Creating packet listener");
		instance = new PacketListener();
		network.channel().pipeline().addBefore("packet_handler", "chungamod_packet_listener", instance);
	}
}

/**
 * Shuts down the packet listener.
 */
public static void onDisconnect() {
	log.info("Removing packet listener");
	instance = null;
}

private PacketListener() {
	cPacketModifiers = new CopyOnWriteArrayList<>();
	sPacketModifiers = new CopyOnWriteArrayList<>();
}

/**
 * Gets the packet listener, or {@code null} if not connected to a server.
 */
public static PacketListener get() {
	return instance;
}

@Override
public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	if(!sPacketModifiers.isEmpty() && msg instanceof Packet) {
		@SuppressWarnings("unchecked")
		Packet<? extends INetHandler> packet = (Packet<? extends INetHandler>) msg;
		for(PacketModifier packetModifier:sPacketModifiers) {
			packet = packetModifier.onPacket(packet);
			if(packet == null) return;
		}
		msg = packet;
	}
	super.channelRead(ctx, msg);
}

@Override
public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
	if(!cPacketModifiers.isEmpty() && msg instanceof Packet) {
		@SuppressWarnings("unchecked")
		Packet<? extends INetHandler> packet = (Packet<? extends INetHandler>) msg;
		for(PacketModifier packetModifier:cPacketModifiers) {
			packet = packetModifier.onPacket(packet);
			if(packet == null) return;
		}
		msg = packet;
	}
	super.write(ctx, msg, promise);
}

/**
 * Adds a packet modifier to the packet listener.
 * @param packetModifier packet modifier to be added
 * @param type determines if the packet modifier should be ran on client packets, server packets, or all packets
 * @param priority priority for the packet modifier to be ran
 * @return the {@code PacketModifier} containing the packet modifier, its priority, and its type
 */
public PacketModifier add(Priority priority, Type type, PacketModifierCore packetModifier) {
	PacketModifier newPacketModifier = new PacketModifier(priority, type) {
		@Override
		public Packet<? extends INetHandler> onPacket(Packet<? extends INetHandler> packet) {
			return packetModifier.onPacket(packet);
		}
	};
	log.debug("Adding {}", newPacketModifier);
	if(type == Type.CLIENT || type == Type.ALL) addSorted(cPacketModifiers, newPacketModifier, priority);
	if(type == Type.SERVER || type == Type.ALL) addSorted(sPacketModifiers, newPacketModifier, priority);
	return newPacketModifier;
}

private static void addSorted(List<PacketModifier> list, PacketModifier input, Priority priority) {
	int low = 0;
	int high = list.size();
	int middle = priority.ordinal() * (high / Priority.values().length);
	if(high > 1) do {
		if(list.get(middle).getPriority().compareTo(priority) > 0) high = middle;
		else low = middle;
		middle = (high + low) / 2;
	} while(high - low > 1);
	if(low == 0 && high - low == 1 && list.get(0).getPriority().compareTo(priority) > 0) high = 0;
	list.add(high, input);
}

/**
 * Adds a packet modifier to the packet listener.
 * @param packetModifier packet modifier to be added
 * @param type determines if the packet modifier should be ran on client packets, server packets, or all packets
 * @return The {@code PacketModifier} containing the packet modifier, its priority, and its type
 */
public PacketModifier add(Type type, PacketModifierCore packetModifier) {
	return add(Priority.NORMAL, type, packetModifier);
}

/**
 * Adds a packet modifier to the packet listener.
 * @param packetModifier packet modifier to be added
 * @param priority priority for the packet modifier to be ran
 * @return The {@code PacketModifier} containing the packet modifier, its priority, and its type
 */
public PacketModifier add(Priority priority, PacketModifierCore packetModifier) {
	return add(priority, Type.CLIENT, packetModifier);
}

/**
 * Adds a packet modifier to the packet listener.
 * @param packetModifier packet modifier to be added
 * @return The {@code PacketModifier} containing the packet modifier, its priority, and its type
 */
public PacketModifier add(PacketModifierCore packetModifier) {
	return add(Priority.NORMAL, Type.CLIENT, packetModifier);
}
/**
 * Removes a packet modifier from the packet listener.
 * @param packetModifier Packet modifier to be removed
 */
public void remove(PacketModifier packetModifier) {
	log.debug("Removing {}", packetModifier);
	if(packetModifier == null) return;
	Type type = packetModifier.getType();
	if(type == Type.CLIENT || type == Type.ALL) removeSorted(cPacketModifiers, packetModifier, packetModifier.getPriority());
	if(type == Type.SERVER || type == Type.ALL) removeSorted(sPacketModifiers, packetModifier, packetModifier.getPriority());
}

private static void removeSorted(List<PacketModifier> list, PacketModifier input, Priority priority) {
	int low = 0;
	int high = list.size();
	int middle = priority.ordinal() * (high / Priority.values().length);
	if(high > 1) do {
		int comparison = list.get(middle).getPriority().compareTo(priority);
		if(comparison == 0) break;
		if(comparison > 0) high = middle;
		else low = middle;
		middle = (high + low) / 2;
	} while(high - low > 1);
	for(int i = middle; i < list.size(); i++) {
		PacketModifier fromList = list.get(i);
		if(fromList.equals(input)) {
			list.remove(i);
			return;
		}
		if(!fromList.getPriority().equals(priority)) break;
	}
	for(int i = middle - 1; i >= 0; i--) {
		PacketModifier fromList = list.get(i);
		if(fromList.equals(input)) {
			list.remove(i);
			return;
		}
		if(!fromList.getPriority().equals(priority)) break;
	}
}

/**
 * Packet modifiers are ordered by their assigned priorities when registered
 * so that packet modifiers with a higher priority (lower ordinal) are processed first for a packet.
 * @author Magnileve
 */
public enum Priority {
	HIGHEST, HIGH, NORMAL, LOW, LOWEST;
}

/**
 * Type of packets a packet modifier should be processed for.
 * @author Magnileve
 */
public enum Type {
	CLIENT, SERVER, ALL;
}

}