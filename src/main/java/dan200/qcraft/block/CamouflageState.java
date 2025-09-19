package dan200.qcraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import dan200.qcraft.QCraftBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class CamouflageState extends BlockStateContainer.StateImplementation {
    
    private final IBlockState camouflageTarget;
    
    public CamouflageState(Block block, IBlockState target) {
        super(block, ImmutableMap.of());
        if (target != null) {
            camouflageTarget = target;
        }
        else
        {
            camouflageTarget = QCraftBlocks.blockSwirl.getDefaultState();
        }
    }
    
    protected CamouflageState(Block block, ImmutableMap<IProperty<?>, Comparable<?>> property) {
        super(block, property);
        camouflageTarget = QCraftBlocks.blockSwirl.getDefaultState();
    }

    protected CamouflageState(Block blockIn, ImmutableMap<IProperty<?>, Comparable<?>> propertiesIn, ImmutableTable<IProperty<?>, Comparable<?>, IBlockState> propertyValueTable) {
        super(blockIn, propertiesIn, propertyValueTable);
        camouflageTarget = QCraftBlocks.blockSwirl.getDefaultState();
    }
    
    public IBlockState getCamouflageTarget() {
        return camouflageTarget;
    }

    public Block getBlock()
    {
        return super.getBlock();
    }

    public Material getMaterial()
    {
        return this.camouflageTarget.getMaterial();
    }

    public boolean isFullBlock()
    {
        return this.camouflageTarget.isFullBlock();
    }

    public boolean canEntitySpawn(Entity entityIn)
    {
        return this.camouflageTarget.canEntitySpawn(entityIn);
    }

    public int getLightOpacity()
    {
        return this.camouflageTarget.getLightOpacity();
    }

    public int getLightValue()
    {
        return this.camouflageTarget.getLightValue();
    }

    @SideOnly(Side.CLIENT)
    public boolean isTranslucent()
    {
        return this.camouflageTarget.isTranslucent();
    }

    public boolean useNeighborBrightness()
    {
        return this.camouflageTarget.useNeighborBrightness();
    }

    public MapColor getMapColor(IBlockAccess world, BlockPos pos)
    {
        return this.camouflageTarget.getMapColor(world, pos);
    }

    public IBlockState withRotation(Rotation rot)
    {
        return this.camouflageTarget.withRotation(rot);
    }

    public IBlockState withMirror(Mirror mirrorIn)
    {
        return this.camouflageTarget.withMirror(mirrorIn);
    }

    public boolean isFullCube()
    {
        return this.camouflageTarget.isFullCube();
    }

    @SideOnly(Side.CLIENT)
    public boolean hasCustomBreakingProgress()
    {
        return this.camouflageTarget.hasCustomBreakingProgress();
    }

    public EnumBlockRenderType getRenderType()
    {
        return this.camouflageTarget.getRenderType();
    }

    @SideOnly(Side.CLIENT)
    public int getPackedLightmapCoords(IBlockAccess source, BlockPos pos)
    {
        return this.camouflageTarget.getPackedLightmapCoords(source, pos);
    }

    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue()
    {
        return this.camouflageTarget.getAmbientOcclusionLightValue();
    }

    public boolean isBlockNormalCube()
    {
        return this.camouflageTarget.isBlockNormalCube();
    }

    public boolean isNormalCube()
    {
        return this.camouflageTarget.isNormalCube();
    }

    public boolean canProvidePower()
    {
        return this.camouflageTarget.canProvidePower();
    }

    public int getWeakPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return this.camouflageTarget.getWeakPower(blockAccess, pos, side);
    }

    public boolean hasComparatorInputOverride()
    {
        return this.camouflageTarget.hasComparatorInputOverride();
    }

    public int getComparatorInputOverride(World worldIn, BlockPos pos)
    {
        return this.camouflageTarget.getComparatorInputOverride(worldIn, pos);
    }

    public float getBlockHardness(World worldIn, BlockPos pos)
    {
        return this.camouflageTarget.getBlockHardness(worldIn, pos);
    }

    public float getPlayerRelativeBlockHardness(EntityPlayer player, World worldIn, BlockPos pos)
    {
        return this.camouflageTarget.getPlayerRelativeBlockHardness(player, worldIn, pos);
    }

    public int getStrongPower(IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return this.camouflageTarget.getStrongPower(blockAccess, pos, side);
    }

    public EnumPushReaction getPushReaction()
    {
        return this.camouflageTarget.getPushReaction();
    }

    public IBlockState getActualState(IBlockAccess blockAccess, BlockPos pos)
    {
        //return QCraftBlocks.blockTransparent.getDefaultState();
        return this.camouflageTarget.getActualState(blockAccess, pos);
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getSelectedBoundingBox(World worldIn, BlockPos pos)
    {
        return this.camouflageTarget.getSelectedBoundingBox(worldIn, pos);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing facing)
    {
        return this.camouflageTarget.shouldSideBeRendered(blockAccess, pos, facing);
    }

    public boolean isOpaqueCube()
    {
        return this.camouflageTarget.isOpaqueCube();
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess worldIn, BlockPos pos)
    {
        return this.camouflageTarget.getCollisionBoundingBox(worldIn, pos);
    }

    public void addCollisionBoxToList(World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185908_6_)
    {
        this.camouflageTarget.addCollisionBoxToList(worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
    }

    public AxisAlignedBB getBoundingBox(IBlockAccess blockAccess, BlockPos pos)
    {
        return this.camouflageTarget.getBoundingBox(blockAccess, pos);
    }

    public RayTraceResult collisionRayTrace(World worldIn, BlockPos pos, Vec3d start, Vec3d end)
    {
        return this.camouflageTarget.collisionRayTrace(worldIn, pos, start, end);
    }

    public boolean isTopSolid()
    {
        return this.camouflageTarget.isTopSolid();
    }

    public Vec3d getOffset(IBlockAccess access, BlockPos pos)
    {
        return this.camouflageTarget.getOffset(access, pos);
    }

    public boolean onBlockEventReceived(World worldIn, BlockPos pos, int id, int param)
    {
        return this.camouflageTarget.onBlockEventReceived(worldIn, pos, id, param);
    }

    public void neighborChanged(World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        this.camouflageTarget.neighborChanged(worldIn, pos, blockIn, fromPos);
    }

    public boolean causesSuffocation()
    {
        return this.camouflageTarget.causesSuffocation();
    }

    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, BlockPos pos, EnumFacing facing)
    {
        return this.camouflageTarget.getBlockFaceShape(worldIn, pos, facing);
    }

    @Override
    public int getLightOpacity(IBlockAccess world, BlockPos pos)
    {
        return this.camouflageTarget.getLightOpacity(world, pos);
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos)
    {
        return this.camouflageTarget.getLightValue(world, pos);
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return this.camouflageTarget.isSideSolid(world, pos, side);
    }

    @Override
    public boolean doesSideBlockChestOpening(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return this.camouflageTarget.doesSideBlockChestOpening(world, pos, side);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockAccess world, BlockPos pos, EnumFacing side)
    {;
        return this.camouflageTarget.doesSideBlockRendering(world, pos, side);
    }
}
