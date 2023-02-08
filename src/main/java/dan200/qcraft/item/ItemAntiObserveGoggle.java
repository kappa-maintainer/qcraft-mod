package dan200.qcraft.item;

import dan200.qcraft.QCraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class ItemAntiObserveGoggle extends ItemArmor {
    public ItemAntiObserveGoggle() {
        super(Objects.requireNonNull(EnumHelper.addArmorMaterial("qgoggle", "qcraft:qgoggle", 0, new int[]{0,0,0,0}, 0, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 1)), 0, EntityEquipmentSlot.HEAD);
        setRegistryName("qcraft:ao_goggles");
        setTranslationKey("qcraft.ao_goggles");
        setCreativeTab(QCraft.QCRAT_TAB);

    }
    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "qcraft:textures/armor/ao_goggles.png";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks){
        QCraft.proxy.renderAntiObserveGoggle(resolution);
    }
}
