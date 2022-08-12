package magnileve.chungamod.packets;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

/**
 * Operates on a packet.
 * @author Magnileve
 */
@FunctionalInterface
public interface PacketModifierCore {

/**
 * Called by the client when a packet is being sent or received.
 * @param packet the packet being processed
 * @return the packet, or a replacement for it
 */
public Packet<? extends INetHandler> onPacket(Packet<? extends INetHandler> packet);

}