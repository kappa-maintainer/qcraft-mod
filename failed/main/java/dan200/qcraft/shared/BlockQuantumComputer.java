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
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockQuantumComputer extends BlockDirectional
        implements ITileEntityProvider
{
    /*private static class Icons
    {
        public static IIcon Front;
        public static IIcon Top;
        public static IIcon Side;
    }*/

    public BlockQuantumComputer()
    {
        super( Material.IRON );
        setCreativeTab( QCraft.getCreativeTab() );
        setHardness( 5.0f );
        setResistance( 10.0f );
        setSoundType(SoundType.METAL);
        setRegistryName( "qcraft:quantumcomputer" );
        setTranslationKey("item.qcraft.quantumcomputer");
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }


    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        List<ItemStack> blocks = new ArrayList<ItemStack>();
        TileEntity entity = world.getTileEntity( pos );
        if(entity instanceof TileEntityQuantumComputer)
        {
            // Get the computer back
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            ItemStack stack = ItemQuantumComputer.create( computer.getEntanglementFrequency(), 1 );
            ItemQuantumComputer.setStoredData( stack, computer.getStoredData() );
            blocks.add( stack );
        }
        return blocks;
    }

    protected boolean shouldDropItemsInCreative( World world, BlockPos pos )
    {
        return false;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if( world.isRemote )
        {
            return false;
        }

        if( !player.capabilities.isCreativeMode || shouldDropItemsInCreative( world, pos) )
        {
            // Regular and silk touch block (identical)

            dropBlockAsItem(world, pos, state, 0);

        }

        return super.removedByPlayer(state, world,  pos, player, willHarvest);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        IBlockState state1 = world.getBlockState( pos);
        List<ItemStack> items = getDrops( world,pos, state1, 0 );
        if( items.size() > 0 )
        {
            return items.get( 0 );
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if( player.isSneaking() )
        {
            return false;
        }

        if( !world.isRemote )
        {
            // Show GUI
            TileEntity entity = world.getTileEntity( pos);
            if(entity instanceof TileEntityQuantumComputer)
            {
                TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
                QCraft.openQuantumComputerGUI( player, computer );
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof TileEntityQuantumComputer)
        {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.onDestroy();
        }
        super.breakBlock( world, pos, state );
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        int direction = ( ( MathHelper.floor( (double) ( placer.rotationYaw * 4.0F / 360.0F ) + 0.5D ) & 0x3 ) + 2 ) % 4;
        //int metadata = ( direction & 0x3 );
        world.setBlockState( pos, getDefaultState().withProperty(FACING, EnumFacing.byIndex(direction)));
    }

    @Override
    public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
    {
        super.onNeighborChange( world, pos, neighbor );

        TileEntity entity = world.getTileEntity( pos);
        if(entity instanceof TileEntityQuantumComputer)
        {
            TileEntityQuantumComputer computer = (TileEntityQuantumComputer) entity;
            computer.setRedstonePowered( ((World)world).isBlockPowered(pos) );
        }
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        return true;
    }
/*
    @Override
    public void registerBlockIcons( IIconRegister iconRegister )
    {
        Icons.Front = iconRegister.registerIcon( "qcraft:computer" );
        Icons.Top = iconRegister.registerIcon( "qcraft:computer_top" );
        Icons.Side = iconRegister.registerIcon( "qcraft:computer_side" );
    }

    @Override
    public IIcon getIcon( IBlockAccess world, int i, int j, int k, int side )
    {
        if( side == 0 || side == 1 )
        {
            return Icons.Top;
        }

        int metadata = world.getBlockMetadata( i, j, k );
        int direction = Direction.directionToFacing[ getDirection( metadata ) ];
        if( side == direction )
        {
            return Icons.Front;
        }

        return Icons.Side;
    }

    @Override
    public IIcon getIcon( int side, int damage )
    {
        switch( side )
        {
            case 0:
            case 1:
            {
                return Icons.Top;
            }
            case 4:
            {
                return Icons.Front;
            }
            default:
            {
                return Icons.Side;
            }
        }
    }*/

    @Override
    public TileEntity createNewTileEntity( World world, int metadata )
    {
        return new TileEntityQuantumComputer();
    }

    @Override
    public TileEntity createTileEntity( World world, IBlockState state )
    {
        return createNewTileEntity( world, 0 );
    }
}
