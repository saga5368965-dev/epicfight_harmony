package sagaa.epicfight_harmony;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

@Mod.EventBusSubscriber(modid = Epicfight_harmony.MODID)
public class HarmonyCapabilityHandler {

    // EFM本体のCapabilityの場所を安全に参照するためのID
    private static final ResourceLocation EFM_CAP_ID = new ResourceLocation("epicfight", "capabilities");

    /**
     * 優先度をLOWESTに設定し、他のアドオンが処理を終えた後に介入する。
     * これにより「他アドオンとの競合」を物理的に回避する。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();

        // 1. 他のアドオンやEFM本体が既にCapabilityを付与しているかチェック
        // これにより、既存のアドオンの挙動を100%優先する
        if (event.getCapabilities().containsKey(EFM_CAP_ID) ||
                stack.getCapability(CapabilityItem.CAPABILITY_ITEM).isPresent()) {
            return;
        }

        // 2. タグやクラスから武器種を動的に判別
        // 判別できない場合は null が返り、そのまま何もしない（フォールバック）
        WeaponCategory detectedCategory = autoDetectCategory(stack);

        if (detectedCategory != null) {
            // ここで、EFM本体のクラスを直接newするのではなく、
            // 互換性レイヤーを通じて付与する（本体アプデ対策）
            HarmonyBridge.applyCompatibleCapability(event, stack, detectedCategory);
        }
    }

    private static WeaponCategory autoDetectCategory(ItemStack stack) {
        // バニラのクラス判定（最も確実）
        if (stack.getItem() instanceof SwordItem) return CapabilityItem.WeaponCategories.SWORD;
        if (stack.getItem() instanceof AxeItem) return CapabilityItem.WeaponCategories.AXE;

        // タグベースの判定（他MOD、TiC、ガンダムMOD、特闘MODなどの広域カバー）
        if (stack.getTags().anyMatch(tag -> tag.location().getPath().contains("swords"))) {
            return CapabilityItem.WeaponCategories.SWORD;
        }

        // 判別できない場合は「深追いしない」のがHarmony流
        return null;
    }
}