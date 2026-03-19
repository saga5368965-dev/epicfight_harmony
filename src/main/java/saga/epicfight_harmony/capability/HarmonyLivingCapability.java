package saga.epicfight_harmony.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.INBTSerializable;

public class HarmonyLivingCapability implements INBTSerializable<CompoundTag> {
    private final LivingEntity entity;
    private boolean isSpecialMob = false;

    public HarmonyLivingCapability(LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("isSpecial", isSpecialMob);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.isSpecialMob = nbt.getBoolean("isSpecial");
    }

    public boolean isSpecialMob() { return isSpecialMob; }
    public void setSpecialMob(boolean value) { this.isSpecialMob = value; }
}