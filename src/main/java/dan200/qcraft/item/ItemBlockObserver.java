package dan200.qcraft.item;

import dan200.qcraft.QCraft;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockObserver extends ItemBlock {
    public ItemBlockObserver(Block block) {
        super(block);
        setRegistryName("qcraft:automatic_observer");
        setTranslationKey("qcraft.automatic_observer");
        setCreativeTab(QCraft.QCRAT_TAB);
    }
}
