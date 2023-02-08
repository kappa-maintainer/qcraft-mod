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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import dan200.QCraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.RecipeSorter;

import java.io.*;
import java.nio.file.Files;

import static net.minecraftforge.oredict.RecipeSorter.Category.SHAPED;

@Mod.EventBusSubscriber(modid = "qcraft")
public abstract class QCraftProxyCommon implements IQCraftProxy
{
    public QCraftProxyCommon()
    {
    }

    // IQCraftProxy implementation

    @Override
    public void preLoad()
    {
        //registerItems();
    }

    @Override
    public void load()
    {
        //registerTileEntities();
        registerForgeHandlers();
    }

    @Override
    public abstract boolean isClient();

    @Override
    public abstract Object getQuantumComputerGUI( InventoryPlayer inventory, TileEntityQuantumComputer computer );

    @Override
    public abstract void showItemTransferGUI( EntityPlayer entityPlayer, TileEntityQuantumComputer computer );

    @Override
    public abstract void travelToServer( LostLuggage.Address address );

    @Override
    public boolean isPlayerWearingGoggles( EntityPlayer player )
    {
        ItemStack headGear = player.inventory.armorItemInSlot( 3 );
        return headGear.getItem() == QCraft.Items.quantumGoggles;
    }

    @Override
    public boolean isPlayerWearingQuantumGoggles( EntityPlayer player )
    {
        ItemStack headGear = player.inventory.armorItemInSlot( 3 );
        return headGear.getItem() == QCraft.Items.quantumGoggles && headGear.getItemDamage() == ItemQuantumGoggles.SubTypes.Quantum;
    }

    @Override
    public abstract boolean isLocalPlayerWearingGoggles();

    @Override
    public abstract boolean isLocalPlayerWearingQuantumGoggles();

    @Override
    public abstract void renderQuantumGogglesOverlay( float width, float height );

    @Override
    public abstract void renderAOGogglesOverlay( float width, float height );

    @Override
    public abstract void spawnQuantumDustFX( World world, BlockPos pos );

    @SubscribeEvent
    private void registerBlocks(RegistryEvent.Register<Block> e) {
        // Register our own creative tab
        QCraft.creativeTab = new CreativeTabQuantumCraft(CreativeTabs.getNextID(), "qCraft");

        // BLOCKS

        // Quantum ore blocks
        QCraft.Blocks.quantumOre = new BlockQuantumOre(false);
        e.getRegistry().register(QCraft.Blocks.quantumOre);

        QCraft.Blocks.quantumOreGlowing = new BlockQuantumOre(true);
        e.getRegistry().register(QCraft.Blocks.quantumOreGlowing);

        // Quantum logic block
        QCraft.Blocks.quantumLogic = new BlockQuantumLogic();
        e.getRegistry().register(QCraft.Blocks.quantumLogic);

        // qBlock block
        QCraft.Blocks.qBlock = new BlockQBlock();
        e.getRegistry().register(QCraft.Blocks.qBlock);

        // Quantum Computer block
        QCraft.Blocks.quantumComputer = new BlockQuantumComputer();
        e.getRegistry().register(QCraft.Blocks.quantumComputer);

        // Quantum Portal block
        QCraft.Blocks.quantumPortal = new BlockQuantumPortal();
        e.getRegistry().register(QCraft.Blocks.quantumPortal);
    }
        // ITEMS
    @SubscribeEvent
    private void registerItems(RegistryEvent.Register<Item> e) {
        // Quantum Dust item
        QCraft.Items.quantumDust = new ItemQuantumDust();
        e.getRegistry().register( QCraft.Items.quantumDust);

        // EOS item
        QCraft.Items.eos = new ItemEOS();
        e.getRegistry().register( QCraft.Items.eos);

        QCraft.Items.eos = new ItemEOO();
        e.getRegistry().register( QCraft.Items.eoo);

        QCraft.Items.eos = new ItemEOE();
        e.getRegistry().register( QCraft.Items.eoe);

        // Quantum Goggles item
        QCraft.Items.quantumGoggles = new ItemQuantumGoggles();
        e.getRegistry().register( QCraft.Items.quantumGoggles);
        
        // Dummy item to contain (modded) items that were sent to this server, but don't exist here
        QCraft.Items.missingItem = new ItemMissing();
        e.getRegistry().register( QCraft.Items.missingItem);

        // RECIPES

        // Automated Observer recipe
        /*ItemStack observer = new ItemStack( QCraft.Blocks.quantumLogic, 1, BlockQuantumLogic.SubType.ObserverOff );
        GameRegistry.addRecipe( observer, new Object[]{
            "XXX", "XYX", "XZX",
            Character.valueOf( 'X' ), Blocks.stone,
            Character.valueOf( 'Y' ), new ItemStack( QCraft.Items.eos, 1, ItemEOS.SubType.Observation ),
            Character.valueOf( 'Z' ), Items.redstone
        } );

        // EOS recipe
        ItemStack eos = new ItemStack( QCraft.Items.eos, 1, ItemEOS.SubType.Superposition );
        GameRegistry.addRecipe( eos, new Object[]{
            "XX", "XX",
            Character.valueOf( 'X' ), QCraft.Items.quantumDust,
        } );

        // EOO recipe
        ItemStack eoo = new ItemStack( QCraft.Items.eos, 1, ItemEOS.SubType.Observation );
        GameRegistry.addRecipe( eoo, new Object[]{
            " X ", "X X", " X ",
            Character.valueOf( 'X' ), QCraft.Items.quantumDust,
        } );

        // EOE recipe
        ItemStack eoe = new ItemStack( QCraft.Items.eos, 1, ItemEOS.SubType.Entanglement );
        GameRegistry.addRecipe( eoe, new Object[]{
            "X X", " Y ", "X X",
            Character.valueOf( 'X' ), QCraft.Items.quantumDust,
            Character.valueOf( 'Y' ), eos,
        } );

        // qBlock recipes
        GameRegistry.addRecipe( new QBlockRecipe() );
        RecipeSorter.register( "qCraft:qBlock", QBlockRecipe.class, SHAPED, "after:minecraft:shapeless" );

        GameRegistry.addRecipe( new EntangledQBlockRecipe() );
        RecipeSorter.register( "qCraft:entangled_qBlock", EntangledQBlockRecipe.class, SHAPED, "after:minecraft:shapeless" );

        // Quantum Computer recipe
        ItemStack regularQuantumComputer = ItemQuantumComputer.create( -1, 1 );
        GameRegistry.addRecipe( regularQuantumComputer, new Object[] {
            "XXX", "XYX", "XZX",
            Character.valueOf( 'X' ), Items.iron_ingot,
            Character.valueOf( 'Y' ), QCraft.Items.quantumDust,
            Character.valueOf( 'Z' ), Blocks.glass_pane,
        } );

        // Entangled Quantum Computer
        ItemStack entangledQuantumComputer = ItemQuantumComputer.create( 0, 1 );
        GameRegistry.addRecipe( new EntangledQuantumComputerRecipe() );
        RecipeSorter.register( "qCraft:entangled_computer", EntangledQuantumComputerRecipe.class, SHAPED, "after:minecraft:shapeless" );

        // Quantum Goggles recipe
        ItemStack quantumGoggles = new ItemStack( QCraft.Items.quantumGoggles, 1, ItemQuantumGoggles.SubTypes.Quantum );
        GameRegistry.addRecipe( quantumGoggles, new Object[] {
            "XYX",
            Character.valueOf( 'X' ), Blocks.glass_pane,
            Character.valueOf( 'Y' ), QCraft.Items.quantumDust,
        } );

        // Anti-observation goggles recipe
        ItemStack aoGoggles = new ItemStack( QCraft.Items.quantumGoggles, 1, ItemQuantumGoggles.SubTypes.AntiObservation );
        GameRegistry.addRecipe( aoGoggles, new Object[] {
            "XYX",
            Character.valueOf( 'X' ), Blocks.glass_pane,
            Character.valueOf( 'Y' ), new ItemStack( QCraft.Items.eos, 1, ItemEOS.SubType.Observation ),
        } );

        if( QCraft.enableWorldGenReplacementRecipes )
        {
            // Quantum dust recipe
            GameRegistry.addRecipe( new ItemStack( QCraft.Items.quantumDust, 2 ), new Object[] {
                "XY",
                Character.valueOf( 'X' ), Items.redstone,
                Character.valueOf( 'Y' ), new ItemStack( Items.dye, 1, 10 ) // Lime green
            } );
        }*/
    }

    @SubscribeEvent
    private void registerTileEntities(RegistryEvent.Register<Block> event)
    {
        // Tile Entities
        GameRegistry.registerTileEntity( TileEntityQBlock.class, new ResourceLocation("qcraft:qblock") );
        GameRegistry.registerTileEntity( TileEntityQuantumComputer.class, new ResourceLocation("qcraft:qcomputer" ));
    }

    private void registerForgeHandlers()
    {
        ForgeHandlers handlers = new ForgeHandlers();
        MinecraftForge.EVENT_BUS.register( handlers );
        //FMLCommonHandler.instance().bus().register( handlers );
        if( QCraft.enableWorldGen )
        {
            GameRegistry.registerWorldGenerator( new QuantumOreGenerator(), 1 );
        }
        NetworkRegistry.INSTANCE.registerGuiHandler( QCraft.instance, handlers );

        ConnectionHandler connectionHandler = new ConnectionHandler();
        MinecraftForge.EVENT_BUS.register( connectionHandler );
        //FMLCommonHandler.instance().bus().register( connectionHandler );
    }

    public class ForgeHandlers implements
        IGuiHandler
    {
        private ForgeHandlers()
        {
        }

        // IGuiHandler implementation

        @Override
        public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
        {
            TileEntity tile = world.getTileEntity( new BlockPos(x,y,z) );
            if (id == QCraft.quantumComputerGUIID) {
                if (tile instanceof TileEntityQuantumComputer) {
                    TileEntityQuantumComputer computer = (TileEntityQuantumComputer) tile;
                    return new ContainerQuantumComputer(player.inventory, computer);
                }
            }
            return null;
        }

        @Override
        public Object getClientGuiElement( int id, EntityPlayer player, World world, int x, int y, int z)
        {
            TileEntity tile = world.getTileEntity( new BlockPos(x,y,z) );
            if (id == QCraft.quantumComputerGUIID) {
                if (tile instanceof TileEntityQuantumComputer) {
                    TileEntityQuantumComputer drive = (TileEntityQuantumComputer) tile;
                    return getQuantumComputerGUI(player.inventory, drive);
                }
            }
            return null;
        }

        // Forge event responses

        @SubscribeEvent
        public void onPlayerLogin( PlayerEvent.PlayerLoggedInEvent event )
        {
            EntityPlayer player = event.player;
            if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
            {
                QCraft.clearUnverifiedLuggage( player ); // Shouldn't be necessary, but can't hurt
                QCraft.requestLuggage( player );
            }
        }

        @SubscribeEvent
        public void onPlayerLogout( PlayerEvent.PlayerLoggedOutEvent event )
        {
            EntityPlayer player = event.player;
            if( FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
            {
                QCraft.clearUnverifiedLuggage( player );
            }
        }
    }

    public static NBTTagCompound loadNBTFromPath( File file )
    {
        try
        {
            if( file != null && file.exists() )
            {
                try (InputStream input = new BufferedInputStream(Files.newInputStream(file.toPath()))) {
                    return CompressedStreamTools.readCompressed(input);
                }
            }
        }
        catch( IOException e )
        {
            QCraft.log( "Warning: failed to load QCraft entanglement info" );
        }
        return null;
    }

    public static void saveNBTToPath( File file, NBTTagCompound nbt )
    {
        try
        {
            if( file != null )
            {
                file.getParentFile().mkdirs();
                try (OutputStream output = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
                    CompressedStreamTools.writeCompressed(nbt, output);
                }
            }
        }
        catch( IOException e )
        {
            QCraft.log( "Warning: failed to save QCraft entanglement info" );
        }
    }
}
