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
import net.minecraft.block.*;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;


import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BlockQBlock extends BlockSand
     implements ITileEntityProvider, IQuantumObservable
{
    public int blockRenderID;
    private static ResourceLocation s_transparentIcon;
    private static ResourceLocation s_swirlIcon;
    private static ResourceLocation s_fuzzIcon;
    private static ItemStack[] s_impostorBlocks;

    private EnumFacing facing = EnumFacing.EAST;
    private float hitX = 0;
    private float hitY = 0;
    private float hitZ = 0;
    private int meta = 0;
    private EntityLivingBase placer;
    private EnumHand hand = EnumHand.MAIN_HAND;

    private ItemStack qDrop = ItemStack.EMPTY;

    public enum Appearance
    {
        Block,
        Fuzz,
        Swirl
    }
    /*
    public static ItemStack[] getImpostorBlockList()
    {
        if( s_impostorBlocks == null )
        {
            s_impostorBlocks = new ItemStack[]{
                    null,
                    new ItemStack( Blocks.STONE, 1, 0 ),
                    new ItemStack( Blocks.GRASS, 1, 0 ),
                    new ItemStack( Blocks.DIRT, 1, 0 ),
                    new ItemStack( Blocks.bedrock, 1, 0 ),
                    new ItemStack( Blocks.sand, 1, 0 ),
                    new ItemStack( Blocks.gravel, 1, 0 ),
                    new ItemStack( Blocks.gold_ore, 1, 0 ),
                    new ItemStack( Blocks.iron_ore, 1, 0 ),
                    new ItemStack( Blocks.coal_ore, 1, 0 ),
                    new ItemStack( Blocks.log, 1, 0 ),
                    new ItemStack( Blocks.lapis_ore, 1, 0 ),
                    new ItemStack( Blocks.sandstone, 1, 0 ),
                    new ItemStack( Blocks.diamond_ore, 1, 0 ),
                    new ItemStack( Blocks.redstone_ore, 1, 0 ),
                    new ItemStack( Blocks.emerald_ore, 1, 0 ),
                    new ItemStack( Blocks.ice, 1, 0 ),
                    new ItemStack( Blocks.clay, 1, 0 ),
                    new ItemStack( Blocks.pumpkin, 1, 0 ),
                    new ItemStack( Blocks.melon_block, 1, 0 ),
                    new ItemStack( Blocks.mycelium, 1, 0 ),
                    new ItemStack( Blocks.obsidian, 1, 0 ), // 21
                    new ItemStack( Blocks.cobblestone, 1, 0 ),
                    new ItemStack( Blocks.planks, 1, 0 ),
                    new ItemStack( Blocks.bookshelf, 1, 0 ),
                    new ItemStack( Blocks.mossy_cobblestone, 1, 0 ),
                    new ItemStack( Blocks.netherrack, 1, 0 ),
                    new ItemStack( Blocks.soul_sand, 1, 0 ),
                    new ItemStack( Blocks.glowstone, 1, 0 ),
                    new ItemStack( Blocks.end_stone, 1, 0 ),
                    new ItemStack( Blocks.iron_block, 1, 0 ),
                    new ItemStack( Blocks.gold_block, 1, 0 ), // 31
                    new ItemStack( Blocks.diamond_block, 1, 0 ),
                    new ItemStack( Blocks.lapis_block, 1, 0 ),
                    new ItemStack( Blocks.wool, 1, 0 ),
                    new ItemStack( Blocks.glass, 1, 0 ),
                    new ItemStack( Blocks.wool, 1, 1 ),
                    new ItemStack( Blocks.wool, 1, 2 ),
                    new ItemStack( Blocks.wool, 1, 3 ),
                    new ItemStack( Blocks.wool, 1, 4 ),
                    new ItemStack( Blocks.wool, 1, 5 ),
                    new ItemStack( Blocks.wool, 1, 6 ),
                    new ItemStack( Blocks.wool, 1, 7 ),
                    new ItemStack( Blocks.wool, 1, 8 ),
                    new ItemStack( Blocks.wool, 1, 9 ),
                    new ItemStack( Blocks.wool, 1, 10 ),
                    new ItemStack( Blocks.wool, 1, 11 ),
                    new ItemStack( Blocks.wool, 1, 12 ),
                    new ItemStack( Blocks.wool, 1, 13 ),
                    new ItemStack( Blocks.wool, 1, 14 ),
                    new ItemStack( Blocks.wool, 1, 15 ),
                    new ItemStack( Blocks.log, 1, 1 ),
                    new ItemStack( Blocks.log, 1, 2 ),
                    new ItemStack( Blocks.log, 1, 3 ),
                    new ItemStack( Blocks.planks, 1, 1 ),
                    new ItemStack( Blocks.planks, 1, 2 ),
                    new ItemStack( Blocks.planks, 1, 3 ),
                    new ItemStack( Blocks.sandstone, 1, 1 ),
                    new ItemStack( Blocks.sandstone, 1, 2 ),
                    new ItemStack( Blocks.stonebrick, 1, 0 ),
                    new ItemStack( Blocks.stonebrick, 1, 1 ),
                    new ItemStack( Blocks.stonebrick, 1, 2 ),
                    new ItemStack( Blocks.stonebrick, 1, 3 ),
                    new ItemStack( Blocks.nether_brick, 1, 0 ),
                    new ItemStack( Blocks.brick_block, 1, 0 ),
                    new ItemStack( Blocks.redstone_block, 1, 0 ),
                    new ItemStack( Blocks.quartz_ore, 1, 0 ),
                    new ItemStack( Blocks.quartz_block, 1, 0 ),
                    new ItemStack( Blocks.quartz_block, 1, 1 ),
                    new ItemStack( Blocks.quartz_block, 1, 2 ),

                    // New in 1.6.4!
                    new ItemStack( Blocks.stained_hardened_clay, 1, 0 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 1 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 2 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 3 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 4 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 5 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 6 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 7 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 8 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 9 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 10 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 11 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 12 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 13 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 14 ),
                    new ItemStack( Blocks.stained_hardened_clay, 1, 15 ),
                    new ItemStack( Blocks.hay_block, 1, 0 ),
                    new ItemStack( Blocks.hardened_clay, 1, 0 ),
                    new ItemStack( Blocks.coal_block, 1, 0 ),

                    // New in 1.7.2!
                    new ItemStack( Blocks.log2, 1, 0 ),
                    new ItemStack( Blocks.log2, 1, 1 ),
                    new ItemStack( Blocks.dirt, 1, 2 ), // Podzol
                    new ItemStack( Blocks.planks, 1, 4 ),
                    new ItemStack( Blocks.planks, 1, 5 ),
                    new ItemStack( Blocks.sand, 1, 1 ), // Red sand
                    new ItemStack( Blocks.packed_ice, 1, 0 ),
                    new ItemStack( Blocks.stained_glass, 1, 0 ),
                    new ItemStack( Blocks.stained_glass, 1, 1 ),
                    new ItemStack( Blocks.stained_glass, 1, 2 ),
                    new ItemStack( Blocks.stained_glass, 1, 3 ),
                    new ItemStack( Blocks.stained_glass, 1, 4 ),
                    new ItemStack( Blocks.stained_glass, 1, 5 ),
                    new ItemStack( Blocks.stained_glass, 1, 6 ),
                    new ItemStack( Blocks.stained_glass, 1, 7 ),
                    new ItemStack( Blocks.stained_glass, 1, 8 ),
                    new ItemStack( Blocks.stained_glass, 1, 9 ),
                    new ItemStack( Blocks.stained_glass, 1, 10 ),
                    new ItemStack( Blocks.stained_glass, 1, 11 ),
                    new ItemStack( Blocks.stained_glass, 1, 12 ),
                    new ItemStack( Blocks.stained_glass, 1, 13 ),
                    new ItemStack( Blocks.stained_glass, 1, 14 ),
                    new ItemStack( Blocks.stained_glass, 1, 15 ),
            };
        }
        return s_impostorBlocks;
    }
    */

    public static class SubType
    {
        public static final int Standard = 0;
        public static final int FiftyFifty = 1;
        public static final int Count = 2;
    }

    public BlockQBlock()
    {
        setCreativeTab( QCraft.getCreativeTab() );
        setHardness( 5.0f );
        setResistance( 10.0f );
        setSoundType( SoundType.METAL );
        setRegistryName( "qcraft:qblock" );
        setTranslationKey("block.qcraft.qblock");
    }

    @Override
    public boolean getUseNeighborBrightness(IBlockState state)
    {
        return state.getBlock().getUseNeighborBrightness(state);
    }

    public int getSubType( IBlockAccess world, BlockPos pos )
    {
        return world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos));
    }

    // IQuantumObservable implementation

    @Override
    public boolean isObserved( World world, BlockPos pos, EnumFacing facing )
    {
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            return qBlock.isForceObserved(facing);
        }
        return false;
    }

    @Override
    public void observe( World world, BlockPos pos, EnumFacing facing)
    {
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.setForceObserved( facing, true );
        }
    }

    @Override
    public void reset( World world, BlockPos pos, EnumFacing facing)
    {
        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.setForceObserved( facing, false );
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

    /*
    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    @Override
    public boolean shouldSideBeRendered( IBlockAccess iblockaccess, int i, int j, int k, int l )
    {
        return true;
    }

    @Override
    public int getRenderType()
    {
        return blockRenderID;
    }*/

    /*
    @Override
    public boolean isNormalCube( IBlockAccess world, BlockPos pos )
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null && !( block instanceof BlockCompressedPowered ) && block != Blocks.ice && block != Blocks.packed_ice && block != Blocks.glass && block != Blocks.stained_glass )
        {
            return true;
        }
        return false;
    }

     */

    @Override
    @Nullable
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos pos, BlockPos beaconPos)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() ) {
            return block.getBeaconColorMultiplier(state, world, pos, beaconPos);
        }
        return super.getBeaconColorMultiplier(state, world, pos, beaconPos);
    /*
        Block block = getImpostorBlock( world, pos );
        if( block == Blocks.grass )
        {
            return block.colorMultiplier( world,pos );
        }
        return 0xffffff;*/
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState)
    {
        if( entity instanceof EntityPlayer )
        {
            // Air blocks are solid to people with goggles on
            EntityPlayer player = (EntityPlayer) entity;
            if( QCraft.isPlayerWearingQuantumGoggles( player ) )
            {
                collidingBoxes.add(new AxisAlignedBB(pos, pos.add(1,1,1)));
                return;
            }
        }
        getImpostorBlock(worldIn, pos).addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entity, isActualState);

    }

    /*
    @Override
    public boolean isReplaceable( IBlockAccess world, BlockPos pos )
    {

		Appearance appearance = getAppearance( world,pos );
		int type = getImpostorType( world,pos );
		if( appearance == Appearance.Block && type == 0 )
		{
			return true;
		}

        return false;
    }*/

/*
    @Override
    public void setBlockBoundsBasedOnState( IBlockAccess world, BlockPos pos )
    {
        Appearance appearance = getAppearance( world,pos );
        ItemStack type = getImpostorStack( world,pos );
        if( appearance != Appearance.Block || !type.isEmpty() )
        {
            super.setBlockBounds( 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f );
        }
        else
        {
            super.setBlockBounds( 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f );
        }
    }*/

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() ){
            return block.getCollisionBoundingBox(blockState, world, pos);
        }
        return super.getCollisionBoundingBox(blockState, world, pos);
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() ){
            return block.getSelectedBoundingBox(state, world, pos);
        }
        return super.getSelectedBoundingBox(state, world, pos);
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos )
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() ) {
            return blockState.getBlockHardness(world, pos);
        }
        return 0.0f;
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return block.getExplosionResistance( world, pos, exploder, explosion );
        }
        return 0.0f;

    }

    @Override
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return getImpostorBlock(world, pos).isSideSolid(base_state, world, pos, side );
        }
        return false;
    }

    @Override
    public boolean isAir(IBlockState blockState, IBlockAccess world, BlockPos pos )
    {
        return getImpostorStack(world, pos).isEmpty();
    }

    @Override
    public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return getImpostorBlock(world, pos).canSustainLeaves(state, world, pos );
        }
        return false;
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return getImpostorBlock(world, pos).canBeReplacedByLeaves(state, world, pos );
        }
        return true;
    }

    @Override
    public boolean isWood( IBlockAccess world, BlockPos pos )
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return getImpostorBlock(world, pos).isWood( world, pos );
        }
        return false;
    }

    @Override
    public int getFlammability( IBlockAccess world, BlockPos pos, EnumFacing face )
    {
        Block block = getImpostorBlock( world, pos );
        if( !getImpostorStack(world, pos).isEmpty() )
        {
            return block.getFlammability( world,pos, face );
        }
        return 0;
    }

    @Override
    public boolean isFlammable( IBlockAccess world, BlockPos pos, EnumFacing face )
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.isFlammable( world,pos, face );
        }
        return false;
    }

    @Override
    public int getFireSpreadSpeed( IBlockAccess world, BlockPos pos, EnumFacing face )
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.getFireSpreadSpeed( world,pos, face );
        }
        return 0;
    }

    @Override
    public boolean isFireSource( World world, BlockPos pos, EnumFacing side )
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.isFireSource( world,pos, side );
        }
        return false;
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.getLightOpacity(state, world,pos );
        }
        return 0;
    }

    @Override
    public boolean isBeaconBase( IBlockAccess world, BlockPos pos, BlockPos beaconPos )
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.isBeaconBase( world,pos, beaconPos );
        }
        return false;
    }

    @Override
    public List<ItemStack> getDrops( IBlockAccess world, BlockPos pos, IBlockState state, int fortune )
    {
        if(!qDrop.isEmpty()) {
            return Collections.singletonList(qDrop);
        }
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.getDrops(world, pos, state, fortune);
        }
        return super.getDrops(world, pos, state, fortune);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        // removeBlockByPlayer handles this instead
    }



    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if( world.isRemote )
        {
            return false;
        }

        if( !player.capabilities.isCreativeMode )
        {
            if( EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand()) > 0)
            {
                // Silk harvest (get qblock back)
                TileEntity entity = world.getTileEntity(new BlockPos(pos ));
                if(entity instanceof TileEntityQBlock)
                {
                    TileEntityQBlock qblock = (TileEntityQBlock) entity;
                    qDrop = ItemQBlock.create( qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1 );
                    dropBlockAsItem( world,pos, state, 0 );
                    qDrop = ItemStack.EMPTY;
                }
            }
            else
            {
                // Regular harvest (get impostor)
                Block block = getImpostorBlock( world, pos );
                if( block != null )
                {
                    if( block.canHarvestBlock(world,pos, player) )
                    {
                        int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, player.getHeldItemMainhand());
                        block.dropBlockAsItem(world, pos, getImpostorState(world, pos), fortune);
                    }
                }
            }
        }
        //TODO needs further inspection
        //return super.removedByPlayer( world, player,pos );
        return true;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
        TileEntity entity = world.getTileEntity(new BlockPos(pos));
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock qblock = (TileEntityQBlock) entity;
            return ItemQBlock.create( qblock.getSubType(), qblock.getTypes(), qblock.getEntanglementFrequency(), 1 );
        }
        return null;
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack)
    {
    }

    @Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player)
    {
        return false;
    }

    @Override
    public void onBlockPlacedBy( World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack )
    {
        world.setBlockState(pos, getImpostorState(world,pos), 3 );
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
    {
        Block block = getImpostorBlock( world, pos);
        block.updateTick( world, pos, state, rand);

    }

    @Override
    protected void onStartFalling(EntityFallingBlock fallingEntity) // onStartFalling
    {
        // Setup NBT for block to place
        World world = fallingEntity.world;
        int x = (int) ( fallingEntity.posX - 0.5f );
        int y = (int) ( fallingEntity.posY - 0.5f );
        int z = (int) ( fallingEntity.posZ - 0.5f );
        TileEntity entity = world.getTileEntity(new BlockPos(x, y,z));
        if(entity instanceof TileEntityQBlock)
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            entity.writeToNBT( nbttagcompound );
            fallingEntity.readFromNBT(nbttagcompound); // data
        }

        // Prevent the falling qBlock from dropping items
        fallingEntity.shouldDropItem = false; // dropItems
    }

    @Override
    public void onEndFalling(World worldIn, BlockPos pos, IBlockState fallingState, IBlockState hitState) // onStopFalling
    {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity instanceof TileEntityQBlock) {
            TileEntityQBlock qBlock = (TileEntityQBlock) entity;
            qBlock.hasJustFallen = true;
        }
    }

    @Override
    public boolean canProvidePower(IBlockState state)
    {
        return true;
    }

    @Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, @Nullable EnumFacing side)
    {
        Block block = getImpostorBlock( world, pos );
        return block.canConnectRedstone(state, world, pos, side);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        Block block = getImpostorBlock( world, pos);
        return block.shouldCheckWeakPower(state, world, pos, side);
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        Block block = getImpostorBlock( world, pos );
        if( block != null )
        {
            return block.getLightValue(state, world, pos);
        }
        return 0;
    }

    public int getColorForType( int side, int type )
    {
        if( type == 2 ) // grass
        {
            //return ( side == 1 ) ? Blocks.GRASS : 0xffffff;
        }
        return 0xffffff;
    }
/*
    public IIcon getIconForType( int side, int type, Appearance appearance )
    {
        if( appearance == Appearance.Swirl )
        {
            return s_swirlIcon;
        }
        else if( appearance == Appearance.Fuzz )
        {
            return s_fuzzIcon;
        }
        else //if( appearance == Appearance.Block )
        {
            ItemStack[] blockList = getImpostorBlockList();
            if( type >= 0 && type < blockList.length )
            {
                ItemStack item = blockList[ type ];
                if( item != null )
                {
                    Block block = ((ItemBlock)item.getItem()).getBlock();
                    int damage = item.getItemDamage();
                    return block.getIcon( side, damage );
                }
            }
            return s_transparentIcon;
        }
    }

    @Override
    public IIcon getIcon( IBlockAccess world, BlockPos pos, int side )
    {
        int type = getImpostorStack( world,pos );
        Appearance appearance = getAppearance( world,pos );
        return getIconForType( side, type, appearance );
    }

    public static boolean s_forceGrass = false;


    @Override
    public IIcon getIcon( int side, int damage )
    {
        if( s_forceGrass )
        {
            return Blocks.GRASS.getIcon( side, damage );
        }
        else
        {
            return s_swirlIcon;
        }
    }

    @Override
    public void registerBlockIcons( IIconRegister iconRegister )
    {
        s_transparentIcon = iconRegister.registerIcon( "qcraft:transparent" );
        s_swirlIcon = iconRegister.registerIcon( "qcraft:qblock_swirl" );
        s_fuzzIcon = iconRegister.registerIcon( "qcraft:qblock_fuzz" );
    }
*/
    @Override
    public TileEntity createNewTileEntity( World world, int metadata )
    {
        return new TileEntityQBlock();
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState b)
    {
        return createNewTileEntity( world, b.getBlock().getMetaFromState(b) );
    }

    private Appearance getAppearance( IBlockAccess world, BlockPos pos )
    {
        TileEntity entity = world.getTileEntity(new BlockPos(pos ));
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            return quantum.getAppearance();
        }
        return Appearance.Fuzz;
    }

    private ItemStack getImpostorStack(IBlockAccess world, BlockPos pos )
    {

        TileEntity entity = world.getTileEntity(pos);
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            return quantum.getObservedType();
        }

        return ItemStack.EMPTY;
    }

    public Block getImpostorBlock( IBlockAccess world, BlockPos pos )
    {
        // Return block
        //ItemStack type = getImpostorStack( world, pos );

            ItemStack item = getImpostorStack(world, pos);
            if( item != null && !item.isEmpty() )
            {
                return Block.getBlockFromItem( item.getItem() );
            }

        return null;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        this.facing = facing;
        this.hitX = hitX;
        this.hitY = hitY;
        this.hitZ = hitZ;
        this.meta = meta;
        this.placer = placer;
        return getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
    }
    private IBlockState getImpostorState(IBlockAccess world, BlockPos pos)
    {
        // Return damage
        ItemStack stack = getImpostorStack( world, pos);
        if (stack.getItem() instanceof ItemBlock) {
            return ((ItemBlock) stack.getItem()).getBlock().getStateForPlacement((World) world, pos, facing, hitX, hitY, hitZ, meta, placer);
        }
        return null;
    }
}
