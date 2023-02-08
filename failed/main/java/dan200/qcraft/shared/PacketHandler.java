/**
 * This file is part of qCraft - http://www.qcraft.org Copyright Daniel Ratcliffe and
 * TeacherGaming LLC, 2013. Do not distribute without permission. Send enquiries
 * to dratcliffe@gmail.com
 */
package dan200.qcraft.shared;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import dan200.QCraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetHandlerPlayServer;

public class PacketHandler
{
    @SubscribeEvent
    public void onClientPacket( FMLNetworkEvent.ClientCustomPacketEvent event )
    {
        try
        {
            QCraftPacket packet = new QCraftPacket();
            packet.fromBytes( event.packet.payload() );
            QCraft.handleClientPacket( packet );
        }
        catch( Exception e )
        {
            // Something failed, ignore it
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onServerPacket( FMLNetworkEvent.ServerCustomPacketEvent event )
    {
        try
        {
            QCraftPacket packet = new QCraftPacket();
            packet.fromBytes( event.packet.payload() );
            QCraft.handleServerPacket( packet, ((NetHandlerPlayServer)event.handler).playerEntity );
        }
        catch( Exception e )
        {
            // Something failed, ignore it
            e.printStackTrace();
        }
    }
}
