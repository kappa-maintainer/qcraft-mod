package dan200.qcraft.entangle;

import dan200.qcraft.Reference;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntangleData extends WorldSavedData {

    private static final String DATA_NAME = Reference.MOD_ID + "_entangle_data";
    
    public EntangleData() {
        super(DATA_NAME);
    }
    
    private static final Map<UUID, SideHolder> entangleMap = new HashMap<>();

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList groupList = compound.getTagList("entangle", Constants.NBT.TAG_COMPOUND);
        groupList.forEach(nbtBase -> {
            UUID uuid = NBTUtil.getUUIDFromTag(((NBTTagCompound) nbtBase).getCompoundTag("uuid"));
            short side = ((NBTTagCompound) nbtBase).getShort("side");
            entangleMap.put(uuid, new SideHolder(side));
            
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList groupList = new NBTTagList();
        for (var entry : entangleMap.entrySet()) {
            NBTTagCompound entangleGroup = new NBTTagCompound();
            entangleGroup.setTag("uuid", NBTUtil.createUUIDTag(entry.getKey()));
            entangleGroup.setShort("side", entry.getValue().getSide());
            groupList.appendTag(entangleGroup);
            
        }
        compound.setTag("entangle", groupList);
        return compound;
    }

    public static EntangleData getInstance(World world) {
        // The IS_GLOBAL constant is there for clarity, and should be simplified into the right branch.
        MapStorage storage = world.getMapStorage();
        EntangleData instance = (EntangleData) storage.getOrLoadData(EntangleData.class, DATA_NAME);

        if (instance == null) {
            instance = new EntangleData();
            storage.setData(DATA_NAME, instance);
        }
        return instance;
    }
    
    public SideHolder getSideFromUUID(UUID uuid, short defaultSide) {
        return entangleMap.computeIfAbsent(uuid, uuid1 -> new SideHolder(defaultSide));
    }
    
}
