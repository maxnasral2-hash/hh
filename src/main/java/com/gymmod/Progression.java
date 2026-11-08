package com.gymmod;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/** Превращает уровни в атрибуты. Ванильный бой (игроки и мобы) сам учитывает всё это. */
public final class Progression {
    private Progression() {}

    public static void apply(Player p) {
        GymStats s = p.getAttachedOrCreate(GymMod.STATS);
        int str = s.level(GymStats.Stat.STRENGTH);
        int agi = s.level(GymStats.Stat.AGILITY);
        int end = s.level(GymStats.Stat.ENDURANCE);
        int total = s.totalLevel();

        // Сила: урон и скорость копания
        set(p, Attributes.ATTACK_DAMAGE, "str_damage", str * 0.12, Operation.ADD_VALUE);
        set(p, Attributes.BLOCK_BREAK_SPEED, "str_mining", str * 0.03, Operation.ADD_MULTIPLIED_BASE);
        // Ловкость: скорость бега и атаки
        set(p, Attributes.MOVEMENT_SPEED, "agi_speed", agi * 0.004, Operation.ADD_MULTIPLIED_BASE);
        set(p, Attributes.ATTACK_SPEED, "agi_attack", agi * 0.004, Operation.ADD_MULTIPLIED_BASE);
        // Выносливость: здоровье, броня, «непробиваемость»
        set(p, Attributes.MAX_HEALTH, "end_health", end * 0.2, Operation.ADD_VALUE);
        set(p, Attributes.ARMOR, "end_armor", end * 0.1, Operation.ADD_VALUE);
        set(p, Attributes.ARMOR_TOUGHNESS, "end_toughness", end * 0.04, Operation.ADD_VALUE);
        set(p, Attributes.KNOCKBACK_RESISTANCE, "end_knockback", end * 0.008, Operation.ADD_VALUE);
        // Размер: чем больше качок, тем он крупнее (макс. +45%)
        set(p, Attributes.SCALE, "total_scale", Math.min(0.45, total * 0.0015), Operation.ADD_MULTIPLIED_BASE);
    }

    private static void set(Player p, Holder<Attribute> attr, String key, double amount, Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.addOrReplacePermanentModifier(new AttributeModifier(GymMod.id(key), amount, op));
    }
}
