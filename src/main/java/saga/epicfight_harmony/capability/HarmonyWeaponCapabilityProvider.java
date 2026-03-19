package saga.epicfight_harmony.capability;

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
        this.instance = LazyOptional.of(() -> new HarmonyDynamicWeaponCap(stack, initialCategory));
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        Capability<CapabilityItem> efmCap = CapabilityManager.get(new CapabilityToken<>() {});
        return cap == efmCap ? instance.cast() : LazyOptional.empty();
    }
}