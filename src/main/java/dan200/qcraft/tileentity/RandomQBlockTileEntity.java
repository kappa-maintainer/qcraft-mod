package dan200.qcraft.tileentity;

import dan200.qcraft.QCraftBlocks;
import net.minecraft.block.Block;

public class RandomQBlockTileEntity extends QBlockTileEntity{
    @Override
    public Block getBlockType() {
        return QCraftBlocks.blockRandomQBlock;
    }

    @Override
    protected short getObservingSide(double dx, double dy, double dz) {
        return (short) world.rand.nextInt(0, 5);
    }
}
