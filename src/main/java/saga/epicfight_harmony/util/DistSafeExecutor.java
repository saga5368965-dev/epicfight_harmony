package saga.epicfight_harmony.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.function.Supplier;

public class DistSafeExecutor {

    public static final boolean IS_CLIENT = FMLEnvironment.dist == Dist.CLIENT;
    private static Boolean isEFMClientCached = null;

    public static void runWhenClient(Supplier<Runnable> clientTask) {
        if (IS_CLIENT) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, clientTask);
        }
    }

    public static <T> T getSafeValue(Dist dist, Supplier<Supplier<T>> supplier, Supplier<T> fallback) {
        if (FMLEnvironment.dist == dist) {
            try {
                return supplier.get().get();
            } catch (Throwable t) {
                return fallback.get();
            }
        }
        return fallback.get();
    }

    public static Level getClientLevel() {
        if (!IS_CLIENT) return null;
        return getSafeValue(Dist.CLIENT, () -> () -> Minecraft.getInstance().level, () -> null);
    }

    public static class EpicFightSafeCaller {

        @OnlyIn(Dist.CLIENT)
        private static boolean checkClientRenderStatus() {
            if (isEFMClientCached != null) return isEFMClientCached;
            try {
                Class.forName("yesman.epicfight.client.events.engine.RenderEngine", false, DistSafeExecutor.class.getClassLoader());
                isEFMClientCached = true;
            } catch (Throwable e) {
                isEFMClientCached = false;
            }
            return isEFMClientCached;
        }

        public static boolean isEpicFightRenderActive() {
            if (!IS_CLIENT) return false;
            return getSafeValue(Dist.CLIENT,
                    () -> () -> checkClientRenderStatus(),
                    () -> false
            );
        }
    }
}
