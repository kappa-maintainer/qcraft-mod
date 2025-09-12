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
import java.util.Optional;
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
        return EnumBlockRenderType.MODEL;
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
        IProperty<?>[] listedProperties = new IProperty[0]; // no listed properties
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty[] { CamouflageBlockProperty.CURRENT_CAMOU};
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
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
        return getCurrentSubState(state).getBlock().getRenderLayer() == layer;
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
    
    private IBlockState getCurrentSubState(IBlockState state) {
        IBlockState property = ((IExtendedBlockState) state).getValue(CamouflageBlockProperty.CURRENT_CAMOU);
        if (property == null) {
            return QCraftBlocks.blockSwirl.getDefaultState();
        }
        return property;
    }

}
