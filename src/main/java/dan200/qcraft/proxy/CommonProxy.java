package dan200.qcraft.proxy;

import javax.annotation.Nullable;

import dan200.qcraft.proxy.IProxy;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CommonProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Override
    public void init(FMLInitializationEvent event) {
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
    }

    @Nullable
    @Override
    public EntityPlayer getClientPlayer() {
        throw new dan200.qcraft.proxy.IProxy.WrongSideException("Tried to get the client player on the dedicated server");
    }

    @Nullable
    @Override
    public World getClientWorld() {
        throw new dan200.qcraft.proxy.IProxy.WrongSideException("Tried to get the client world on the dedicated server");
    }

    @Override
    public IThreadListener getThreadListener(final MessageContext context) {
        if (context.side.isServer()) {
            return context.getServerHandler().player.server;
        } else {
            throw new dan200.qcraft.proxy.IProxy.WrongSideException(
                    "Tried to get the IThreadListener from a client-side MessageContext on the dedicated server");
        }
    }

    @Override
    public EntityPlayer getPlayer(final MessageContext context) {
        if (context.side.isServer()) {
            return context.getServerHandler().player;
        } else {
            throw new dan200.qcraft.proxy.IProxy.WrongSideException(
                    "Tried to get the player from a client-side MessageContext on the dedicated server");
        }
    }

    @Override
    public void renderAntiObserveGoggle(ScaledResolution scaledRes) {

    }

    @Override
    public void renderQuantumGoggle(ScaledResolution scaledRes) {

    }
}
