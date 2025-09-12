package dan200.qcraft.render;

import codechicken.lib.render.block.ICCBlockRenderer;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.block.ICamouflageableBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class QBlockCCBlockRenderer implements ICCBlockRenderer {
    private static BlockRendererDispatcher dispatcher;
    @Override
    public void handleRenderBlockDamage(IBlockAccess iBlockAccess, BlockPos blockPos, IBlockState iBlockState, TextureAtlasSprite textureAtlasSprite, BufferBuilder bufferBuilder) {
        if (!(iBlockAccess.getTileEntity(blockPos) instanceof ICamouflageableBlock iCamouflageableBlock)) {
            getDispatcher().renderBlockDamage(iBlockState, blockPos, textureAtlasSprite, iBlockAccess);
        } else {
            IBlockState camoState = iCamouflageableBlock.getCamouflageBlockState();
            getDispatcher().renderBlockDamage(camoState, blockPos, textureAtlasSprite, iBlockAccess);
        }
    }

    @Override
    public boolean renderBlock(IBlockAccess iBlockAccess, BlockPos blockPos, IBlockState iBlockState, BufferBuilder bufferBuilder) {
        if (!(iBlockAccess.getTileEntity(blockPos) instanceof ICamouflageableBlock iCamouflageableBlock)) {
            getDispatcher().renderBlock(QCraftBlocks.blockSwirl.getDefaultState(), blockPos, iBlockAccess, bufferBuilder);
        } else {
            IBlockState camoState = iCamouflageableBlock.getCamouflageBlockState();
            getDispatcher().renderBlock(camoState, blockPos, iBlockAccess, bufferBuilder);
        }
        return true;
    }
    
    private BlockRendererDispatcher getDispatcher() {
        if (dispatcher == null) {
            dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
        }
        return dispatcher;
    }

    @Override
    public void renderBrightness(IBlockState iBlockState, float v) {

    }

    @Override
    public void registerTextures(TextureMap textureMap) {

    }
}
