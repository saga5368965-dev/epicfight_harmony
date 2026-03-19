package saga.epicfight_harmony.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.epicfight_harmony.Epicfight_harmony;
import saga.epicfight_harmony.capability.HarmonyWeaponCapabilityProvider;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Epicfight_harmony.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SafeCompatibilityHandler {

    private static final ResourceLocation HARMONY_BRIDGE_ID = new ResourceLocation(Epicfight_harmony.MODID, "dynamic_bridge");
    private static final ResourceLocation EFM_CAP_ID = new ResourceLocation("epicfight", "capabilities");
    private static final Map<Item, Boolean> PROCESSED_ITEMS = new ConcurrentHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachCapabilitiesSafe(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack == null || stack.isEmpty()) return;
        Item item = stack.getItem();
        if (PROCESSED_ITEMS.containsKey(item)) {
            return;
        }
        if (event.getCapabilities().containsKey(EFM_CAP_ID) || hasEpicFightCapability(stack)) {
            PROCESSED_ITEMS.put(item, Boolean.TRUE);
            return;
        }
        if (event.getCapabilities().containsKey(HARMONY_BRIDGE_ID)) {
            PROCESSED_ITEMS.put(item, Boolean.TRUE);
            return;
        }

        WeaponCategory category = Epicfight_harmony.getInstance().autoDetectCategory(stack);
        if (category == null || category == CapabilityItem.WeaponCategories.NOT_WEAPON) {
            PROCESSED_ITEMS.put(item, Boolean.TRUE);
            return;
        }

        try {
            event.addCapability(HARMONY_BRIDGE_ID, new HarmonyWeaponCapabilityProvider(stack, category));
            PROCESSED_ITEMS.put(item, Boolean.TRUE);
        } catch (Exception e) {
            PROCESSED_ITEMS.put(item, Boolean.TRUE);
        }
    }

    private static boolean hasEpicFightCapability(ItemStack stack) {
        try {
            Capability<CapabilityItem> cap = CapabilityManager.get(new CapabilityToken<>() {});
            return stack.getCapability(cap).isPresent();
        } catch (Throwable t) {
            return false;
        }
    }
}