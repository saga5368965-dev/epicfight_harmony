package saga.epicfight_harmony.mixin;

import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 【真・最終解決版】
 * ターゲット：RenderEngine$Events クラス
 * メソッド：renderLivingPre (EFMはここで全てのエンティティを上書きしています)
 */
@Mixin(targets = "yesman.epicfight.client.events.engine.RenderEngine$Events", remap = false)
public abstract class EFMRenderGuardMixin {

    @Inject(
            method = "renderLivingEvent", // RenderEngine.java 内の実際のイベントリスナー名
            at = @At("HEAD"),
            cancellable = true
    )
    private static void harmony$guardThirdPartyRender(RenderLivingEvent.Pre<?, ?> event, CallbackInfo ci) {
        try {
            // もし描画対象がプレイヤーであり、
            if (event.getEntity() instanceof Player player) {
                // かつ、レンダラーが標準の PlayerRenderer ではない場合
                // (ガンダムMOD、特撮MOD、GeckoLibモデルなど)
                if (event.getRenderer().getClass() != PlayerRenderer.class) {
                    ci.cancel(); // EFMの全処理をここでストップ！
                    return;
                }

                // NBT救済フラグ
                if (player.getPersistentData().getBoolean("custom_render_active")) {
                    ci.cancel();
                }
            }
        } catch (Exception e) {
            // Iron's Spells Mixin スタイルで安全確保
        }
    }
}