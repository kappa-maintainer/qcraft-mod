package dan200.qcraft.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockRandomQBlock extends ItemBlockQBlock {
    public ItemBlockRandomQBlock(Block qblock) {
        super(qblock);
        setTranslationKey("qcraft.qblock");
    }

    @Override
    public String getTranslationKey(ItemStack stack)
    {
        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey("entangled")) {
            return "tile.qcraft.qblock_entangled";
        } else {
            return "tile.qcraft.qblock";
        }
    }
}
