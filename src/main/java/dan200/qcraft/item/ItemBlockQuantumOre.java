package dan200.qcraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockQuantumOre extends ItemBlock {
    public ItemBlockQuantumOre(Block block) {
        super(block);
        setRegistryName("qcraft:ore");
        setTranslationKey("qcraft.ore");
    }

}
