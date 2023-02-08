/*
Copyright 2014 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package dan200.qcraft.shared;

import dan200.QCraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockQuantumLogic extends BlockHorizontal
{
    public int blockRenderID;
    protected static final AxisAlignedBB REDSTONE_DIODE_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);
    public static final PropertyBool POWERED = PropertyBool.create("powered");
    //private IIcon[] m_icons;

    public int getSubType( int metadata )
    {
        return ( ( metadata >> 2 ) & 0x3 );
    }

    protected BlockQuantumLogic()
    {
        super( Material.CIRCUITS );
        setHardness( 0.0F );
        setSoundType( SoundType.WOOD );
        setRegistryName( "qcraft:automatic_observer" );
        setTranslationKey("block.qcraft.automatic_observer");
        setCreativeTab( QCraft.getCreativeTab() );
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return REDSTONE_DIODE_AABB;
    }


    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        return (downState.isTopSolid() || downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID) ? super.canPlaceBlockAt(worldIn, pos) : false;
    }

    //@Override
    public boolean canBlockStay(World worldIn, BlockPos pos)
    {
        IBlockState downState = worldIn.getBlockState(pos.down());
        return downState.isTopSolid() || downState.getBlockFaceShape(worldIn, pos.down(), EnumFacing.UP) == BlockFaceShape.SOLID;
    }

    /*
    @Override
    public IIcon getIcon( IBlockAccess world, int i, int j, int k, int side )
    {
        int metadata = world.getBlockMetadata( i, j, k );
        int damage = getSubType( metadata );
        return getIcon( side, damage );
    }

    @Override
    public IIcon getIcon( int side, int damage )
    {
        int subType = damage;
        if( side == 1 && damage >= 0 && damage < m_icons.length )
        {
            return m_icons[ damage ];
        }
        return Blocks.double_stone_slab.getBlockTextureFromSide( side );
    }*/

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return 0;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return 0;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        return ( side == ((EnumFacing) state.getProperties().get(FACING)).getOpposite() );
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        if (!this.canBlockStay(worldIn, pos))
        {
            this.dropBlockAsItem(worldIn, pos, state, 0);
            worldIn.setBlockToAir(pos);

            for (EnumFacing enumfacing : EnumFacing.values())
            {
                worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this, false);
            }
        }
    }



    private void updateOutput( World world, BlockPos pos )
    {
        if( world.isRemote )
        {
            return;
        }

        // Redetermine subtype
        IBlockState blockState = world.getBlockState( pos );
        EnumFacing facing = (EnumFacing)blockState.getProperties().get(FACING);
        boolean powered = (boolean) blockState.getProperties().get(POWERED);
        boolean newPowered = evaluateInput( world,pos );
        if( powered != newPowered )
        {
            // Set new subtype
            setDirectionAndSubType( world, pos, facing, powered );
            powered = newPowered;

            // Notify
            // Probably needn't in 1.12
            //world.markBlockForUpdate( x, y, z );
            //world.notifyBlocksOfNeighborChange( x, y, z, this );
        }

        // Observe

        observe( world, pos, facing.getOpposite(), powered );
    }

    private void setDirectionAndSubType( World world, BlockPos pos, EnumFacing facing, boolean powered )
    {
        world.setBlockState(pos, this.getDefaultState().withProperty(POWERED, powered).withProperty(FACING, facing));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        EnumFacing direction = EnumFacing.byIndex(( ( MathHelper.floor( (double) ( placer.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 3 ) + 2 ) % 4);
        //int subType = stack.getItemDamage();
        setDirectionAndSubType( worldIn, pos, direction, false );
    }

    @Override
    public void onBlockAdded( World worldIn, BlockPos pos, IBlockState state)
    {
        updateOutput( worldIn, pos);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        super.onBlockHarvested(worldIn, pos, state, player);
    }

    /*
    @Override
    public void registerBlockIcons( IIconRegister iconRegister )
    {
        m_icons = new IIcon[ SubType.Count ];
        m_icons[ SubType.ObserverOff ] = iconRegister.registerIcon( "qcraft:automatic_observer" );
        m_icons[ SubType.ObserverOn ] = iconRegister.registerIcon( "qcraft:automatic_observer_on" );
    }

     */

    private boolean evaluateInput( World world, BlockPos pos )
    {
        IBlockState blockState1 = world.getBlockState( pos );
        return getRedstoneSignal( world, pos, (EnumFacing)blockState1.getProperties().get(FACING) );
    }

    private boolean getRedstoneSignal( World world, BlockPos pos, EnumFacing facing )
    {

        return world.isBlockPowered(pos.offset(facing,2));
    }

    private void observe( World world, BlockPos pos, EnumFacing facing, boolean observe )
    {

        Block block = world.getBlockState( pos.offset(facing) ).getBlock();
        if(block instanceof IQuantumObservable)
        {
            EnumFacing side = facing.getOpposite();
            IQuantumObservable observable = (IQuantumObservable) block;
            if( observable.isObserved( world, pos, side ) != observe )
            {
                if( observe )
                {
                    observable.observe( world, pos, side );
                }
                else
                {
                    observable.reset( world, pos, side );
                }
            }
        }
    }
}
