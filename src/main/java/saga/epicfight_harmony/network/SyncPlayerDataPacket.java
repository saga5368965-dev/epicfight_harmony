package saga.epicfight_harmony.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import saga.epicfight_harmony.Epicfight_harmony;

import java.util.function.Supplier;
public class SyncPlayerDataPacket {
    private final CompoundTag data;
    private final int playerId;

    public SyncPlayerDataPacket(Player player, CompoundTag data) {
        this.playerId = player.getId();
        this.data = data;
    }

    public SyncPlayerDataPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.data = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeNbt(data);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getDirection().getReceptionSide().isClient()) {
                handleClient();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    
    private void handleClient() {
        var mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level != null) {
            if (mc.level.getEntity(playerId) instanceof Player player) {
                player.getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(cap -> {
                    cap.deserializeNBT(this.data);
                });
            }
        }
    }
    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            player.getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(cap -> {
                CompoundTag tag = cap.serializeNBT();
                ModMessages.sendToPlayer(new SyncPlayerDataPacket(player, tag), serverPlayer);
            });
        }
    }
    public static void syncToAllTracking(Player player) {
        if (!player.level().isClientSide) {
            player.getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(cap -> {
                CompoundTag tag = cap.serializeNBT();
                ModMessages.sendToAllTracking(new SyncPlayerDataPacket(player, tag), player);
            });
        }
    }
}
