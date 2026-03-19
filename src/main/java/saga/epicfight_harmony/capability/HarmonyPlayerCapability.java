package saga.epicfight_harmony.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public class HarmonyPlayerCapability implements INBTSerializable<CompoundTag> {
    private final Player player;
    private boolean customRenderActive = false;
    private String modelId = "default";
    private boolean dirty = false;
    private int lastSyncTick = 0;

    public HarmonyPlayerCapability(Player player) {
        this.player = player;
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("active", this.customRenderActive);
        nbt.putString("model", this.modelId != null ? this.modelId : "default");
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt == null) return;
        this.customRenderActive = nbt.getBoolean("active");
        this.modelId = nbt.contains("model") ? nbt.getString("model") : "default";
        this.dirty = false;
    }

    public void setCustomRenderActive(boolean active) {
        if (this.customRenderActive != active) {
            this.customRenderActive = active;
            this.dirty = true;
        }
    }

    public void setModelId(String modelId) {
        if (modelId != null && !modelId.equals(this.modelId)) {
            this.modelId = modelId;
            this.dirty = true;
        }
    }

    public @NotNull String getModelId() {
        return modelId != null ? modelId : "default";
    }

    public boolean isProtected() {
        return customRenderActive;
    }

    public boolean needsSync() {
        return dirty || (player.tickCount - lastSyncTick > 100);
    }

    public void markSynced() {
        this.dirty = false;
        this.lastSyncTick = player.tickCount;
    }
}