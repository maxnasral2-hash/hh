package com.gymmod;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Опыт трёх статов + «скользящая» диета (белки/углеводы/жиры). Хранится на игроке и переживает смерть. */
public record GymStats(int strXp, int agiXp, int endXp, double protein, double carbs, double fat) {

    public static final int MAX_LEVEL = 100;
    public static final GymStats EMPTY = new GymStats(0, 0, 0, 0, 0, 0);

    public static final Codec<GymStats> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("str", 0).forGetter(GymStats::strXp),
            Codec.INT.optionalFieldOf("agi", 0).forGetter(GymStats::agiXp),
            Codec.INT.optionalFieldOf("end", 0).forGetter(GymStats::endXp),
            Codec.DOUBLE.optionalFieldOf("protein", 0.0).forGetter(GymStats::protein),
            Codec.DOUBLE.optionalFieldOf("carbs", 0.0).forGetter(GymStats::carbs),
            Codec.DOUBLE.optionalFieldOf("fat", 0.0).forGetter(GymStats::fat)
    ).apply(i, GymStats::new));

    public enum Stat {
        STRENGTH("gymmod.stat.strength"), AGILITY("gymmod.stat.agility"), ENDURANCE("gymmod.stat.endurance");
        public final String key;
        Stat(String key) { this.key = key; }
    }

    public static int levelOf(int xp) { return Math.min(MAX_LEVEL, (int) Math.sqrt(xp / 12.0)); }

    public int xp(Stat s) { return switch (s) { case STRENGTH -> strXp; case AGILITY -> agiXp; case ENDURANCE -> endXp; }; }
    public int level(Stat s) { return levelOf(xp(s)); }
    public int totalLevel() { return level(Stat.STRENGTH) + level(Stat.AGILITY) + level(Stat.ENDURANCE); }

    public GymStats withXp(Stat s, int add) {
        return switch (s) {
            case STRENGTH -> new GymStats(strXp + add, agiXp, endXp, protein, carbs, fat);
            case AGILITY -> new GymStats(strXp, agiXp + add, endXp, protein, carbs, fat);
            case ENDURANCE -> new GymStats(strXp, agiXp, endXp + add, protein, carbs, fat);
        };
    }

    /** Новая еда: старая диета «забывается» на 20%, новая добавляется. */
    public GymStats withMeal(Nutrition.Macros m) {
        return new GymStats(strXp, agiXp, endXp, protein * 0.8 + m.protein(), carbs * 0.8 + m.carbs(), fat * 0.8 + m.fat());
    }

    /** 0..1 — насколько баланс белки/углеводы/жиры близок к идеалу 35/45/20. */
    public double dietQuality() {
        double total = protein + carbs + fat;
        if (total < 1) return -1; // данных ещё нет
        double err = Math.abs(protein / total - 0.35) + Math.abs(carbs / total - 0.45) + Math.abs(fat / total - 0.20);
        return Math.max(0, 1 - err / 0.9);
    }

    /** Множитель опыта от питания: 0.5x (одни печеньки) … 2.0x (идеальный баланс). */
    public double xpMultiplier() {
        double q = dietQuality();
        return q < 0 ? 1.0 : 0.5 + 1.5 * q;
    }
}
