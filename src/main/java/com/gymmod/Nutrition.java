package com.gymmod;

import java.util.Map;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class Nutrition {
    private Nutrition() {}

    public record Macros(int protein, int carbs, int fat) {
        public int value() { return protein + carbs + fat; }
    }

    private static final Map<String, Macros> TABLE = Map.ofEntries(
            Map.entry("minecraft:cooked_beef", new Macros(26, 0, 8)),
            Map.entry("minecraft:cooked_porkchop", new Macros(24, 0, 10)),
            Map.entry("minecraft:cooked_chicken", new Macros(25, 0, 4)),
            Map.entry("minecraft:cooked_mutton", new Macros(22, 0, 8)),
            Map.entry("minecraft:cooked_rabbit", new Macros(22, 0, 3)),
            Map.entry("minecraft:cooked_salmon", new Macros(22, 0, 10)),
            Map.entry("minecraft:cooked_cod", new Macros(20, 0, 2)),
            Map.entry("minecraft:beef", new Macros(14, 0, 5)),
            Map.entry("minecraft:chicken", new Macros(12, 0, 3)),
            Map.entry("minecraft:baked_potato", new Macros(3, 25, 0)),
            Map.entry("minecraft:bread", new Macros(4, 22, 2)),
            Map.entry("minecraft:carrot", new Macros(1, 8, 0)),
            Map.entry("minecraft:golden_carrot", new Macros(1, 14, 0)),
            Map.entry("minecraft:apple", new Macros(0, 12, 0)),
            Map.entry("minecraft:sweet_berries", new Macros(0, 6, 0)),
            Map.entry("minecraft:melon_slice", new Macros(0, 5, 0)),
            Map.entry("minecraft:beetroot_soup", new Macros(2, 12, 0)),
            Map.entry("minecraft:mushroom_stew", new Macros(3, 10, 2)),
            Map.entry("minecraft:rabbit_stew", new Macros(14, 14, 4)),
            Map.entry("minecraft:cookie", new Macros(1, 12, 5)),
            Map.entry("minecraft:pumpkin_pie", new Macros(3, 20, 8)),
            Map.entry("minecraft:honey_bottle", new Macros(0, 22, 0)),
            Map.entry("minecraft:dried_kelp", new Macros(1, 3, 0)),
            Map.entry("minecraft:rotten_flesh", new Macros(4, 0, 6)),
            // свои блюда
            Map.entry("gymmod:protein_shake", new Macros(30, 10, 2)),
            Map.entry("gymmod:power_bowl", new Macros(30, 35, 8)),
            Map.entry("gymmod:oat_bar", new Macros(6, 22, 6))
    );

    /** КБЖУ предмета или null, если это не еда. Неизвестную еду считаем по её сытности. */
    public static Macros of(ItemStack stack) {
        var food = stack.get(DataComponents.FOOD);
        if (food == null) return null;
        Macros m = TABLE.get(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        if (m != null) return m;
        int n = food.nutrition();
        return new Macros(n / 2, n * 2, n / 2);
    }

    /** Шкала ценности ▮▮▮▯▯▯▯▯▯▯ */
    public static String bar(Macros m) {
        int filled = Math.max(1, Math.min(10, Math.round(m.value() / 4f)));
        return "▮".repeat(filled) + "▯".repeat(10 - filled);
    }
}
