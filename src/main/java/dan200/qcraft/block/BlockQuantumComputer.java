package dan200.qcraft.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockQuantumComputer extends Block {
    public BlockQuantumComputer() {
        super(Material.IRON);
        setRegistryName("qcraft:computer");
        setTranslationKey("qcraft.computer");
    }
}
