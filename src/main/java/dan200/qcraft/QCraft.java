package dan200.qcraft;


import codechicken.lib.CodeChickenLib;
import codechicken.lib.render.block.BlockRenderingRegistry;
import dan200.qcraft.block.BlockFuzz;
import dan200.qcraft.block.BlockObserver;
import dan200.qcraft.block.BlockQBlock;
import dan200.qcraft.block.BlockQuantumOre;
import dan200.qcraft.block.BlockSwirl;
import dan200.qcraft.crafting.QBlockRecipe;
import dan200.qcraft.gen.OreGenerator;
import dan200.qcraft.item.*;
import dan200.qcraft.proxy.IProxy;
import dan200.qcraft.render.QBlockBakedModel;
import dan200.qcraft.render.QBlockMeshDefinition;
import dan200.qcraft.tileentity.QBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        useMetadata = true,
        version = Reference.VERSION,
        dependencies = CodeChickenLib.MOD_VERSION_DEP
)
@Mod.EventBusSubscriber
public class QCraft {
    
    @SidedProxy(clientSide = "dan200.qcraft.proxy.ClientProxy", serverSide = "dan200.qcraft.proxy.CommonProxy")
    public static IProxy proxy;
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID);
    public static Logger LOGGER = LogManager.getLogger("qCraft");
	public static int currentType;
    private int ticks;
	@Instance(Reference.MOD_ID)
	public static QCraft _instance;

    private static EnumBlockRenderType qblock;
    public QCraft() {}

    public static final CreativeTabs QCRAT_TAB = new CreativeTabs("qcraft") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(QCraftItems.itemEoO);
        }
        @SideOnly(Side.CLIENT)
        public String getTranslationKey()
        {
            return "itemgroup.qcraft.name";
        }
    };

    @SubscribeEvent
    public void registerRecipe(RegistryEvent.Register<IRecipe> event) {
        event.getRegistry().register(new QBlockRecipe().setRegistryName(new ResourceLocation("qcraft", "qblockrecipe")));

    }

    @SubscribeEvent
    public void registerBlock(RegistryEvent.Register<Block> event) {
        QCraftBlocks.blockFuzz = new BlockFuzz();
        QCraftBlocks.blockSwirl = new BlockSwirl();
        QCraftBlocks.blockQuantumOre = new BlockQuantumOre(false);
        QCraftBlocks.blockQuantumOreOn = new BlockQuantumOre(true);
        QCraftBlocks.blockObserver = new BlockObserver(false);
        QCraftBlocks.blockQBlock = new BlockQBlock();

        IForgeRegistry<Block> registry = event.getRegistry();
        registry.register(QCraftBlocks.blockFuzz);
        registry.register(QCraftBlocks.blockSwirl);
        registry.register(QCraftBlocks.blockQuantumOre);
        registry.register(QCraftBlocks.blockQuantumOreOn);
        registry.register(QCraftBlocks.blockObserver);
        registry.register(QCraftBlocks.blockQBlock);
        registry.register(new Block(Material.AIR).setRegistryName("qcraft:transparent").setTranslationKey("qcraft.transparent"));

        GameRegistry.registerTileEntity(QBlockTileEntity.class, new ResourceLocation("qcraft:qbte"));
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        QCraftItems.itemQuantumDust = new ItemQuantumDust();
        QCraftItems.itemEoO = new ItemEoO();
        QCraftItems.itemEoS = new ItemEoS();
        QCraftItems.itemEoE = new ItemEoE();
        QCraftItems.itemBlockQuantumOre = new ItemBlockQuantumOre(QCraftBlocks.blockQuantumOre);
        QCraftItems.itemBlockObserver = new ItemBlockObserver(QCraftBlocks.blockObserver);
        QCraftItems.itemQuantumGoggle = new ItemQuantumGoggle();
        QCraftItems.itemAntiObserveGoggle = new ItemAntiObserveGoggle();
        QCraftItems.itemBlockQBlock = new ItemBlockQBlock(QCraftBlocks.blockQBlock);

        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(QCraftItems.itemQuantumDust);
        registry.register(QCraftItems.itemEoO);
        registry.register(QCraftItems.itemEoS);
        registry.register(QCraftItems.itemEoE);
        registry.register(QCraftItems.itemBlockQuantumOre);
        registry.register(QCraftItems.itemBlockObserver);
        registry.register(QCraftItems.itemQuantumGoggle);
        registry.register(QCraftItems.itemAntiObserveGoggle);
        registry.register(QCraftItems.itemBlockQBlock);

    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void initModel(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemQuantumDust, 0,
                new ModelResourceLocation(QCraftItems.itemQuantumDust.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemEoO, 0,
                new ModelResourceLocation(QCraftItems.itemEoO.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemEoS, 0,
                new ModelResourceLocation(QCraftItems.itemEoS.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemEoE, 0,
                new ModelResourceLocation(QCraftItems.itemEoE.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemBlockQuantumOre, 0,
                new ModelResourceLocation(QCraftItems.itemBlockQuantumOre.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemBlockObserver, 0,
                new ModelResourceLocation(QCraftItems.itemBlockObserver.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemQuantumGoggle, 0,
                new ModelResourceLocation(QCraftItems.itemQuantumGoggle.getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(QCraftItems.itemAntiObserveGoggle, 0,
                new ModelResourceLocation(QCraftItems.itemAntiObserveGoggle.getRegistryName(), "inventory"));

        ModelLoader.setCustomMeshDefinition(QCraftItems.itemBlockQBlock, new  QBlockMeshDefinition());

        //ClientRegistry.bindTileEntitySpecialRenderer(QBlockTileEntity.class, new QBlockTERender());



    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event) {
        StateMapperBase mapperBase = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return QBlockBakedModel.variant;
            }
        };
        ModelLoader.setCustomStateMapper(QCraftBlocks.blockQBlock, mapperBase);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelBake(ModelBakeEvent event) {
        IBakedModel iBakedModel = event.getModelRegistry().getObject(QBlockBakedModel.variant);
        LOGGER.info("STATE {}", iBakedModel);
        if(iBakedModel != null) {
            QBlockBakedModel model = new QBlockBakedModel(iBakedModel);
            event.getModelRegistry().putObject(QBlockBakedModel.variant, model);
        }
    }

    @SubscribeEvent
    public void handleTick( TickEvent.ClientTickEvent clientTickEvent )
    {
        if( clientTickEvent.phase == TickEvent.Phase.START )
        {
            ticks++;
        }
        ticks = ticks % 120;
        if(ticks % 20 < 5) {
            currentType = 6;
        } else {
            currentType = ticks / 20;
        }

    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        qblock = BlockRenderingRegistry.createRenderType("qblock");
        proxy.preInit(event);
        MinecraftForge.EVENT_BUS.register(QCraft._instance);
    }

    @EventHandler
    public static void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new OreGenerator(), 0);
    }
    
    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
    }
    
    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
    }

    @EventHandler
    public void onServerStopping(FMLServerStoppingEvent event) {
    }

    public static EnumBlockRenderType getQblockRenderType() {
        return qblock;
    }
}
