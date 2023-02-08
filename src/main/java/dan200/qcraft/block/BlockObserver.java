package dan200.qcraft.block;

import dan200.qcraft.QCraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockObserver extends BlockRedstoneDiode implements ITileEntityProvider {
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockObserver(boolean powered) {
        super(powered);
        setTranslationKey("qcraft.automatic_observer");
        setRegistryName("qcraft:automatic_observer");
        setCreativeTab(QCraft.QCRAT_TAB);
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(POWERED, false));
    }

    @Override
    protected int getDelay(IBlockState state) {
        return 0;
    }

    @Override
    protected IBlockState getPoweredState(IBlockState unpoweredState) {
        return unpoweredState.withProperty(POWERED, true);
    }

    @Override
    protected IBlockState getUnpoweredState(IBlockState poweredState) {
        return poweredState.withProperty(POWERED, false);
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        return side == state.getValue(FACING).getOpposite();
    }
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING, POWERED);
    }
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return 0;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite()).withProperty(POWERED, false);
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
    {
        if (!worldIn.isRemote)
        {
            if (state.getValue(POWERED) && !worldIn.isBlockPowered(pos))
            {
                worldIn.setBlockState(pos, state.withProperty(POWERED, false), 2);
            }
            else if (!state.getValue(POWERED) && worldIn.isBlockPowered(pos) && worldIn.isSidePowered(pos.offset(state.getValue(FACING)), state.getValue(FACING)))
            {
                worldIn.setBlockState(pos, state.withProperty(POWERED, true), 2);
            }
        }
    }
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!worldIn.isRemote)
        {
            if (!worldIn.isBlockPowered(pos))
            {
                worldIn.scheduleUpdate(pos, this, 4);
                worldIn.setBlockState(pos, state.withProperty(POWERED, false), 2);
            }
            else if (worldIn.isBlockPowered(pos) && worldIn.isSidePowered(pos.offset(state.getValue(FACING)), state.getValue(FACING)))
            {
                worldIn.setBlockState(pos, state.withProperty(POWERED, true), 2);
            }
        }
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (!worldIn.isRemote)
        {
            if (state.getValue(POWERED) && !worldIn.isBlockPowered(pos))
            {
                worldIn.setBlockState(pos, state.withProperty(POWERED, false), 2);
            }
        }
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int i = 0;
        i = i | state.getValue(FACING).getHorizontalIndex();

        if (state.getValue(POWERED))
        {
            i |= 0b100;
        }
        return i;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta)).withProperty(POWERED, (meta & 0b100) > 0);
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
    {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }
    @Override
    public IBlockState withRotation(IBlockState state, Rotation rot)
    {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }
}
