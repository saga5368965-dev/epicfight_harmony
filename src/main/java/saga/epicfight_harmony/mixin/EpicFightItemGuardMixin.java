package saga.epicfight_harmony.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = EpicFightCapabilities.class, remap = false)
public class EpicFightItemGuardMixin {
    @Unique
    private static final Map<Integer, Long> harmony$brokenItems = new ConcurrentHashMap<>();
    @Unique
    private static final long BROKEN_EXPIRY = 60000L;
    @Unique
    private static long harmony$lastCleanup = 0L;
    @Unique
    private static final long CLEANUP_INTERVAL = 30000L; // 30秒ごと

    @Inject(method = "getItemStackCapability", at = @At("RETURN"), cancellable = true)
    private static void harmony$postCheck(ItemStack stack, CallbackInfoReturnable<CapabilityItem> cir) {
        if (stack == null || stack.isEmpty()) return;

        int itemHash = System.identityHashCode(stack.getItem());
        long currentTime = System.currentTimeMillis();

        Long brokenTime = harmony$brokenItems.get(itemHash);
        if (brokenTime != null) {
            if (currentTime - brokenTime < BROKEN_EXPIRY) {
                cir.setReturnValue(CapabilityItem.EMPTY);
                return;
            }
            harmony$brokenItems.remove(itemHash);
        }

        if (cir.getReturnValue() == null) {
            harmony$brokenItems.put(itemHash, currentTime);
            cir.setReturnValue(CapabilityItem.EMPTY);
        }

        // ★ 改善：クリーンアップは30秒ごとに1回だけ
        if (currentTime - harmony$lastCleanup > CLEANUP_INTERVAL) {
            harmony$brokenItems.entrySet().removeIf(e -> currentTime - e.getValue() > BROKEN_EXPIRY);
            harmony$lastCleanup = currentTime;
        }
    }
}