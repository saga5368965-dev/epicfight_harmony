package saga.epicfight_harmony.handler;

import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;

public class CommonSetupHandler {
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            System.out.println("EFH: Initializing Network Messages...");
            try {
                System.out.println("EFH: Network Messages Registered Successfully.");
            } catch (Exception e) {
                System.err.println("EFH: Network registration failed: " + e.getMessage());
            }
        });
    }
    public static void onInterModEnqueue() {
        InterModComms.sendTo("epicfight", "harmony_bridge_status", () -> "active");
    }
    public static void onInterModProcess(InterModProcessEvent event) {
        event.getIMCStream().forEach(message -> {
            if ("epicfight".equals(message.getSenderModId())) {
                System.out.println("EFH: Handshake with Epic Fight established.");
            }
        });
    }
}