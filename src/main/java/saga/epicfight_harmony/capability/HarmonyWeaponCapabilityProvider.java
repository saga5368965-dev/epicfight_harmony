package saga.epicfight_harmony;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public class HarmonyWeaponCapabilityProvider implements ICapabilityProvider {
    private final LazyOptional<CapabilityItem> instance;

    public HarmonyWeaponCapabilityProvider(ItemStack stack, WeaponCategory initialCategory) {
        // 動的判定クラスを生成。getResult()をフックして常に最新の武器種を返します。
        this.instance = LazyOptional.of(() -> new HarmonyDynamicWeaponCap(stack, initialCategory));
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        // 直接 CAPABILITY_ITEM を参照せず、Tokenで安全に取得
        Capability<CapabilityItem> efmCap = CapabilityManager.get(new CapabilityToken<>() {});
        return cap == efmCap ? instance.cast() : LazyOptional.empty();
    }

    /**
     * 調和の核：常にアイテムの状態を監視するCapability
     */
    private static class HarmonyDynamicWeaponCap extends CapabilityItem {
        private final ItemStack stack;

        protected HarmonyDynamicWeaponCap(ItemStack stack, WeaponCategory category) {
            super(CapabilityItem.builder().category(category));
            this.stack = stack;
        }

        @Override
        public WeaponCategory getWeaponCategory() {
            // メインクラスに集約された判定ロジックをリアルタイムに実行
            WeaponCategory dynamicCategory = Epicfight_harmony.getInstance().autoDetectCategory(stack);
            return dynamicCategory != null ? dynamicCategory : super.getWeaponCategory();
        }

        @Override
        public CapabilityItem getResult(ItemStack itemstack) {
            // 使用時に毎回自分自身を評価対象として返すことで、
            // getWeaponCategory() の動的な結果をEFM本体に流し込む
            return this;
        }
    }
}