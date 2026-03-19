package saga.epicfight_harmony.capability;

import net.minecraft.world.item.ItemStack;
import saga.epicfight_harmony.Epicfight_harmony;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class HarmonyDynamicWeaponCap extends CapabilityItem {
    private final ItemStack stack;
    private WeaponCategory lastCategory;
    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL = 1000;

    public HarmonyDynamicWeaponCap(ItemStack stack, WeaponCategory initialCategory) {
        super(CapabilityItem.builder().category(initialCategory));
        this.stack = stack;
        this.lastCategory = initialCategory;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        if (stack == null || stack.isEmpty()) {
            return CapabilityItem.WeaponCategories.FIST;
        }

        long now = System.currentTimeMillis();
        if (now - lastCheckTime > CHECK_INTERVAL) {
            try {
                // メインクラスの判定を実行（この中ではCapabilityを参照しない）
                WeaponCategory dynamic = Epicfight_harmony.getInstance().autoDetectCategory(stack);
                if (dynamic != null && dynamic != CapabilityItem.WeaponCategories.NOT_WEAPON) {
                    this.lastCategory = dynamic;
                }
            } catch (Exception e) {
                // エラー時はフォールバックとして前回の値を維持
            }
            this.lastCheckTime = now;
        }
        return this.lastCategory;
    }

    @Override
    public CapabilityItem getResult(ItemStack itemstack) {
        // 自分自身を返すことで、常に最新の動的判定が適用されるようにする
        return this;
    }

    @Override
    public boolean isEmpty() {
        return stack == null || stack.isEmpty();
    }
}