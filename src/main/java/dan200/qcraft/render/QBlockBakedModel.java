package dan200.qcraft.render;

import dan200.qcraft.QCraft;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.block.CamouflageBlockProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;

public class QBlockBakedModel implements IBakedModel {
    private static final LazyInitializer<BlockModelShapes> shapes = new LazyInitializer<>() {
        @Override
        protected BlockModelShapes initialize() {
            return  Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        }
    };
    
    public QBlockBakedModel(IBakedModel model) {
        currentModel = model;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState iBlockState, @Nullable EnumFacing enumFacing, long l) {
        currentModel = handleBlockState(iBlockState);
        return currentModel.getQuads(iBlockState, enumFacing, l);
    }

    public static final ModelResourceLocation blockstate = new ModelResourceLocation("qcraft:qblock#normal");
    public static final ModelResourceLocation variant = new ModelResourceLocation(QCraftBlocks.blockQBlock.getRegistryName(), null);

    private IBakedModel currentModel;

    private IBakedModel handleBlockState(@Nullable IBlockState iBlockState) {
        IBakedModel emptyModel = null;

        if(iBlockState instanceof IExtendedBlockState iExtendedBlockState) {
            IBlockState currentState = iExtendedBlockState.getValue(CamouflageBlockProperty.CURRENT_CAMOU);

            try {
                if(currentState != null) {
                    emptyModel = shapes.get().getModelForState(currentState);
                } else {
                    QCraft.LOGGER.info("Can't get sub state, block {}", iBlockState);
                    emptyModel = shapes.get().getModelForState(QCraftBlocks.blockSwirl.getDefaultState());
                }
            } catch (ConcurrentException ignored) {
            }
        }
        return emptyModel;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return currentModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return currentModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return currentModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return currentModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return currentModel.getOverrides();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() { 
        return currentModel.getItemCameraTransforms(); 
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        Matrix4f matrix4f = currentModel.handlePerspective(cameraTransformType).getRight();
        return Pair.of(this, matrix4f);
    }
}
