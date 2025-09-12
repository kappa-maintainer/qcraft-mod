package dan200.qcraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class CamouflageBlockProperty implements IUnlistedProperty<IBlockState> {
    
    public static final CamouflageBlockProperty CURRENT_CAMOU = new CamouflageBlockProperty(); 

    public CamouflageBlockProperty() {

    }

    @Override
    public String getName() {
        return "camouflage-current";
    }

    @Override
    public boolean isValid(IBlockState state) {
        return state.getBlock() != null;
    }


    @Override
    public Class<IBlockState> getType() {
        return IBlockState.class;
    }

    @Override
    public String valueToString(IBlockState state) {
        return state.toString();
    }

}