package saga.epicfight_harmony.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import saga.epicfight_harmony.Epicfight_harmony;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

@Mixin(value = EpicFightCapabilities.class, remap = false)
public class EpicFightTimingGuard {
    @Inject(method = "getItemStackCapability", at = @At("HEAD"), cancellable = true)
    private static void harmony$delayCheck(ItemStack stack, CallbackInfoReturnable<CapabilityItem> cir) {
        if (!Epicfight_harmony.isReady()) {
            cir.setReturnValue(CapabilityItem.EMPTY);
        }
    }
}
