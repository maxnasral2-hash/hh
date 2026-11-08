package com.gymmod;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumables;

public final class ModItems {
    private ModItems() {}

    public static final Item DUMBBELL = reg("dumbbell",
            p -> new ExerciseItem(p, GymStats.Stat.STRENGTH, 10, 20, 1.0f), new Item.Properties().stacksTo(1));
    public static final Item BARBELL = reg("barbell",
            p -> new ExerciseItem(p, GymStats.Stat.STRENGTH, 25, 40, 2.5f), new Item.Properties().stacksTo(1));
    public static final Item JUMP_ROPE = reg("jump_rope",
            p -> new ExerciseItem(p, GymStats.Stat.AGILITY, 12, 15, 1.2f), new Item.Properties().stacksTo(1));
    public static final Item KETTLEBELL = reg("kettlebell",
            p -> new ExerciseItem(p, GymStats.Stat.ENDURANCE, 15, 30, 1.8f), new Item.Properties().stacksTo(1));

    public static final Item PROTEIN_SHAKE = reg("protein_shake", Item::new,
            new Item.Properties().stacksTo(16).food(
                    new FoodProperties.Builder().nutrition(5).saturationModifier(0.8f).alwaysEdible().build(),
                    Consumables.DEFAULT_DRINK));
    public static final Item POWER_BOWL = reg("power_bowl", Item::new,
            new Item.Properties().stacksTo(16).food(
                    new FoodProperties.Builder().nutrition(10).saturationModifier(1.0f).build()));
    public static final Item OAT_BAR = reg("oat_bar", Item::new,
            new Item.Properties().food(
                    new FoodProperties.Builder().nutrition(4).saturationModifier(0.6f).build()));

    private static <T extends Item> T reg(String name, Function<Item.Properties, T> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, GymMod.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(props.setId(key)));
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(e -> {
            e.accept(DUMBBELL); e.accept(BARBELL); e.accept(JUMP_ROPE); e.accept(KETTLEBELL);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FOOD_AND_DRINKS).register(e -> {
            e.accept(PROTEIN_SHAKE); e.accept(POWER_BOWL); e.accept(OAT_BAR);
        });
    }
}
