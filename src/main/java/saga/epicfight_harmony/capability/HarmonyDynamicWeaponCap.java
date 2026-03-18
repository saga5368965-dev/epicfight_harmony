package saga.epicfight_harmony.capability; // パッケージを合わせる

import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import net.minecraft.world.item.ItemStack;
import saga.epicfight_harmony.Epicfight_harmony;

public class HarmonyDynamicWeaponCap extends CapabilityItem {
    private final ItemStack stack;

    public HarmonyDynamicWeaponCap(ItemStack stack, WeaponCategory defaultCategory) {
        // ビルダーで初期カテゴリを設定して親クラスを初期化
        super(CapabilityItem.builder().category(defaultCategory));
        this.stack = stack;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        // アイテムのNBTなどをリアルタイムに監視してカテゴリを返す
        WeaponCategory detected = Epicfight_harmony.getInstance().autoDetectCategory(stack);
        return detected != null ? detected : super.getWeaponCategory();
    }
}