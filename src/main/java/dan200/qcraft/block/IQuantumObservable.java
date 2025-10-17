package dan200.qcraft.block;

import net.minecraft.util.EnumFacing;

/**
 * Interface to blocks with quantum property.
 * DO NOT do remote observing, that's entangle 
 */
public interface IQuantumObservable {
    boolean isObserved();
    
    boolean isObserved(EnumFacing facing);

    void observe(EnumFacing facing);

    void reset(EnumFacing facing);
}
