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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemQBlock extends ItemBlock
{
    public ItemQBlock( Block block )
    {
        super( block );
        setMaxStackSize( 64 );
        setHasSubtypes( true );
        setTranslationKey( "qcraft:odb" );
        setCreativeTab( QCraft.getCreativeTab() );
    }

    public static ItemStack create( int subType, ItemStack[] types, int entanglementFrequency, int quantity )
    {
        ItemStack result = new ItemStack( QCraft.Blocks.qBlock, quantity, subType );
        setTypes( result, types );
        setEntanglementFrequency( result, entanglementFrequency );
        return result;
    }

    public static int getSubType( ItemStack stack )
    {
        return stack.getItemDamage();
    }

    public static void setTypes( ItemStack stack, ItemStack[] types )
    {
        // Ensure the nbt
        if( !stack.hasTagCompound() )
        {
            stack.setTagCompound( new NBTTagCompound() );
        }

        // Set the tags
        NBTTagCompound nbt = stack.getTagCompound();
        NBTTagCompound temp = new NBTTagCompound();
        for( int i = 0; i < types.length; ++i )
        {

            types[i].writeToNBT(temp);
            nbt.setTag( "s" + i,  temp);
        }
    }

    public static ItemStack[] getTypes( ItemStack stack )
    {
        // Get the tags
        ItemStack[] types = new ItemStack[ 6 ];
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            for( int i = 0; i < types.length; ++i )
            {
                if( nbt.hasKey( "s" + i ) )
                {
                    types[i] = new ItemStack((NBTTagCompound)nbt.getTag("s" + i));
                }
                else
                {
                    types[ i ] = ItemStack.EMPTY;
                }
            }
        }
        return types;
    }

    public static boolean compareTypes( ItemStack[] left, ItemStack[] right )
    {
        for( int i = 0; i < 6; ++i )
        {
            if( left[ i ].isItemEqual(right[ i ]) )
            {
                return false;
            }
        }
        return true;
    }

    public static void setEntanglementFrequency( ItemStack stack, int entanglementFrequency )
    {
        // Ensure the nbt
        if( !stack.hasTagCompound() )
        {
            stack.setTagCompound( new NBTTagCompound() );
        }

        // Set the tags
        NBTTagCompound nbt = stack.getTagCompound();
        if( entanglementFrequency < 0 )
        {
            // No frequency
            if( nbt.hasKey( "e" ) )
            {
                nbt.removeTag( "e" );
            }
            if( nbt.hasKey( "R" ) )
            {
                nbt.removeTag( "R" );
            }
        }
        else if( entanglementFrequency == 0 )
        {
            // Unknown frequency
            nbt.setInteger( "e", entanglementFrequency );
            nbt.setInteger( "R", TileEntityQBlock.s_random.nextInt( 0xffffff ) );
        }
        else
        {
            // Known frequency
            nbt.setInteger( "e", entanglementFrequency );
            if( nbt.hasKey( "R" ) )
            {
                nbt.removeTag( "R" );
            }
        }
    }

    public static int getEntanglementFrequency( ItemStack stack )
    {
        if( stack.hasTagCompound() )
        {
            NBTTagCompound nbt = stack.getTagCompound();
            if( nbt.hasKey( "e" ) )
            {
                return nbt.getInteger( "e" );
            }
        }
        return -1;
    }


    @Override
    public int getMetadata( int damage )
    {
        return damage;
    }

    @Override
    public void onCreated( ItemStack stack, World world, EntityPlayer player )
    {
        if( getEntanglementFrequency( stack ) == 0 && !world.isRemote )
        {
            setEntanglementFrequency( stack, TileEntityQBlock.getEntanglementRegistry( world ).getUnusedFrequency() );
            player.inventory.markDirty();
            if( player.openContainer != null )
            {
                player.openContainer.detectAndSendChanges();
            }
        }
    }

    @Override
    public boolean placeBlockAt( ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState blockState )
    {
        if( super.placeBlockAt( stack, player, world, pos, side, hitX, hitY, hitZ, blockState ) )
        {
            TileEntity entity = world.getTileEntity(pos);
            if(entity instanceof TileEntityQBlock)
            {
                TileEntityQBlock quantum = (TileEntityQBlock) entity;
                quantum.setTypes( getTypes( stack ) );
                quantum.setEntanglementFrequency( getEntanglementFrequency( stack ) );
            }
            return true;
        }
        return false;
    }

    @Override
    public String getTranslationKey( ItemStack stack )
    {
        boolean entangled = ( getEntanglementFrequency( stack ) >= 0 );
        switch( getSubType( stack ) )
        {
            case BlockQBlock.SubType.Standard:
            default:
            {
                return entangled ? "tile.qcraft:odb_entangled" : "tile.qcraft:odb";
            }
            case BlockQBlock.SubType.FiftyFifty:
            {
                return entangled ? "tile.qcraft:qb_entangled" : "tile.qcraft:qb";
            }
        }
    }

    public static String formatFrequency( int frequency )
    {
        String result = Integer.toHexString( frequency ).toUpperCase();
        if( result.length() == 1 )
        {
            return "0" + result;
        }
        return result;
    }

    @Override
    public void addInformation( ItemStack stack, World worldIn, List<String> list, ITooltipFlag par4 )
    {
        int frequency = getEntanglementFrequency( stack );
        if( frequency > 0 )
        {
            list.add( "Group: " + formatFrequency( frequency ) );
        }
        //else if( frequency == 0 )
        //{
        //	list.add( "Group: ???" );
        //}
    }
}
