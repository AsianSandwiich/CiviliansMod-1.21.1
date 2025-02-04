package net.asian.civiliansmod;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class ModItems {
    // Identifier for the NPC Totem item with enchant glint
    public static final Item NPC_TOTEM = registerItem("npctotem", settings -> new Item(settings.maxCount(3)) { // Limit stack size to 3
        @Override
        public boolean hasGlint(ItemStack stack) {
            return true; // Force the enchant glint effect
        }
    });

    // Define a custom item group
    public static final RegistryKey<ItemGroup> CIVILIANS_MOD_GROUP = RegistryKey.of(Registries.ITEM_GROUP.getKey(),
            Identifier.of(CiviliansMod.MOD_ID, "civilians_mod_group")); // Custom identifier for the item group

    private static Item registerItem(String id, Function<Item.Settings, Item> factory) {
        return Registry.register(Registries.ITEM, Identifier.of(CiviliansMod.MOD_ID, id), factory.apply(new Item.Settings().registryKey(keyOf(id))));
    }

    private static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CiviliansMod.MOD_ID, id));
    }

    public static void registerModItems() {
        // Register the custom creative tab (item group)
        Registry.register(Registries.ITEM_GROUP, CIVILIANS_MOD_GROUP,
                FabricItemGroup.builder()
                        .displayName(Text.literal("Civilians Mod")) // Name of the creative tab
                        .icon(() -> new ItemStack(NPC_TOTEM)) // Icon for the creative tab
                        .entries((enabledFeatures, entries) -> entries.add(NPC_TOTEM)) // Add items here
                        .build()
        );

        // Log successful registration
        CiviliansMod.LOGGER.info("[CiviliansMod] Registered Mod Items and Item Group!");
    }
}