package ftgumod.packet;

import ftgumod.packet.client.TechnologyMessage;
import ftgumod.packet.client.TechnologyMessage.TechnologyMessageHandler;
import ftgumod.packet.server.CopyTechMessage;
import ftgumod.packet.server.CopyTechMessage.CopyTechMessageHandler;
import ftgumod.packet.server.RequestTechMessage;
import ftgumod.packet.server.RequestTechMessage.RequestTechMessageHandler;
import ftgumod.packet.server.UnlockTechMessage;
import ftgumod.packet.server.UnlockTechMessage.UnlockTechMessageHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketDispatcher {

	private static byte packetId = 0;

	private static SimpleNetworkWrapper dispatcher;

	public static void registerPackets() {
		dispatcher = NetworkRegistry.INSTANCE.newSimpleChannel("ftgu");
		PacketDispatcher.registerMessage(RequestTechMessageHandler.class, RequestTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(UnlockTechMessageHandler.class, UnlockTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(CopyTechMessageHandler.class, CopyTechMessage.class, Side.SERVER);
		PacketDispatcher.registerMessage(TechnologyMessageHandler.class, TechnologyMessage.class, Side.CLIENT);
	}

	@SuppressWarnings({"unchecked"})
	private static void registerMessage(Class handlerClass, Class messageClass, Side side) {
		PacketDispatcher.dispatcher.registerMessage(handlerClass, messageClass, packetId++, side);
	}

	public static void sendTo(IMessage message, EntityPlayerMP player) {
		PacketDispatcher.dispatcher.sendTo(message, player);
	}

	public static void sendToServer(IMessage message) {
		PacketDispatcher.dispatcher.sendToServer(message);
	}
}
