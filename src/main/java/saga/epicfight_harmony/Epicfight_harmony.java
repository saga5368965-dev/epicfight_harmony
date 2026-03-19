package saga.epicfight_harmony;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import saga.epicfight_harmony.capability.HarmonyDynamicWeaponCap;
import saga.epicfight_harmony.capability.HarmonyLivingCapability;
import saga.epicfight_harmony.capability.HarmonyPlayerCapability;
import saga.epicfight_harmony.handler.CommonSetupHandler;
import saga.epicfight_harmony.handler.EntityCapabilityHandler;
import saga.epicfight_harmony.handler.SafeCompatibilityHandler; // 追加
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod(Epicfight_harmony.MODID)
public class Epicfight_harmony {
    public static final String MODID = "epicfight_harmony";
    public static final Capability<HarmonyPlayerCapability> PLAYER_DATA_CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public static final Capability<HarmonyLivingCapability> LIVING_DATA_CAP = CapabilityManager.get(new CapabilityToken<>(){});

    private static Epicfight_harmony instance;

    // キャッシュ周り
    private static final Map<Item, WeaponCategory> CATEGORY_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, WeaponCategory> NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Byte> PROCESSED_ITEMS = new ConcurrentHashMap<>();
    private static final Byte PROCESSED = (byte)1;

    public Epicfight_harmony() {
        instance = this;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        saga.epicfight_harmony.network.ModMessages.register();
        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(CommonSetupHandler::onCommonSetup);

        // 各種ハンドラーの登録
        MinecraftForge.EVENT_BUS.register(EntityCapabilityHandler.class);
        MinecraftForge.EVENT_BUS.register(SafeCompatibilityHandler.class);
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(HarmonyPlayerCapability.class);
        event.register(HarmonyLivingCapability.class);
        event.register(HarmonyDynamicWeaponCap.class);
    }
    public WeaponCategory autoDetectCategory(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        Item item = stack.getItem();
        if (PROCESSED_ITEMS.containsKey(item)) {
            return CATEGORY_CACHE.get(item);
        }
        if (stack.hasTag() && stack.getTag().contains("epicfight_category")) {
            try {
                WeaponCategory nbtCat = WeaponCategory.ENUM_MANAGER.get(
                        stack.getTag().getString("epicfight_category").toUpperCase());
                if (nbtCat != null) {
                    cacheResult(item, nbtCat);
                    return nbtCat;
                }
            } catch (Exception ignored) {}
        }
        WeaponCategory result = detect(item, stack);

        if (result != null) {
            cacheResult(item, result);
        } else {
            PROCESSED_ITEMS.put(item, PROCESSED);
        }

        return result;
    }

    private WeaponCategory detect(Item item, ItemStack stack) {
        WeaponCategory result = detectByName(item);
        if (result != null) return result;
        result = detectByClass(item);
        if (result != null) return result;
        return detectByTags(stack);
    }

    private WeaponCategory detectByName(Item item) {
        ResourceLocation res = ForgeRegistries.ITEMS.getKey(item);
        if (res == null) return null;

        String path = res.getPath().toLowerCase();
        return NAME_CACHE.computeIfAbsent(path, p -> {
            if (p.contains("greatsword") || p.contains("claymore") || p.contains("heavy"))
                return CapabilityItem.WeaponCategories.GREATSWORD;
            if (p.contains("katana") || p.contains("uchigatana") || p.contains("tachi"))
                return CapabilityItem.WeaponCategories.UCHIGATANA;
            if (p.contains("spear") || p.contains("pike") || p.contains("lance"))
                return CapabilityItem.WeaponCategories.SPEAR;
            if (p.contains("dagger") || p.contains("knife"))
                return CapabilityItem.WeaponCategories.DAGGER;
            if (p.contains("sword") || p.contains("blade") || p.contains("saber"))
                return CapabilityItem.WeaponCategories.SWORD;
            if (p.contains("axe") || p.contains("halberd"))
                return CapabilityItem.WeaponCategories.AXE;
            return null;
        });
    }

    private WeaponCategory detectByClass(Item item) {
        if (item instanceof SwordItem) return CapabilityItem.WeaponCategories.SWORD;
        if (item instanceof AxeItem) return CapabilityItem.WeaponCategories.AXE;
        return null;
    }

    private WeaponCategory detectByTags(ItemStack stack) {
        return stack.getTags()
                .map(tag -> tag.location().getPath())
                .filter(path -> path.contains("sword") || path.contains("axe"))
                .findFirst()
                .map(path -> path.contains("sword") ?
                        CapabilityItem.WeaponCategories.SWORD :
                        CapabilityItem.WeaponCategories.AXE)
                .orElse(null);
    }

    private void cacheResult(Item item, WeaponCategory category) {
        CATEGORY_CACHE.put(item, category);
        PROCESSED_ITEMS.put(item, PROCESSED);
    }

    public static Epicfight_harmony getInstance() { return instance; }
}