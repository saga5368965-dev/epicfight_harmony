package saga.epicfight_harmony.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HarmonyLivingCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
    private final HarmonyLivingCapability backend;
    private final LazyOptional<HarmonyLivingCapability> optional;

    public HarmonyLivingCapabilityProvider(LivingEntity entity) {
        this.backend = new HarmonyLivingCapability(entity);
        this.optional = LazyOptional.of(() -> backend);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() { return backend.serializeNBT(); }

    @Override
    public void deserializeNBT(CompoundTag nbt) { backend.deserializeNBT(nbt); }
}
