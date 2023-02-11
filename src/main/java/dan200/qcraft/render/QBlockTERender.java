package dan200.qcraft.render;

import dan200.qcraft.QCraft;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class QBlockTERender extends TileEntitySpecialRenderer<QBlockTileEntity> {
    @Override
    public void render(QBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        //if(getWorld().getTileEntity(new BlockPos(x,y,z)) instanceof QBlockTileEntity) {

        IBlockState state = te.getCurrentState();
        if(state == null || state.getBlock() == Blocks.AIR) return;
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        Minecraft.getMinecraft().getBlockRendererDispatcher().renderBlock(Blocks.GLASS.getDefaultState(), new BlockPos(x, y, z), getWorld(), buffer);
        Tessellator.getInstance().draw();


        //}
    }
}
