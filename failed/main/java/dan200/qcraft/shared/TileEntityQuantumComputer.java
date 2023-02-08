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

import com.google.common.base.CaseFormat;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import dan200.QCraft;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class TileEntityQuantumComputer extends TileEntity implements ITickable
{
    public static final EntanglementRegistry<TileEntityQuantumComputer> ComputerRegistry = new EntanglementRegistry<TileEntityQuantumComputer>();
    public static final EntanglementRegistry<TileEntityQuantumComputer> ClientComputerRegistry = new EntanglementRegistry<TileEntityQuantumComputer>();
    private static boolean tooManyPossiblePortals = false;

    public static EntanglementRegistry<TileEntityQuantumComputer> getEntanglementRegistry( World world )
    {
        if( !world.isRemote )
        {
            return ComputerRegistry;
        }
        else
        {
            return ClientComputerRegistry;
        }
    }

    public static class AreaShape
    {
        public int m_xMin;
        public int m_xMax;
        public int m_yMin;
        public int m_yMax;
        public int m_zMin;
        public int m_zMax;

        public boolean equals( AreaShape o )
        {
            return
                o.m_xMin == m_xMin &&
                o.m_xMax == m_xMax &&
                o.m_yMin == m_yMin &&
                o.m_yMax == m_yMax &&
                o.m_zMin == m_zMin &&
                o.m_zMax == m_zMax;
        }
    }

    public static class AreaData
    {
        public AreaShape m_shape;
        public Block[] m_blocks;
        public int[] m_metaData;

        public NBTTagCompound encode()
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger( "xmin", m_shape.m_xMin );
            nbttagcompound.setInteger( "xmax", m_shape.m_xMax );
            nbttagcompound.setInteger( "ymin", m_shape.m_yMin );
            nbttagcompound.setInteger( "ymax", m_shape.m_yMax );
            nbttagcompound.setInteger( "zmin", m_shape.m_zMin );
            nbttagcompound.setInteger( "zmax", m_shape.m_zMax );

            NBTTagList blockNames = new NBTTagList();
            for (Block mBlock : m_blocks) {
                String name = null;
                if (mBlock != null) {
                    name = mBlock.getRegistryName().toString();
                }
                if (name != null && name.length() > 0) {
                    blockNames.appendTag(new NBTTagString(name));
                } else {
                    blockNames.appendTag(new NBTTagString("null"));
                }
            }
            nbttagcompound.setTag( "blockNames", blockNames );

            nbttagcompound.setIntArray( "metaData", m_metaData );
            return nbttagcompound;
        }

        public static AreaData decode( NBTTagCompound nbttagcompound )
        {
            AreaData storedData = new AreaData();
            storedData.m_shape = new AreaShape();
            storedData.m_shape.m_xMin = nbttagcompound.getInteger( "xmin" );
            storedData.m_shape.m_xMax = nbttagcompound.getInteger( "xmax" );
            storedData.m_shape.m_yMin = nbttagcompound.getInteger( "ymin" );
            storedData.m_shape.m_yMax = nbttagcompound.getInteger( "ymax" );
            storedData.m_shape.m_zMin = nbttagcompound.getInteger( "zmin" );
            storedData.m_shape.m_zMax = nbttagcompound.getInteger( "zmax" );

            int size =
                ( storedData.m_shape.m_xMax - storedData.m_shape.m_xMin + 1 ) *
                ( storedData.m_shape.m_yMax - storedData.m_shape.m_yMin + 1 ) *
                ( storedData.m_shape.m_zMax - storedData.m_shape.m_zMin + 1 );
            storedData.m_blocks = new Block[ size ];
            if( nbttagcompound.hasKey( "blockData" ) )
            {
                int[] blockIDs = nbttagcompound.getIntArray( "blockData" );
                for( int i=0; i<size; ++i )
                {
                    storedData.m_blocks[i] = Block.getBlockById( blockIDs[i] );
                }
            }
            else
            {
                NBTTagList blockNames = nbttagcompound.getTagList( "blockNames", Constants.NBT.TAG_STRING );
                for( int i=0; i<size; ++i )
                {
                    String name = blockNames.getStringTagAt( i );
                    if( name.length() > 0 && !name.equals( "null" ) )
                    {
                        storedData.m_blocks[i] = Block.getBlockFromName( name );
                    }
                }
            }
            storedData.m_metaData = nbttagcompound.getIntArray( "metaData" );
            return storedData;
        }
    }

    // Shared state
    private boolean m_powered;
    private int m_entanglementFrequency;
    private int m_timeSinceEnergize;

    // Area Teleportation state
    private AreaData m_storedData;

    // Server Teleportation state
    private String m_portalID;
    private boolean m_portalNameConflict;
    private String m_remoteServerAddress;
    private String m_remoteServerName;
    private String m_remotePortalID;

    public TileEntityQuantumComputer()
    {
        m_powered = false;
        m_entanglementFrequency = -1;
        m_timeSinceEnergize = 0;

        m_storedData = null;

        m_portalID = null;
        m_portalNameConflict = false;
        m_remoteServerAddress = null;
        m_remoteServerName = null;
        m_remotePortalID = null;
    }

    private EntanglementRegistry<TileEntityQuantumComputer> getEntanglementRegistry()
    {
        return getEntanglementRegistry( world );
    }

    private PortalRegistry getPortalRegistry()
    {
        return PortalRegistry.getPortalRegistry( world );
    }

    @Override
    public void validate()
    {
        super.validate();
        register();
    }

    @Override
    public void invalidate()
    {
        unregister();
        super.invalidate();
    }

    public void onDestroy()
    {
        PortalLocation location = getPortal();
        if( location != null && isPortalDeployed( location ) )
        {
            undeployPortal( location );
        }
        unregisterPortal();
    }

    // Entanglement

    private void register()
    {
        if( m_entanglementFrequency >= 0 )
        {
            getEntanglementRegistry().register( m_entanglementFrequency, this, this.getWorld() );
        }
    }

    private void unregister()
    {
        if( m_entanglementFrequency >= 0 )
        {
            getEntanglementRegistry().unregister( m_entanglementFrequency, this, this.getWorld() );
        }
    }

    public void setEntanglementFrequency( int frequency )
    {
        if( m_entanglementFrequency != frequency )
        {
            unregister();
            m_entanglementFrequency = frequency;
            register();
        }
    }

    public int getEntanglementFrequency()
    {
        return m_entanglementFrequency;
    }

    private TileEntityQuantumComputer findEntangledTwin()
    {
        if( m_entanglementFrequency >= 0 )
        {
            List<TileEntityQuantumComputer> twins = ComputerRegistry.getEntangledObjects( m_entanglementFrequency );
            if( twins != null )
            {
                for (TileEntityQuantumComputer computer : twins) {
                    if (computer != this) {
                        return computer;
                    }
                }
            }
        }
        return null;
    }

    // Area Teleportation

    public void setStoredData( AreaData data )
    {
        m_storedData = data;
    }

    public AreaData getStoredData()
    {
        return m_storedData;
    }

    private boolean isPillarBase( int x, int y, int z, int side )
    {
        if( y < 0 || y >= 256 )
        {
            return false;
        }

        TileEntity entity = world.getTileEntity(new BlockPos( x, y, z ));
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            ItemStack[] types = quantum.getTypes();
            for( int i = 0; i < 6; ++i )
            {
                if( i == side )
                {
                    if( OreDictionary.getOres("blockGold").contains(types[i])) // GOLD
                    {
                        return false;
                    }
                }
                else
                {
                    if( OreDictionary.getOres("obsidian").contains(types[i]) ) // OBSIDIAN
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean isPillar( int x, int y, int z )
    {
        if( y < 0 || y >= 256 )
        {
            return false;
        }

        IBlockState block = world.getBlockState(new BlockPos( x, y, z ));
        return block.getBlock() == Blocks.OBSIDIAN;
    }

    private boolean isGlass( int x, int y, int z )
    {
        if( y < 0 || y >= 256 )
        {
            return false;
        }

        IBlockState block = world.getBlockState(new BlockPos( x, y, z ));
        if( block.getMaterial() == Material.GLASS && !(block instanceof BlockPane) )
        {
            return true;
        }
        return false;
    }

    private AreaShape calculateShape()
    {
        AreaShape shape = new AreaShape();
        shape.m_xMin = -99;
        shape.m_xMax = -99;
        shape.m_yMin = 0;
        shape.m_yMax = 0;
        shape.m_zMin = -99;
        shape.m_zMax = -99;
        for( int i = 0; i < QCraft.maxQTPSize; ++i )
        {
            if( shape.m_xMin == -99 && isPillarBase( getPos().getX() - i - 1, getPos().getY(), getPos().getZ(), 5 ) )
            {
                shape.m_xMin = -i;
            }
            if( shape.m_xMax == -99 && isPillarBase( getPos().getX() + i + 1, getPos().getY(), getPos().getZ(), 4 ) )
            {
                shape.m_xMax = i;
            }
            if( shape.m_zMin == -99 && isPillarBase( getPos().getX(), getPos().getY(), getPos().getZ() - i - 1, 3 ) )
            {
                shape.m_zMin = -i;
            }
            if( shape.m_zMax == -99 && isPillarBase( getPos().getX(), getPos().getY(), getPos().getZ() + i + 1, 2 ) )
            {
                shape.m_zMax = i;
            }
        }

        if( shape.m_xMin != -99 &&
                shape.m_xMax != -99 &&
                shape.m_zMin != -99 &&
                shape.m_zMax != -99 )
        {
            // Find Y Min
            for( int i = 1; i < QCraft.maxQTPSize; ++i )
            {
                if( isPillar( getPos().getX() + shape.m_xMin - 1, getPos().getY() - i, getPos().getZ() ) &&
                        isPillar( getPos().getX() + shape.m_xMax + 1, getPos().getY() - i, getPos().getZ() ) &&
                        isPillar( getPos().getX(), getPos().getY() - i, getPos().getZ() + shape.m_zMin - 1 ) &&
                        isPillar( getPos().getX(), getPos().getY() - i, getPos().getZ() + shape.m_zMax + 1 ) )
                {
                    shape.m_yMin = -i;
                }
                else
                {
                    break;
                }
            }

            // Find Y Max
            for( int i = 1; i < QCraft.maxQTPSize; ++i )
            {
                if( isPillar( getPos().getX() + shape.m_xMin - 1, getPos().getY() + i, getPos().getZ() ) &&
                        isPillar( getPos().getX() + shape.m_xMax + 1, getPos().getY() + i, getPos().getZ() ) &&
                        isPillar( getPos().getX(), getPos().getY() + i, getPos().getZ() + shape.m_zMin - 1 ) &&
                        isPillar( getPos().getX(), getPos().getY() + i, getPos().getZ() + shape.m_zMax + 1 ) )
                {
                    shape.m_yMax = i;
                }
                else
                {
                    break;
                }
            }

            // Check glass caps
            int top = getPos().getY() + shape.m_yMax + 1;
            if( isGlass( getPos().getX() + shape.m_xMin - 1, top, getPos().getZ() ) &&
                    isGlass( getPos().getX() + shape.m_xMax + 1, top, getPos().getZ() ) &&
                    isGlass( getPos().getX(), top, getPos().getZ() + shape.m_zMin - 1 ) &&
                    isGlass( getPos().getX(), top, getPos().getZ() + shape.m_zMax + 1 ) )
            {
                return shape;
            }
        }
        return null;
    }

    private AreaData storeArea()
    {
        AreaShape shape = calculateShape();
        if( shape == null )
        {
            return null;
        }

        AreaData storedData = new AreaData();
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;

        int size = ( maxX - minX + 1 ) * ( maxY - minY + 1 ) * ( maxZ - minZ + 1 );
        int index = 0;

        storedData.m_shape = shape;
        storedData.m_blocks = new Block[ size ];
        storedData.m_metaData = new int[ size ];
        for( int y = minY; y <= maxY; ++y )
        {
            for( int x = minX; x <= maxX; ++x )
            {
                for( int z = minZ; z <= maxZ; ++z )
                {
                    int worldX = getPos().getX() + x;
                    int worldY = getPos().getY() + y;
                    int worldZ = getPos().getZ() + z;
                    if( !( worldX == getPos().getX() && worldY == getPos().getY() && worldZ == getPos().getZ() ) )
                    {
                        TileEntity tileentity = world.getTileEntity(new BlockPos( worldX, worldY, worldZ ));
                        if( tileentity != null )
                        {
                            return null;
                        }

                        IBlockState block = world.getBlockState(new BlockPos( worldX, worldY, worldZ ));
                        int meta = world.getBlockState(new BlockPos( worldX, worldY, worldZ )).getBlock().getMetaFromState(world.getBlockState(new BlockPos( worldX, worldY, worldZ )));
                        storedData.m_blocks[ index ] = block.getBlock();
                        storedData.m_metaData[ index ] = meta;
                    }
                    index++;
                }
            }
        }

        return storedData;
    }

    private void notifyBlockOfNeighborChange( int x, int y, int z )
    {
        world.notifyNeighborsOfStateChange( new BlockPos(x, y, z), world.getBlockState(new BlockPos( x, y, z )).getBlock(), false);
    }

    private void notifyEdgeBlocks( AreaShape shape )
    {
        // Notify the newly transported blocks on the edges of the area that their neighbours have changed
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;
        for( int x = minX; x <= maxX; ++x )
        {
            for( int y = minY; y <= maxY; ++y )
            {
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + y, getPos().getZ() + minZ );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + y, getPos().getZ() + maxZ );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + y, getPos().getZ() + minZ );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + y, getPos().getZ() + maxZ + 1 );
            }
        }
        for( int x = minX; x <= maxX; ++x )
        {
            for( int z = minZ; z <= maxZ; ++z )
            {
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + minY, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + maxY, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + minY - 1, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + x, getPos().getY() + maxY + 1, getPos().getZ() + z );
            }
        }
        for( int y = minY; y <= maxY; ++y )
        {
            for( int z = minZ; z <= maxZ; ++z )
            {
                notifyBlockOfNeighborChange( getPos().getX() + minX, getPos().getY() + y, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + maxX, getPos().getY() + y, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + minX - 1, getPos().getY() + y, getPos().getZ() + z );
                notifyBlockOfNeighborChange( getPos().getX() + maxX + 1, getPos().getY() + y, getPos().getZ() + z );
            }
        }
    }

    private Set<EntityItem> getEntityItemsInArea( AreaShape shape )
    {
        AxisAlignedBB aabb = new AxisAlignedBB(
                getPos().getX() + shape.m_xMin,
                getPos().getY() + shape.m_yMin,
                getPos().getZ() + shape.m_zMin,
                getPos().getX() + shape.m_xMax + 1,
                getPos().getY() + shape.m_yMax + 1,
                getPos().getZ() + shape.m_zMax + 1
        );

        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity( null, aabb );
        Set<EntityItem> set = new HashSet<EntityItem>();
        for (Entity value : list) {
            if (value instanceof EntityItem && !value.isDead) {
                set.add((EntityItem) value);
            }
        }
        return set;
    }

    private void killNewItems( Set<EntityItem> before, Set<EntityItem> after )
    {
        for (EntityItem item : after) {
            if (!item.isDead && !before.contains(item)) {
                item.setDead();
            }
        }
    }

    private void clearArea( AreaShape shape )
    {
        // Cache the loose entities
        Set<EntityItem> before = getEntityItemsInArea( shape );

        // Set the area around us to air, notifying the adjacent blocks
        int minX = shape.m_xMin;
        int maxX = shape.m_xMax;
        int minY = shape.m_yMin;
        int maxY = shape.m_yMax;
        int minZ = shape.m_zMin;
        int maxZ = shape.m_zMax;
        for( int y = minY; y <= maxY; ++y )
        {
            for( int x = minX; x <= maxX; ++x )
            {
                for( int z = minZ; z <= maxZ; ++z )
                {
                    int worldX = getPos().getX() + x;
                    int worldY = getPos().getY() + y;
                    int worldZ = getPos().getZ() + z;
                    if( !( worldX == getPos().getX() && worldY == getPos().getY() && worldZ == getPos().getZ() ) )
                    {
                        world.setBlockToAir(new BlockPos( worldX, worldY, worldZ ));
                    }
                }
            }
        }

        // Kill the new entities
        Set<EntityItem> after = getEntityItemsInArea( shape );
        killNewItems( before, after );

        // Notify edge blocks
        notifyEdgeBlocks( shape );
    }

    private void unpackArea( AreaData storedData )
    {
        // Cache the loose entities
        Set<EntityItem> before = getEntityItemsInArea( storedData.m_shape );

        // Set the area around us to the stored data
        int index = 0;
        int minX = storedData.m_shape.m_xMin;
        int maxX = storedData.m_shape.m_xMax;
        int minY = storedData.m_shape.m_yMin;
        int maxY = storedData.m_shape.m_yMax;
        int minZ = storedData.m_shape.m_zMin;
        int maxZ = storedData.m_shape.m_zMax;
        for( int y = minY; y <= maxY; ++y )
        {
            for( int x = minX; x <= maxX; ++x )
            {
                for( int z = minZ; z <= maxZ; ++z )
                {
                    int worldX = getPos().getX() + x;
                    int worldY = getPos().getY() + y;
                    int worldZ = getPos().getZ() + z;
                    if( !( worldX == getPos().getX() && worldY == getPos().getY() && worldZ == getPos().getZ() ) )
                    {
                        Block block = storedData.m_blocks[ index ];
                        if( block != null )
                        {
                            int meta = storedData.m_metaData[ index ];
                            world.setBlockState( new BlockPos(worldX, worldY, worldZ), block.getStateFromMeta(meta), 2 );
                        }
                        else
                        {
                            world.setBlockToAir( new BlockPos(worldX, worldY, worldZ ));
                        }
                    }
                    index++;
                }
            }
        }

        // Kill the new entities
        Set<EntityItem> after = getEntityItemsInArea( storedData.m_shape );
        killNewItems( before, after );

        // Notify edge blocks
        notifyEdgeBlocks( storedData.m_shape );
    }

    private boolean checkCooling()
    {
        for( int i = 0; i < 6; ++i )
        {
            /*
            int x = getPos().getX() + Facing.offsetsXForSide[ i ];
            int y = getPos().getY() + Facing.offsetsYForSide[ i ];
            int z = getPos().getZ() + Facing.offsetsZForSide[ i ];
             */
            IBlockState block = world.getBlockState( getPos().offset(EnumFacing.byIndex(i)) );
            if(block.getMaterial() == Material.ICE || block.getMaterial() == Material.PACKED_ICE)
            {
                return true;
            }
        }
        return false;
    }

    public static enum TeleportError
    {
        Ok,
        NoTwin,
        FrameIncomplete,
        DestinationFrameIncomplete,
        FrameMismatch,
        FrameObstructed,
        InsufficientCooling,
        AreaNotTransportable,
        DestinationNotTransportable,
        FrameDeployed,
        NameConflict,
        MultiplePossiblePortalsFound;

        public static String decode( TeleportError error )
        {
            if( error != Ok )
            {
                return "gui.qcraft:computer.error_" + CaseFormat.UPPER_CAMEL.to( CaseFormat.LOWER_UNDERSCORE, error.toString() );
            }
            return null;
        }
    }

    private TeleportError canTeleport()
    {
        // Check entangled:
        TileEntityQuantumComputer twin = null;
        if( m_entanglementFrequency >= 0 )
        {
            // Find entangled twin:
            twin = findEntangledTwin();

            // Check the twin exists:
            if( twin == null )
            {
                return TeleportError.NoTwin;
            }
        }

        // Check the shape is big enough:
        AreaShape localShape = calculateShape();
        if( localShape == null )
        {
            return TeleportError.FrameIncomplete;
        }

        if( twin != null )
        {
            // Check the twin shape matches
            AreaShape twinShape = twin.calculateShape();
            if( twinShape == null )
            {
                return TeleportError.DestinationFrameIncomplete;
            }
            if( !localShape.equals( twinShape ) )
            {
                return TeleportError.FrameMismatch;
            }
        }
        else
        {
            // Check the stored shape matches
            if( m_storedData != null )
            {
                if( !localShape.equals( m_storedData.m_shape ) )
                {
                    return TeleportError.FrameMismatch;
                }
            }
        }

        // Check cooling
        if( !checkCooling() )
        {
            return TeleportError.InsufficientCooling;
        }

        // Store the two areas:
        AreaData localData = storeArea();
        if( localData == null )
        {
            return TeleportError.AreaNotTransportable;
        }

        if( twin != null )
        {
            AreaData twinData = twin.storeArea();
            if( twinData == null )
            {
                return TeleportError.DestinationNotTransportable;
            }
        }

        return TeleportError.Ok;
    }

    private TeleportError tryTeleport()
    {
        TeleportError error = canTeleport();
        if( error == TeleportError.Ok )
        {
            if( m_entanglementFrequency >= 0 )
            {
                // Store the two areas:
                TileEntityQuantumComputer twin = findEntangledTwin();
                if( twin != null )
                {
                    AreaData localData = storeArea();
                    AreaData twinData = twin.storeArea();
                    if( localData != null && twinData != null )
                    {
                        // Unpack the two areas:
                        unpackArea( twinData );
                        twin.unpackArea( localData );
                    }
                }
            }
            else
            {
                // Store the local area:
                AreaData localData = storeArea();

                // Unpack the stored area:
                if( m_storedData != null )
                {
                    unpackArea( m_storedData );
                }
                else
                {
                    clearArea( localData.m_shape );
                }

                m_storedData = localData;
            }

            // Effects
            world.playSound(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.AMBIENT, 1.0F, 1.0F , false);

        }
        return error;
    }

    // Server Teleportation

    private void registerPortal()
    {
        PortalLocation location = findPortal();
        if( location != null )
        {
            if( m_portalID == null )
            {
                m_portalID = getPortalRegistry().getUnusedID();
                //world.mark( getPos().getX(), getPos().getY(), getPos().getZ() );
                this.notifyBlockOfNeighborChange(getPos().getX(), getPos().getY(), getPos().getZ());
            }
            if( !getPortalRegistry().register( m_portalID, location ) )
            {
                m_portalNameConflict = true;
            }
            else
            {
                m_portalNameConflict = false;
            }
            EntanglementSavedData.get(this.getWorld()).markDirty(); //Notify that this needs to be saved on world save
        }
        tooManyPossiblePortals = false;
    }

    private void unregisterPortal()
    {
        if( m_portalID != null )
        {
            if( !m_portalNameConflict )
            {
                getPortalRegistry().unregister( m_portalID );
                EntanglementSavedData.get(this.getWorld()).markDirty(); //Notify that this needs to be saved on world save
            }
            m_portalNameConflict = false;
        }
    }

    public void setPortalID( String id )
    {
        unregisterPortal();
        m_portalID = id;
        registerPortal();
    }

    public String getPortalID()
    {
        return m_portalID;
    }

    public void setRemoteServerAddress( String address )
    {
        m_remoteServerAddress = address;
        m_remoteServerName = getPortalRegistry().getServerName( address );
    }

    public String getRemoteServerAddress()
    {
        return m_remoteServerAddress;
    }

    public String getRemoteServerName()
    {
        return m_remoteServerName;
    }

    public void cycleRemoteServerAddress( String previousAddress )
    {
        m_remoteServerAddress = getPortalRegistry().getServerAddressAfter( previousAddress );
        m_remoteServerName = getPortalRegistry().getServerName( m_remoteServerAddress );
    }

    public void setRemotePortalID( String id )
    {
        m_remotePortalID = id;
    }

    public String getRemotePortalID()
    {
        return m_remotePortalID;
    }

    public boolean isTeleporter()
    {
        if( m_entanglementFrequency >= 0 )
        {
            return false;
        }
        if( !QCraft.canAnybodyCreatePortals() )
        {
            return false;
        }
        PortalLocation location = getPortal();
        if( location != null )
        {
            return true;
        }
        return false;
    }

    public boolean isTeleporterEnergized()
    {
        return canDeactivatePortal() == TeleportError.Ok;
    }

    private boolean isPortalCorner( int x, int y, int z, int dir )
    {
        if( y < 0 || y >= 256 )
        {
            return false;
        }

        TileEntity entity = world.getTileEntity(new BlockPos( x, y, z ));
        if(entity instanceof TileEntityQBlock)
        {
            TileEntityQBlock quantum = (TileEntityQBlock) entity;
            ItemStack[] types = quantum.getTypes();
            for( int i = 0; i < 6; ++i )
            {
                if( i == dir || i == EnumFacing.byIndex(dir).getOpposite().getIndex() )
                {
                    if( OreDictionary.getOres("blockGold").contains(types[i]) ) // GOLD
                    {
                        return false;
                    }
                }
                else
                {
                    if( OreDictionary.getOres("obsidian").contains(types[i]) ) // OBSIDIAN
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static class PortalLocation
    {
        public int m_dimensionID;
        public int m_x1;
        public int m_x2;
        public int m_y1;
        public int m_y2;
        public int m_z1;
        public int m_z2;
        
        private PortalLocation(int x1, int y1, int z1, int x2, int y2, int z2, int id) {
            m_dimensionID = id;
            m_x1 = x1;
            m_x2 = x2;
            m_y1 = y1;
            m_y2 = y2;
            m_z1 = z1;
            m_z2 = z2;
        }
        
        public NBTTagCompound encode()
        {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setInteger( "dimensionID", m_dimensionID );
            nbttagcompound.setInteger( "x1", m_x1 );
            nbttagcompound.setInteger( "y1", m_y1 );
            nbttagcompound.setInteger( "z1", m_z1 );
            nbttagcompound.setInteger( "x2", m_x2 );
            nbttagcompound.setInteger( "y2", m_y2 );
            nbttagcompound.setInteger( "z2", m_z2 );
            return nbttagcompound;
        }

        public static PortalLocation decode( NBTTagCompound nbttagcompound )
        {
            int dimID;
            if( nbttagcompound.hasKey( "dimensionID" ) )
            {
                dimID = nbttagcompound.getInteger( "dimensionID" );
            }
            else
            {
                dimID = 0;
            }
            int x1 = nbttagcompound.getInteger( "x1" );
            int y1 = nbttagcompound.getInteger( "y1" );
            int z1 = nbttagcompound.getInteger( "z1" );
            int x2 = nbttagcompound.getInteger( "x2" );
            int y2 = nbttagcompound.getInteger( "y2" );
            int z2 = nbttagcompound.getInteger( "z2" );
            return new PortalLocation(x1, y1, z1, x2, y2, z2, dimID);
        }
    }

    private boolean portalExistsAt( PortalLocation location )
    {
        int lookDir; //direction the portal is looking
        int c1;
        int c2;
        if (location.m_x1 == location.m_x2) {
            lookDir = 4;
            c1 = location.m_z1;
            c2 = location.m_z2;
        } else {
            lookDir = 2;
            c1 = location.m_x1;
            c2 = location.m_x2;
        }
        
        // Check walls from bottom to top
        for( int y = Math.min(location.m_y1, location.m_y2) + 1; y < Math.max(location.m_y1, location.m_y2); ++y )
        {
            if( !isGlass( location.m_x1, y, location.m_z1) )
            {
                return false;
            }
            if( !isGlass( location.m_x2, y, location.m_z2) )
            {
                return false;
            }
        }
        
        // Check ceiling and floor
        for( int xz = Math.min(c1, c2) + 1; xz < Math.max(c1, c2); ++xz )
        {
            if( !isGlass( (location.m_x1 == location.m_x2 ? location.m_x1 : xz) , location.m_y1, (location.m_z1 == location.m_z2 ? location.m_z1 : xz) ) )
            {
                return false;
            }
            if( !isGlass( (location.m_x1 == location.m_x2 ? location.m_x1 : xz), location.m_y2, (location.m_z1 == location.m_z2 ? location.m_z1 : xz) ) )
            {
                return false;
            }
        }
        // Check corners
        if( !isPortalCorner( location.m_x1, location.m_y1, location.m_z1, lookDir ) )
        {
            return false;
        }
        if( !isPortalCorner( location.m_x1, location.m_y2, location.m_z1, lookDir ) )
        {
            return false;
        }
        if( !isPortalCorner( location.m_x2, location.m_y1, location.m_z2, lookDir ) )
        {
            return false;
        }
        if( !isPortalCorner( location.m_x2, location.m_y2, location.m_z2, lookDir ) )
        {
            return false;
        }        
        return true;
    }

    private PortalLocation getPortal()
    {
        if( m_portalID != null )
        {
            if( !m_portalNameConflict )
            {
                return getPortalRegistry().getPortal( m_portalID );
            }
            else
            {
                return findPortal();
            }
        }
        return null;
    }
    
    private ArrayList<PortalLocation> findPortalsAt(int x, int y, int z) {
        ArrayList<PortalLocation> returnValue = new ArrayList<>();
        ArrayList<int[]> possibleCornerPairs = new ArrayList<int[]>();
        if (isGlass(x,y,z)) { //must find a pair of corner blocks that are on the same line and have the same facing.            
            int x1 = 0;
            int y1 = 0;
            int z1 = 0;        
            for( int dir = 0; dir < 6; ++dir ) {
                int tempX = x;
                int tempY = y;
                int tempZ = z;
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { // maximum portal size
                    tempX += EnumFacing.byIndex(dir).getXOffset();
                    tempY += EnumFacing.byIndex(dir).getYOffset();
                    tempZ += EnumFacing.byIndex(dir).getZOffset();
                    if (!isGlass(tempX, tempY, tempZ)) {
                        break;
                    }
                }
                if (dir % 2 == 0) { //every first corner
                     x1 = tempX;
                     y1 = tempY;
                     z1 = tempZ;
                } else { // every second corner
                    if (dir < 2) { //if direction of search was up/down
                        for (int i = 0; i<2; i++) {
                            if (isPortalCorner(tempX, tempY, tempZ, (i+1)*2) &&
                                    isPortalCorner(x1, y1, z1, (i+1)*2) &&
                                    Math.abs(tempY-y1) < QCraft.maxPortalSize + 2) { //+2: +1 for the extra corner block and +1 for the "<" boolean operator
                                int[] temp = {tempX, tempY, tempZ, x1, y1, z1, (i+1)*2};
                                possibleCornerPairs.add(temp);
                                break;
                            }
                        }
                    } else { //if search direction was sideways
                        int perpenDir = ((dir - 1) % 4) + 2;
                        if (isPortalCorner(tempX, tempY, tempZ, perpenDir) &&
                                isPortalCorner(x1, y1, z1, perpenDir) &&
                                Math.abs(tempX-x1) < QCraft.maxPortalSize + 2 &&
                                Math.abs(tempZ-z1) < QCraft.maxPortalSize + 2) {
                            int[] temp = {tempX, tempY, tempZ, x1, y1, z1, perpenDir};
                            possibleCornerPairs.add(temp);
                        }
                    }
                }
            }            
        } else { //if it's a corner
            for( int dir = 0; dir < 6; ++dir ) {
                if (isPortalCorner(x, y, z, dir)) {
                    continue; //skipping the two directions in which this portalCorner can not be part of a portal.
                }
                int tempX = x;
                int tempY = y;
                int tempZ = z;
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { // maximum portal size
                    tempX += EnumFacing.byIndex(dir).getXOffset();
                    tempY += EnumFacing.byIndex(dir).getYOffset();
                    tempZ += EnumFacing.byIndex(dir).getZOffset();
                    if (!isGlass(tempX, tempY, tempZ)) {
                        break;
                    }
                }
                if (dir < 2) { //if direction of search was up/down
                    for (int i = 0; i<2; i++) { //northsouth OR eastwest
                        if (isPortalCorner(x, y, z, (i+1)*2) && isPortalCorner(tempX, tempY, tempZ, (i+1)*2)) {
                            int[] temp = {x, y, z, tempX, tempY, tempZ, (i+1)*2};
                            possibleCornerPairs.add(temp);
                            break;
                        }
                    }
                } else { //if search direction was sideways
                    int perpenDir = ((dir - 1) % 4) + 2;
                    if (isPortalCorner(tempX, tempY, tempZ, perpenDir)) { // we already know that THIS portalcorner is in this direction, since we are skipping the other directions
                        int[] temp = {x, y, z, tempX, tempY, tempZ, perpenDir};
                        possibleCornerPairs.add(temp);
                    }
                }
            }
        }
        for (int[] i : possibleCornerPairs) {
            ArrayList<PortalLocation> temp = findRestOfPortal(i);
            if (temp != null) {
                for (PortalLocation location : temp) {
                    returnValue.add(location);
                }                
            }
        }
        return returnValue; //contains 0 up to 6 portal locations
    }
    
    private ArrayList<PortalLocation> findRestOfPortal(int[] cornerPair) {
        ArrayList<PortalLocation> returnValue = new ArrayList();
        int x1 = cornerPair[0]; //x = east/west = dir 4 5
        int y1 = cornerPair[1]; //y = up/down = dir 0 1
        int z1 = cornerPair[2]; //z = north/south = dir 2 3 
        int x2 = cornerPair[3];
        int y2 = cornerPair[4];
        int z2 = cornerPair[5];
        int lookDir = cornerPair[6]; //direction the portal should be looking
        int searchDir = ((lookDir - (lookDir % 2)) % 4) + 2; //converts {2, 3, 4, 5} to {4, 4, 2, 2}
        if (Math.abs(y1 - y2) < 4 && (Math.abs(x1 - x2) < 3) && (Math.abs(z1 - z2) < 3)) { //if the portal would be too small if this pair of corners would make a portal
            return null;
        } else  if (y1 == y2){ //both corners and the glass between them would form the upper OR lower portal border
            for (int dir = 0; dir < 2; dir++) {
                int tempY = y2;
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { //check for maximal portal size
                    tempY += EnumFacing.byIndex(dir).getYOffset();
                    if (!isGlass(x1, tempY, z1) || !isGlass(x2, tempY,z2)) { //once connected glass stops
                        break;
                    }
                }
                if (isGlass(x1, tempY, z1) || isGlass(x2, tempY, z2) || Math.abs(y1 - tempY) < 4) { //if not both are non-glass OR the portal wouldn't be high enough
                    continue;
                }
                if (isPortalCorner(x1, tempY, z1, lookDir) && isPortalCorner(x2, tempY, z2, lookDir)) {
                    int c1;
                    int c2;
                    if (x1 == x2) {
                        c1 = z1;
                        c2 = z2;
                    } else {
                        c1 = x1;
                        c2 = x2;
                    }
                    //check for completeness of last horizontal border.
                    for(int i = Math.min(c1, c2) + 1; i < Math.max(c1, c2); i++ ) {
                        if (!isGlass((x1 == x2) ? x1 : i, tempY, (z1 == z2) ? z1 : i )) {
                            break;
                        }
                        if (i == Math.max(c1, c2) - 1) {
                            returnValue.add(new PortalLocation(x1, y1, z1, x2, tempY, z2, world.provider.getDimension()));
                        }
                    }
                }
            }
        } else { //if the z and x coordinates of both corners are equal (corners are above eachother)
            for (int dir = searchDir; dir < searchDir+2 ; dir++) {
                int tempX = x2;
                int tempZ = z2;
                for (int i = 0; i < QCraft.maxPortalSize + 1; i++) { //check for maximal portal size
                    tempX += EnumFacing.byIndex(dir).getXOffset();
                    tempZ += EnumFacing.byIndex(dir).getZOffset();
                    if (!isGlass(tempX, y1, tempZ) || !isGlass(tempX, y2,tempZ)) { //once connected glass stops
                        break;
                    }
                }
                if (isGlass(tempX, y1, tempZ) || isGlass(tempX, y2, tempZ) || (Math.abs(x1 - tempX) < 3 && Math.abs(z1 - tempZ) < 3) ) { //if not both are non-glass OR the portal wouldn't be high enough
                    continue;
                }
                if (isPortalCorner(tempX, y1, tempZ, lookDir) && isPortalCorner(tempX, y2, tempZ, lookDir)) {
                    //check for completeness of last vertical border.
                    for(int i = Math.min(y1, y2) + 1; i < Math.max(y1, y2); i++ ) {
                        if (!isGlass(tempX, i, tempZ )) {
                            break;
                        }
                        if (i == Math.max(y1, y2) - 1) {
                            returnValue.add(new PortalLocation(x1, y1, z1, tempX, y2, tempZ, world.provider.getDimension()));
                        }
                    }                    
                }
            }            
        }
        return returnValue; //contains 0 up to 2 portal locations
    }

    private PortalLocation findPortal() 
    {
        ArrayList<PortalLocation> portalLocations = new ArrayList();
        tooManyPossiblePortals = false;
        for( int dir = 0; dir < 6; ++dir )
        {
            // See if this adjoining block is part of a portal:
            int x = getPos().getX() + EnumFacing.byIndex(dir).getXOffset();
            int y = getPos().getY() + EnumFacing.byIndex(dir).getYOffset();
            int z = getPos().getZ() + EnumFacing.byIndex(dir).getZOffset();
            if( !isGlass( x, y, z ) && !isPortalCorner( x, y, z, 2 ) && !isPortalCorner( x, y, z, 4 ) )
            {
                continue;
            }            
            
            ArrayList<PortalLocation> tempLocations = findPortalsAt(x, y, z);
            if ( (tempLocations.size() == 2 && ! (isPortalCorner( x, y, z, 2 ) || isPortalCorner( x, y, z, 4 ) ) ) || tempLocations.size() > 2)  {
                    tooManyPossiblePortals = true;
                    return null;
            }
            portalLocations.addAll(tempLocations);
            if (portalLocations.size() > 2) {
                tooManyPossiblePortals = true;
                return null;
            }
        }
        
        if (portalLocations.size() < 1) {
            return null;
        } else if (portalLocations.size() == 2) {
            PortalLocation portal1 = portalLocations.get(0);
            PortalLocation portal2 = portalLocations.get(1);            
            
            if( Math.min(portal1.m_x1,  portal1.m_x2) == Math.min(portal2.m_x1,  portal2.m_x2) &&
                    Math.min(portal1.m_y1,  portal1.m_y2) == Math.min(portal2.m_y1,  portal2.m_y2) &&
                    Math.min(portal1.m_z1,  portal1.m_z2) == Math.min(portal2.m_z1,  portal2.m_z2))
            {
                return portalLocations.get(0);
            } else {
                tooManyPossiblePortals = true;
                return null;
            }
        } else if (portalLocations.size() > 2) {
            tooManyPossiblePortals = true;
            return null;
        } else {
            return portalLocations.get(0);
        }
    }

    private boolean isPortalClear( PortalLocation portal )
    {
        for( int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y )
        {
            for( int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x )
            {
                if( !world.isAirBlock(new BlockPos( x, y, portal.m_z1 ) ) )
                {
                    return false;
                }
            }
            for( int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z )
            {
                if( !world.isAirBlock(new BlockPos( portal.m_x1, y, z )) )
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void deployPortal( PortalLocation portal )
    {
        for( int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y )
        {
            for( int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x )
            {
                world.setBlockState(new BlockPos(x, y, portal.m_z1), QCraft.Blocks.quantumPortal.getDefaultState() );
            }
            for( int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z )
            {
                world.setBlockState(new BlockPos(portal.m_x1, y, z), QCraft.Blocks.quantumPortal.getDefaultState());
            }
        }
    }

    private void undeployPortal( PortalLocation portal )
    {
        for( int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y )
        {
            for( int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x )
            {
                world.setBlockToAir(new BlockPos(x, y, portal.m_z1 ));
            }
            for( int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z )
            {
                world.setBlockToAir(new BlockPos( portal.m_x1, y, z ));
            }
        }
    }

    private boolean isPortalDeployed( PortalLocation portal )
    {
        for( int y = Math.min(portal.m_y1, portal.m_y2) + 1; y < Math.max(portal.m_y1, portal.m_y2); ++y )
        {
            for( int x = Math.min(portal.m_x1, portal.m_x2) + 1; x < Math.max(portal.m_x1, portal.m_x2); ++x )
            {
                if( world.getBlockState(new BlockPos(x, y, portal.m_z1) ).getBlock() != QCraft.Blocks.quantumPortal )
                {
                    return false;
                }
            }
            for( int z = Math.min(portal.m_z1, portal.m_z2) + 1; z < Math.max(portal.m_z1, portal.m_z2); ++z )
            {
                if( world.getBlockState(new BlockPos(portal.m_x1, y, z )).getBlock() != QCraft.Blocks.quantumPortal )
                {
                    return false;
                }
            }
        }
        return true;
    }

    private TeleportError canActivatePortal()
    {
        if( m_entanglementFrequency >= 0 )
        {
            return TeleportError.FrameIncomplete;
        }
        if( !QCraft.canAnybodyCreatePortals() )
        {
            return TeleportError.FrameIncomplete;
        }

        tooManyPossiblePortals = false;
        PortalLocation location = getPortal();
        if(tooManyPossiblePortals) {
            tooManyPossiblePortals = false;
            return TeleportError.MultiplePossiblePortalsFound;
        }
        
        if( location == null )
        {
            return TeleportError.FrameIncomplete;
        }
        if( isPortalDeployed( location ) )
        {
            return TeleportError.FrameDeployed;
        }
        if( m_portalNameConflict )
        {
            return TeleportError.NameConflict;
        }
        if( !isPortalClear( location ) )
        {
            return TeleportError.FrameObstructed;
        }
        if( !checkCooling() )
        {
            return TeleportError.InsufficientCooling;
        }
        return TeleportError.Ok;
    }

    private TeleportError tryActivatePortal()
    {
        TeleportError error = canActivatePortal();
        if( error == TeleportError.Ok )
        {
            // Deploy
            PortalLocation location = getPortal();
            if( location != null )
            {
                deployPortal( location );
            }

            // Effects
            world.playSound( getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.AMBIENT,  1.0F , 1.0F, false);
        }
        return error;
    }

    public TeleportError canDeactivatePortal()
    {
        if( m_entanglementFrequency >= 0 )
        {
            return TeleportError.FrameIncomplete;
        }
        PortalLocation location = getPortal();
        if( location == null )
        {
            return TeleportError.FrameIncomplete;
        }
        if( !isPortalDeployed( location ) )
        {
            return TeleportError.FrameIncomplete;
        }
        return TeleportError.Ok;
    }

    private TeleportError tryDeactivatePortal()
    {
        TeleportError error = canDeactivatePortal();
        if( error == TeleportError.Ok )
        {
            // Deploy
            PortalLocation location = getPortal();
            if( location != null )
            {
                undeployPortal( location );
            }

            // Effects
            world.playSound( getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, SoundEvents.BLOCK_PORTAL_AMBIENT, SoundCategory.AMBIENT,  1.0F , 1.0F, false);
        }
        return error;
    }


    // Common

    public void setRedstonePowered( boolean powered )
    {
        if( m_powered != powered )
        {
            m_powered = powered;
            if( !world.isRemote )
            {
                if( m_powered && m_timeSinceEnergize >= 4 )
                {
                    tryEnergize();
                }
            }
        }
    }

    public TeleportError canEnergize()
    {
        TeleportError error = canTeleport();
        if( error == TeleportError.Ok )
        {
            return error;
        }

        TeleportError serverError = canActivatePortal();
        if( serverError == TeleportError.Ok )
        {
            return serverError;
        }

        TeleportError deactivateError = canDeactivatePortal();
        if( deactivateError == TeleportError.Ok )
        {
            return deactivateError;
        }

        if( error.ordinal() >= serverError.ordinal() )
        {
            return error;
        }
        else
        {
            return serverError;
        }
    }

    public TeleportError tryEnergize()
    {
        TeleportError error = tryTeleport();
        if( error == TeleportError.Ok )
        {
            m_timeSinceEnergize = 0;
            return error;
        }

        TeleportError serverError = tryActivatePortal();
        if( serverError == TeleportError.Ok )
        {
            m_timeSinceEnergize = 0;
            return serverError;
        }

        TeleportError deactivateError = tryDeactivatePortal();
        if( deactivateError == TeleportError.Ok )
        {
            return deactivateError;
        }

        if( error.ordinal() >= serverError.ordinal() )
        {
            return error;
        }
        else
        {
            return serverError;
        }
    }

    @Override
    public void update()
    {
        m_timeSinceEnergize++;

        if( !world.isRemote )
        {
            // Try to register conflicted portal
            if( m_portalNameConflict )
            {
                registerPortal();
            }

            // Validate existing portal
            PortalLocation location = getPortal();
            if( location != null )
            {
                if( !portalExistsAt( location ) )
                {
                    if( isPortalDeployed( location ) )
                    {
                        undeployPortal( location );
                    }
                    unregisterPortal();
                    location = null;
                }
                else if( !checkCooling() )
                {
                    if( isPortalDeployed( location ) )
                    {
                        undeployPortal( location );
                    }
                }
            }
            else
            {
                unregisterPortal();
            }

            // Find new portal
            if( location == null )
            {
                registerPortal();
                location = getPortal();
            }

            // Try teleporting entities through portal
            if( location != null && isPortalDeployed( location ) )
            {
                // Search for players
                int x1 = Math.min(location.m_x1, location.m_x2);
                int x2 = Math.max(location.m_x1, location.m_x2);
                int y1 = Math.min(location.m_y1, location.m_y2);
                int y2 = Math.max(location.m_y1, location.m_y2);
                int z1 = Math.min(location.m_z1, location.m_z2);
                int z2 = Math.max(location.m_z1, location.m_z2);
                AxisAlignedBB aabb = new AxisAlignedBB(
                    ((double) x1 ) + (x1 == x2 ? 0.25 : 1),
                    ((double) y1 + 1),
                    ((double) z1 ) + (z1 == z2 ? 0.25 : 1),
                    ((double) x2 ) + (x1 == x2 ? 0.75 : 0),
                    ((double) y2 ),
                    ((double) z2 ) + (z1 == z2 ? 0.75 : 0)
                );

                List<EntityPlayer> entities = world.getEntitiesWithinAABB( EntityPlayer.class, aabb );
                if(entities.size() > 0)
                {
                    for (EntityPlayer player : entities) {
                        if (player != null) {
                            if (player.timeUntilPortal <= 0 && player.ticksExisted >= 200 &&
                                    player.getRidingEntity() == null && !player.isBeingRidden()) {
                                // Teleport them:
                                teleportPlayer(player);
                            }
                        }
                    }
                }
            }
        }
    }

    private void teleportPlayer( EntityPlayer player )
    {
        if( m_remoteServerAddress != null )
        {
            queryTeleportPlayerRemote( player );
        }
        else
        {
            teleportPlayerLocal( player, m_remotePortalID );
        }
    }

    private void queryTeleportPlayerRemote( EntityPlayer player )
    {
        QCraft.requestQueryGoToServer( player, this );
        player.timeUntilPortal = 50;
    }

    public void teleportPlayerRemote( EntityPlayer player, boolean takeItems )
    {
        teleportPlayerRemote( player, m_remoteServerAddress, m_remotePortalID, takeItems );
    }

    public static void teleportPlayerRemote( EntityPlayer player, String remoteServerAddress, String remotePortalID, boolean takeItems )
    {
        // Log the trip
        QCraft.log( "Sending player " + player.getDisplayName() + " to server \"" + remoteServerAddress + "\"" );

        NBTTagCompound luggage = new NBTTagCompound();
        if( takeItems )
        {
            // Remove and encode the items from the players inventory we want them to take with them
            NBTTagList items = new NBTTagList();
            InventoryPlayer playerInventory = player.inventory;
            for( int i = 0; i < playerInventory.getSizeInventory(); ++i )
            {
                ItemStack stack = playerInventory.getStackInSlot( i );
                if(stack.getCount() > 0)
                {
                    // Ignore entangled items
                    if( stack.getItem() == Item.getItemFromBlock( QCraft.Blocks.quantumComputer ) && ItemQuantumComputer.getEntanglementFrequency( stack ) >= 0 )
                    {
                        continue;
                    }
                    if( stack.getItem() == Item.getItemFromBlock( QCraft.Blocks.qBlock ) && ItemQBlock.getEntanglementFrequency( stack ) >= 0 )
                    {
                        continue;
                    }

                    // Store items
                    NBTTagCompound itemTag = new NBTTagCompound();
                    if (stack.getItem() == QCraft.Items.missingItem) {
                        itemTag = stack.getTagCompound();
                    } else {
                        stack.writeToNBT( itemTag );
                    }
                    items.appendTag( itemTag );

                    // Remove items
                    playerInventory.setInventorySlotContents( i, ItemStack.EMPTY );
                }
            }

            if( items.tagCount() > 0 )
            {
                QCraft.log( "Removed " + items.tagCount() + " items from " + player.getDisplayName() + "'s inventory." );
                playerInventory.markDirty();
                luggage.setTag( "items", items );
            }
        }

        // Set the destination portal ID
        if( remotePortalID != null )
        {
            luggage.setString( "destinationPortal", remotePortalID );
        }

        try
        {
            // Cryptographically sign the luggage
            luggage.setString( "uuid", UUID.randomUUID().toString() );
            byte[] luggageData = null;
            CompressedStreamTools.write( luggage, (DataOutput) new DataInputStream(new ByteArrayInputStream(luggageData )));
            byte[] luggageSignature = EncryptionRegistry.Instance.signData( luggageData );
            NBTTagCompound signedLuggage = new NBTTagCompound();
            signedLuggage.setByteArray( "key", EncryptionRegistry.Instance.encodePublicKey( EncryptionRegistry.Instance.getLocalKeyPair().getPublic() ) );
            signedLuggage.setByteArray( "luggage", luggageData );
            signedLuggage.setByteArray( "signature", luggageSignature );

            // Send the player to the remote server with the luggage
            //byte[] signedLuggageData = CompressedStreamTools.read( signedLuggage );
            //QCraft.requestGoToServer( player, remoteServerAddress, signedLuggageData );
        }
        catch( IOException e )
        {
            throw new RuntimeException( "Error encoding inventory" );
        }
        finally
        {
            // Prevent the player from being warped twice
            player.timeUntilPortal = 200;
        }
    }

    public void teleportPlayerLocal( EntityPlayer player )
    {
        teleportPlayerLocal( player, m_remotePortalID );
    }

    public static void teleportPlayerLocal( EntityPlayer player, String portalID )
    {
        PortalLocation location = (portalID != null) ?
            PortalRegistry.PortalRegistry.getPortal( portalID ) :
            null;

        if( location != null )
        {
            double xPos = ((double)location.m_x1 + location.m_x2 + 1) / 2;
            double yPos = (double) Math.min(location.m_y1, location.m_y2) + 1;
            double zPos = ((double)location.m_z1 + location.m_z2 + 1) / 2;
            if( location.m_dimensionID == player.dimension )
            {
                player.timeUntilPortal = 40;
                player.setPositionAndUpdate( xPos, yPos, zPos );
            }
            else if( player instanceof EntityPlayerMP )
            {
                player.timeUntilPortal = 40;
                FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().transferPlayerToDimension(
                    (EntityPlayerMP)player,
                    location.m_dimensionID,
                    new QuantumTeleporter(
                            FMLCommonHandler.instance().getMinecraftServerInstance().getWorld( location.m_dimensionID ),
                        xPos, yPos, zPos
                    )
                );
            }
        }
    }

    @Override
    public void readFromNBT( NBTTagCompound nbttagcompound )
    {
        // Read properties
        super.readFromNBT( nbttagcompound );
        m_powered = nbttagcompound.getBoolean( "p" );
        m_timeSinceEnergize = nbttagcompound.getInteger( "tse" );
        m_entanglementFrequency = nbttagcompound.getInteger( "f" );
        if( nbttagcompound.hasKey( "d" ) )
        {
            m_storedData = AreaData.decode( nbttagcompound.getCompoundTag( "d" ) );
        }
        if( nbttagcompound.hasKey( "portalID" ) )
        {
            m_portalID = nbttagcompound.getString( "portalID" );
        }
        m_portalNameConflict = nbttagcompound.getBoolean( "portalNameConflict" );
        if( nbttagcompound.hasKey( "remoteIPAddress" ) )
        {
            m_remoteServerAddress = nbttagcompound.getString( "remoteIPAddress" );
        }
        if( nbttagcompound.hasKey( "remoteIPName" ) )
        {
            m_remoteServerName = nbttagcompound.getString( "remoteIPName" );
        }
        else
        {
            m_remoteServerName = m_remoteServerAddress;
        }
        if( nbttagcompound.hasKey( "remotePortalID" ) )
        {
            m_remotePortalID = nbttagcompound.getString( "remotePortalID" );
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound )
    {
        // Write properties
        super.writeToNBT( nbttagcompound );
        nbttagcompound.setBoolean( "p", m_powered );
        nbttagcompound.setInteger( "tse", m_timeSinceEnergize );
        nbttagcompound.setInteger( "f", m_entanglementFrequency );
        if( m_storedData != null )
        {
            nbttagcompound.setTag( "d", m_storedData.encode() );
        }
        if( m_portalID != null )
        {
            nbttagcompound.setString( "portalID", m_portalID );
        }
        nbttagcompound.setBoolean( "portalNameConflict", m_portalNameConflict );
        if( m_remoteServerAddress != null )
        {
            nbttagcompound.setString( "remoteIPAddress", m_remoteServerAddress );
        }
        if( m_remoteServerName != null )
        {
            nbttagcompound.setString( "remoteIPName", m_remoteServerName );
        }
        if( m_remotePortalID != null )
        {
            nbttagcompound.setString( "remotePortalID", m_remotePortalID );
        }
        return nbttagcompound;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        // Communicate networked state
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setInteger( "f", m_entanglementFrequency );
        if( m_portalID != null )
        {
            nbttagcompound.setString( "portalID", m_portalID );
        }
        nbttagcompound.setBoolean( "portalNameConflict", m_portalNameConflict );
        if( m_remoteServerAddress != null )
        {
            nbttagcompound.setString( "remoteIPAddress", m_remoteServerAddress );
        }
        if( m_remoteServerName != null )
        {
            nbttagcompound.setString( "remoteIPName", m_remoteServerName );
        }
        if( m_remotePortalID != null )
        {
            nbttagcompound.setString( "remotePortalID", m_remotePortalID );
        }
        return new SPacketUpdateTileEntity( this.getPos(), 0, nbttagcompound );
    }

    @Override
    public void onDataPacket( NetworkManager net, SPacketUpdateTileEntity packet )
    {
        // actionType
        if (packet.getTileEntityType() == 0) {// Read networked state
            NBTTagCompound nbttagcompound = packet.getNbtCompound(); // data
            setEntanglementFrequency(nbttagcompound.getInteger("f"));
            if (nbttagcompound.hasKey("portalID")) {
                m_portalID = nbttagcompound.getString("portalID");
            } else {
                m_portalID = null;
            }
            m_portalNameConflict = nbttagcompound.getBoolean("portalNameConflict");
            if (nbttagcompound.hasKey("remoteIPAddress")) {
                m_remoteServerAddress = nbttagcompound.getString("remoteIPAddress");
            } else {
                m_remoteServerAddress = null;
            }
            if (nbttagcompound.hasKey("remoteIPName")) {
                m_remoteServerName = nbttagcompound.getString("remoteIPName");
            } else {
                m_remoteServerName = null;
            }
            if (nbttagcompound.hasKey("remotePortalID")) {
                m_remotePortalID = nbttagcompound.getString("remotePortalID");
            } else {
                m_remotePortalID = null;
            }
        }
    }
}
