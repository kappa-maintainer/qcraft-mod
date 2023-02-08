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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockQuantumOre extends Block
{
    //private static IIcon s_icon;
    private boolean m_glowing;

    public BlockQuantumOre( boolean glowing )
    {
        super( Material.ROCK );
        setHardness( 3.0f );
        setResistance( 5.0f );
        setTranslationKey("block.qcraft.quantumore");

        m_glowing = glowing;
        if( m_glowing )
        {
            setLightLevel( 0.625f );
            setTickRandomly( true );
            setRegistryName( "qcraft:quantumoreglowing" );
        }
        else
        {
            setCreativeTab( QCraft.getCreativeTab() );
            setRegistryName( "qcraft:quantumore" );

        }
        this.setTickRandomly(true);
    }

    @Override
    public int tickRate( World par1World )
    {
        return 30;
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn)
    {
        this.activate(worldIn, pos);
        super.onEntityWalk(worldIn, pos, entityIn);
    }


    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        this.activate(worldIn, pos);
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
        if (this == QCraft.Blocks.quantumOreGlowing)
        {
            worldIn.setBlockState(pos, QCraft.Blocks.quantumOre.getDefaultState());
        }
    }

    private void activate(World worldIn, BlockPos pos)
    {
        this.spawnParticles(worldIn, pos);

        if (this == Blocks.REDSTONE_ORE)
        {
            worldIn.setBlockState(pos, Blocks.LIT_REDSTONE_ORE.getDefaultState());
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return QCraft.Items.quantumDust;
    }

    @Override
    public int quantityDroppedWithBonus( int par1, Random par2Random )
    {
        return this.quantityDropped( par2Random ) + par2Random.nextInt( par1 + 1 );
    }

    @Override
    public int quantityDropped( Random par1Random )
    {
        return 1 + par1Random.nextInt( 2 );
    }

    @Override
    public int getExpDrop(IBlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune)
    {
        if (this.getItemDropped(state, RANDOM, fortune) != Item.getItemFromBlock(this))
        {
            return 1 + RANDOM.nextInt(5);
        }
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
    {
        if (this.m_glowing)
        {
            this.spawnParticles(worldIn, pos);
        }
    }

    private void spawnParticles(World worldIn, BlockPos pos)
    {
        Random random = worldIn.rand;
        double d0 = 0.0625D;

        for (int i = 0; i < 6; ++i)
        {
            double d1 = (float)pos.getX() + random.nextFloat();
            double d2 = (float)pos.getY() + random.nextFloat();
            double d3 = (float)pos.getZ() + random.nextFloat();

            if (i == 0 && !worldIn.getBlockState(pos.up()).isOpaqueCube())
            {
                d2 = (double)pos.getY() + d0 + 1.0D;
            }

            if (i == 1 && !worldIn.getBlockState(pos.down()).isOpaqueCube())
            {
                d2 = (double)pos.getY() - d0;
            }

            if (i == 2 && !worldIn.getBlockState(pos.south()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() + d0 + 1.0D;
            }

            if (i == 3 && !worldIn.getBlockState(pos.north()).isOpaqueCube())
            {
                d3 = (double)pos.getZ() - d0;
            }

            if (i == 4 && !worldIn.getBlockState(pos.east()).isOpaqueCube())
            {
                d1 = (double)pos.getX() + d0 + 1.0D;
            }

            if (i == 5 && !worldIn.getBlockState(pos.west()).isOpaqueCube())
            {
                d1 = (double)pos.getX() - d0;
            }

            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1))
            {
                QCraft.spawnQuantumDustFX(worldIn, new BlockPos(d1, d2, d3));
            }
        }
    }

    protected ItemStack getSilkTouchDrop(IBlockState state)
    {
        return new ItemStack(QCraft.Blocks.quantumOre);
    }
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(QCraft.Blocks.quantumOre), 1, this.damageDropped(state));
    }
/*
    @Override
    protected ItemStack createStackedBlock( int i )
    {
        return new ItemStack( QCraft.Blocks.quantumOre );
    }

    @Override
    public void registerBlockIcons( IIconRegister iconRegister )
    {
        s_icon = iconRegister.registerIcon( "qcraft:ore" );
    }

 */

}
