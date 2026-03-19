package saga.epicfight_harmony.util;

import net.minecraftforge.fml.ModList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
public enum ModConflictDetector {
    INSTANCE;
    public static final String EPIC_FIGHT = "epicfight";
    public static final String IRON_SPELLS = "irons_spells_js";
    private final Map<String, Boolean> modPresenceCache = new HashMap<>();
    private final Map<String, ConflictHandler> conflictHandlers = new HashMap<>();

    public interface ConflictHandler {
        void handleConflict(String modId);
    }

    public void registerConflictHandler(String modId, ConflictHandler handler) {
        conflictHandlers.put(modId, handler);
    }
    public boolean isModPresent(String modId) {
        return modPresenceCache.computeIfAbsent(modId, id -> {
            boolean present = ModList.get().isLoaded(id);
            if (present) {
                ConflictHandler handler = conflictHandlers.get(id);
                if (handler != null) {
                    handler.handleConflict(id);
                }
            }
            return present;
        });
    }
    public <T> T runIfSafe(String conflictingModId, Supplier<T> action, Supplier<T> fallback) {
        if (!isModPresent(conflictingModId)) {
            return action.get();
        }
        return fallback.get();
    }

    public void runIfSafe(String conflictingModId, Runnable action) {
        if (!isModPresent(conflictingModId)) {
            action.run();
        }
    }
}

