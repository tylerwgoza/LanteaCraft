package lc.common.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;

import lc.common.LCLog;
import lc.common.util.WorldLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.common.network.ForgeMessage;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@ChannelHandler.Sharable
public class LanteaCraftPacketPipe extends MessageToMessageCodec<FMLProxyPacket, LCPacket> {
	private EnumMap<Side, FMLEmbeddedChannel> channels;
	private LinkedList<Class<? extends LCPacket>> packets = new LinkedList<Class<? extends LCPacket>>();

	public boolean registerPacket(Class<? extends LCPacket> clazz) {
		if (packets.size() > 256 || packets.contains(clazz))
			return false;
		packets.add(clazz);
		return true;
	}

	public void init(String channelName) {
		channels = NetworkRegistry.INSTANCE.newChannel(channelName, this);
		// TODO: Register packets
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, LCPacket msg, List<Object> out) throws Exception {

		Class<? extends LCPacket> clazz = msg.getClass();
		if (!packets.contains(msg.getClass()))
			LCLog.warn("Attempt to send unregistered packet class %s!", msg.getClass().getCanonicalName());

		ByteBuf buffer = Unpooled.buffer();
		byte discriminator = (byte) packets.indexOf(clazz);
		buffer.writeByte(discriminator);
		msg.encodeInto(ctx, buffer);
		FMLProxyPacket proxyPacket = new FMLProxyPacket(buffer.copy(), ctx.channel().attr(NetworkRegistry.FML_CHANNEL)
				.get());
		out.add(proxyPacket);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
		ByteBuf payload = msg.payload();
		byte discriminator = payload.readByte();
		Class<? extends LCPacket> clazz = packets.get(discriminator);
		if (clazz == null)
			LCLog.warn("Attempt to handlle unregistered packet class %s!", discriminator);

		LCPacket pkt = clazz.newInstance();
		pkt.decodeFrom(ctx, payload.slice());

		EntityPlayer player;
		switch (FMLCommonHandler.instance().getEffectiveSide()) {
		case CLIENT:
			player = getClientPlayer();
			break;
		case SERVER:
			INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
			player = ((NetHandlerPlayServer) netHandler).playerEntity;
			break;
		default:
			// We should never get here
			throw new IOException("Instance is not client or server. Cannot continue!");
		}
		// TODO: Fix packets. :<
		// LanteaCraft.getProxy().handlePacket(pkt, player);
	}

	@SideOnly(Side.CLIENT)
	private EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}

	public void sendToAll(LCPacket message) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	public void sendTo(LCPacket message, EntityPlayerMP player) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	public void sendForgeMessageTo(ForgeMessage message, EntityPlayerMP player) {
		FMLEmbeddedChannel channel = NetworkRegistry.INSTANCE.getChannel("FORGE", Side.SERVER);
		channel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
		channel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
		channel.writeAndFlush(message);
	}

	public void sendToAllAround(LCPacket message, WorldLocation location, double range) {
		TargetPoint point = new TargetPoint(location.dimension, location.x, location.y, location.z, range);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	public void sendToDimension(LCPacket message, int dimensionId) {
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.DIMENSION);
		channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
		channels.get(Side.SERVER).writeAndFlush(message);
	}

	public void sendToServer(LCPacket message) {
		channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
				.set(FMLOutboundHandler.OutboundTarget.TOSERVER);
		channels.get(Side.CLIENT).writeAndFlush(message);
	}
}
