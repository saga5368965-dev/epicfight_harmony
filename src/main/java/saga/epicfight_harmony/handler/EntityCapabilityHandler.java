package saga.epicfight_harmony.handler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import saga.epicfight_harmony.Epicfight_harmony;
import saga.epicfight_harmony.capability.HarmonyLivingCapabilityProvider;
import saga.epicfight_harmony.capability.HarmonyPlayerCapabilityProvider;
import saga.epicfight_harmony.network.SyncPlayerDataPacket;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mod.EventBusSubscriber(modid = Epicfight_harmony.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityCapabilityHandler {
    private static final ResourceLocation PLAYER_DATA_KEY = new ResourceLocation(Epicfight_harmony.MODID, "player_data");
    private static final ResourceLocation LIVING_DATA_KEY = new ResourceLocation(Epicfight_harmony.MODID, "living_data");
    private static final ResourceLocation EFM_ENTITY_CAP_ID = new ResourceLocation("epicfight", "entitydata");
    private static final byte FLAG_PLAYER = 1;
    private static final byte FLAG_LIVING = 2;
    private static final byte FLAG_BOTH = 3;
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
        if (!(entity instanceof LivingEntity livingEntity)) return;
        if (event.getCapabilities().containsKey(EFM_ENTITY_CAP_ID) || hasEpicFightEntityCap(livingEntity)) {
            return;
        }
        if (entity instanceof Player player) {
            if (!event.getCapabilities().containsKey(PLAYER_DATA_KEY)) {
                event.addCapability(PLAYER_DATA_KEY, new HarmonyPlayerCapabilityProvider(player));
            }
        } else {
            if (!event.getCapabilities().containsKey(LIVING_DATA_KEY)) {
                event.addCapability(LIVING_DATA_KEY, new HarmonyLivingCapabilityProvider(livingEntity));
            }
        }
    }

    private static boolean hasEpicFightEntityCap(LivingEntity entity) {
        try {
            return entity.getCapability(EpicFightCapabilities.CAPABILITY_ENTITY).isPresent();
        } catch (Throwable t) {
            return false;
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(oldCap -> {
            event.getEntity().getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(newCap -> {
                newCap.deserializeNBT(oldCap.serializeNBT());
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        SyncPlayerDataPacket.syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        SyncPlayerDataPacket.syncToClient(event.getEntity());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof Player targetPlayer) {
            SyncPlayerDataPacket.syncToAllTracking(targetPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
            if (event.player.tickCount % 100 == 0) {
                event.player.getCapability(Epicfight_harmony.PLAYER_DATA_CAP).ifPresent(cap -> {
                    if (cap.needsSync()) {
                        SyncPlayerDataPacket.syncToAllTracking(event.player);
                        cap.markSynced();
                    }
                });
            }
        }
    }
}