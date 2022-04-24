package fr.devsylone.fallenkingdom.manager.packets;

import com.mojang.datafixers.util.Pair;
import fr.devsylone.fallenkingdom.utils.NMSUtils;
import fr.devsylone.fallenkingdom.utils.PacketUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class PacketManager1_16 extends PacketManager1_14
{

    @Override
    protected void sendEquipment(int id, ItemSlot slot, Material material)
    {
        try
        {
            ItemStack bukkitItem = new ItemStack(material);
            Object nmsItem = NMSUtils.getClass("CraftItemStack").getDeclaredMethod("asNMSCopy", ItemStack.class).invoke(null, bukkitItem);
            Object itemSlot = getEnumItemSlot(slot);
            Pair<?, ?> pair = Pair.of(itemSlot, nmsItem);

            Object armors = NMSUtils.getClass("PacketPlayOutEntityEquipment").getConstructor(int.class, List.class).newInstance(id, Collections.singletonList(pair));
            PacketUtils.sendPacket(getPlayer(id), armors);
        }catch(ReflectiveOperationException ex)
        {
            ex.printStackTrace();
        }
    }

}
