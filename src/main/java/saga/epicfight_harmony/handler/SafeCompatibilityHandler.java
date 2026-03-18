package saga.epicfight_harmony;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.epicfight_harmony.capability.HarmonyWeaponCapabilityProvider;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

@Mod.EventBusSubscriber(modid = Epicfight_harmony.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SafeCompatibilityHandler {

    // EFMが内部で使用するCapabilityの識別子
    // 文字列指定なので、マッピング（開発/本番）の影響を受けません
    private static final ResourceLocation EFM_CAP_ID = ResourceLocation.fromNamespaceAndPath("epicfight", "capabilities");

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachCapabilitiesSafe(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();

        // 1. 他のMODやEFM本体が既に Capability を付与済みかチェック
        // IDチェックと型チェックの両方を行うことで、あらゆる環境で確実にガードします
        if (event.getCapabilities().containsKey(EFM_CAP_ID) || hasEpicFightCapability(stack)) {
            return;
        }

        // 2. 武器種を判定（わからないときは null が返る）
        WeaponCategory category = Epicfight_harmony.getInstance().autoDetectCategory(stack);

        // 3. 【調和の鉄則】判定できないアイテムには何もせず、バニラ（他MOD）の挙動を守る
        if (category == null || category == CapabilityItem.WeaponCategories.NOT_WEAPON) {
            return;
        }

        try {
            // 4. 動的ブリッジを付与
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(Epicfight_harmony.MODID, "dynamic_bridge"),
                    new HarmonyWeaponCapabilityProvider(stack, category)
            );
        } catch (Exception e) {
            // エラー時も安全を優先（開発環境での型不一致などの事故防止）
        }
    }

    /**
     * リフレクションや直参照を避け、Forge推奨の CapabilityToken を使用。
     * これにより、開発環境と本番環境の違い（難読化）を吸収します。
     */
    private static boolean hasEpicFightCapability(ItemStack stack) {
        try {
            // 型トークンによる実行時取得
            Capability<CapabilityItem> cap = CapabilityManager.get(new CapabilityToken<>() {});
            return stack.getCapability(cap).isPresent();
        } catch (NoClassDefFoundError | Exception e) {
            // EFMが入っていない環境でもクラッシュしない
            return false;
        }
    }
}