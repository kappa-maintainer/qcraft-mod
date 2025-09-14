package dan200.qcraft.block;

import dan200.qcraft.QCraft;
import dan200.qcraft.QCraftBlocks;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockQBlock extends BlockFalling {
    public BlockQBlock() {
        super(Material.GLASS);
        setRegistryName("qcraft:qblock");
        setTranslationKey("qcraft.qblock");
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
    
    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return QCraft.getQblockRenderType();
    }

    @Override
    public Material getMaterial(IBlockState state)
    {
        IBlockState subState = ((IExtendedBlockState) state).getValue(CamouflageBlockProperty.CURRENT_CAMOU);
        if (subState != null) {
            return subState.getMaterial();
        }
        else {
            return Material.ROCK;
        }

    }
    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return getCurrentSubState(state).isOpaqueCube();
    }
    
    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(CamouflageBlockProperty.CURRENT_CAMOU).build();
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (!state.getBlock().equals(QCraftBlocks.blockQBlock)) return state;
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;

        TileEntity te = world.getTileEntity(pos);
        if(te instanceof QBlockTileEntity) {
            return extendedBlockState.withProperty(CamouflageBlockProperty.CURRENT_CAMOU, ((QBlockTileEntity) te).getCamouflageBlockState());
        }
        return extendedBlockState;
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        IBlockState sub = getCurrentSubState(state);
        if (sub.getBlock() instanceof BlockFalling) {
            super.updateTick(worldIn, pos, sub, rand);
        }
    }
    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new QBlockTileEntity();
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return getCurrentSubState(state).getSelectedBoundingBox(world, pos);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getCurrentSubState(state).getBoundingBox(world, pos);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getCurrentSubState(state).getCollisionBoundingBox(world, pos);
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        //return BlockRenderLayer.CUTOUT == layer;
        boolean can = getCurrentSubState(state).getBlock().getRenderLayer() == layer;
        QCraft.LOGGER.info("{} {}", state, can);
        return can;
    }

    @Override
    public boolean canCollideCheck(IBlockState state, boolean hitIfLiquid)
    {
        return getCurrentSubState(state).getBlock().canCollideCheck(getCurrentSubState(state), hitIfLiquid);
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        //return false;
        return getCurrentSubState(state).isFullCube();
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return getCurrentSubState(state).getBlockFaceShape(worldIn, pos, face);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face)
    {
        //return true;
        return getCurrentSubState(state).doesSideBlockRendering(world, pos, face);
    }

    @Override
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return getCurrentSubState(state).getBlock().isLadder(getCurrentSubState(state), world, pos, entity); 
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        return getCurrentSubState(state).getLightValue(world, pos);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state)
    {
        return getCurrentSubState(state).isBlockNormalCube();
    }

    @Override
    public boolean isNormalCube(IBlockState state)
    {
        return getCurrentSubState(state).isNormalCube();
    }

    @Override
    public boolean causesSuffocation(IBlockState state)
    {
        return getCurrentSubState(state).causesSuffocation();
    }

    @Override
    public float getAmbientOcclusionLightValue(IBlockState state)
    {
        return getCurrentSubState(state).getAmbientOcclusionLightValue();
    }

    @Override
    public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return getCurrentSubState(state).getWeakPower(world, pos, facing);
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return getCurrentSubState(state).canProvidePower();
    }

    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        getCurrentSubState(state).getBlock().onEntityCollision(world, pos, getCurrentSubState(state), entity);
    }

    @Override
    public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing facing)
    {
        return getCurrentSubState(state).getStrongPower(world, pos, facing);
    }
    
    public static IBlockState getCurrentSubState(IBlockState state) {
        IBlockState property = ((IExtendedBlockState) state).getValue(CamouflageBlockProperty.CURRENT_CAMOU);
        if (property == null) {
            return QCraftBlocks.blockSwirl.getDefaultState();
        }
        return property;
    }

}
