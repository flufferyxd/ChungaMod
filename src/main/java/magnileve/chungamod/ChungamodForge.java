package magnileve.chungamod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import magnileve.chungamod.events.ConnectionEvent;
import magnileve.chungamod.events.EventManager;
import magnileve.chungamod.events.EventPoster;
import magnileve.chungamod.events.SwitchWorldEvent;
import magnileve.chungamod.modules.ContainsInit;
import magnileve.chungamod.modules.Init;
import magnileve.chungamod.packets.PacketListener;

/**
 * <p>
 * Manages most interaction with the Forge Mod Loader.  Other Chungamod classes that interact with Forge are:
 * </p>
 * <p style="margin-left:40px">
 * {@link magnileve.chungamod.Tick}<br>
 * {@link magnileve.chungamod.Commands}
 * </p>
 * @author Magnileve
 */
@Mod(modid = Chung.MODID,
		name = Chung.NAME,
		version = Chung.VERSION,
		acceptedMinecraftVersions = Chung.ACCEPTED_MINCERFAT_VERSIONS,
		clientSideOnly = true)
@Mod.EventBusSubscriber(modid=Chung.MODID)
@ContainsInit
public class ChungamodForge {

private static Minecraft mc;
private static EventManager eventManager;
private static boolean switchingWorld;

private static EventPoster<ConnectionEvent> connectionEvents;
private static EventPoster<SwitchWorldEvent> worldEvents;

@Instance
public static ChungamodForge instance;

@Init
static void init(Minecraft mcIn, EventManager eventManagerIn) {
	mc = mcIn;
	eventManager = eventManagerIn;
	connectionEvents = eventManager.registerEvent(ConnectionEvent.class);
	worldEvents = eventManager.registerEvent(SwitchWorldEvent.class);
}

@EventHandler
public void preInit(FMLPreInitializationEvent event) {
    Chung.US.init();
}

/*@EventHandler
public void init(FMLInitializationEvent event) {
	
}

@EventHandler
public void postInit(FMLPostInitializationEvent event) {
	
}*/

@SubscribeEvent
public static void onLoadWorld(WorldEvent.Load event) {
	World world = event.getWorld();
	if(world instanceof WorldClient) {
		if(mc.world == null) {
			connectionEvents.post(new ConnectionEvent((WorldClient) world));
			Tick.MAIN.add(() -> {
				Chung.US.connectModules();
				return -1;
			});
		} else {
			switchingWorld = true;
			worldEvents.post(new SwitchWorldEvent((WorldClient) world));
		}
	}
}

@SubscribeEvent
public static void onUnloadWorld(WorldEvent.Unload event) {
	if(event.getWorld() instanceof WorldClient) {
		if(switchingWorld) switchingWorld = false;
		else {
			Chung.US.disconnectModules();
			connectionEvents.post(new ConnectionEvent(null));
		}
	}
}

@SubscribeEvent
public static void onConnect(ClientConnectedToServerEvent event) {
	PacketListener.onConnect(event.getManager());
	
}

@SubscribeEvent
public static void onDisconnect(ClientDisconnectionFromServerEvent event) {
	PacketListener.onDisconnect();
}

}