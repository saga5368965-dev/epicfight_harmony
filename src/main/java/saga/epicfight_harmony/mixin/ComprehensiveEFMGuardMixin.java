package saga.epicfight_harmony.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import saga.epicfight_harmony.Epicfight_harmony;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(targets = "yesman.epicfight.client.events.engine.RenderEngine$Events", remap = false)
public abstract class ComprehensiveEFMGuardMixin {

    @Unique
    private static final AtomicBoolean harmony$inProcessing = new AtomicBoolean(false);
    @Unique
    private static final Map<Player, Long> harmony$cache = new WeakHashMap<>();

    @Unique
    private static final long CHECK_INTERVAL_MS = 500L;

    @Inject(method = "renderLivingEvent", at = @At("HEAD"), cancellable = true)
    private static void harmony$safeUniversalGuard(RenderLivingEvent.Pre<?, ?> event, CallbackInfo ci) {
        if (harmony$inProcessing.get() || event == null || event.isCanceled() ||
                !(event.getEntity() instanceof Player player)) {
            return;
        }
        if (!harmony$inProcessing.compareAndSet(false, true)) {
            return;
        }

        try {
            long now = System.currentTimeMillis();
            Long packedData = harmony$cache.get(player);
            if (packedData != null) {
                long timestamp = packedData & Long.MAX_VALUE;
                boolean isProtected = packedData < 0;

                if (now - timestamp < CHECK_INTERVAL_MS) {
                    if (isProtected) ci.cancel();
                    return;
                }
            }
            boolean protectedMode = false;
            try {
                protectedMode = player.getCapability(Epicfight_harmony.PLAYER_DATA_CAP)
                        .map(cap -> {
                            try { return cap.isProtected(); }
                            catch (Throwable t) { return false; }
                        })
                        .orElse(false);
            } catch (Throwable t) {
                protectedMode = false;
            }
            long dataToPack = now & Long.MAX_VALUE;
            if (protectedMode) {
                dataToPack |= Long.MIN_VALUE;
            }
            harmony$cache.put(player, dataToPack);

            if (protectedMode) {
                ci.cancel();
            }

        } catch (Throwable t) {
        } finally {
            harmony$inProcessing.set(false);
        }
    }
}