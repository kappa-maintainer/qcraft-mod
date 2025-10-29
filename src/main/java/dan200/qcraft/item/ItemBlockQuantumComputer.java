package dan200.qcraft.item;

import dan200.qcraft.QCraft;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockQuantumComputer extends ItemBlock {
    public ItemBlockQuantumComputer(Block block) {
        super(block);
        setRegistryName("qcraft:computer");
        setTranslationKey("qcraft.computer");
        setCreativeTab(QCraft.QCRAT_TAB);
    }
}
