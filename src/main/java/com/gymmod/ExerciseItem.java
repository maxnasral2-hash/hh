package com.gymmod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Снаряд: ПКМ — подход. Нужна еда в животе, тратит сытость, опыт зависит от диеты. */
public class ExerciseItem extends Item {
    public final GymStats.Stat stat;
    public final int baseXp;
    public final int cooldownTicks;
    public final float exhaustion;

    public ExerciseItem(Properties props, GymStats.Stat stat, int baseXp, int cooldownTicks, float exhaustion) {
        super(props);
        this.stat = stat;
        this.baseXp = baseXp;
        this.cooldownTicks = cooldownTicks;
        this.exhaustion = exhaustion;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide() || !(player instanceof ServerPlayer sp)) return InteractionResult.SUCCESS;

        if (sp.getFoodData().getFoodLevel() < 6) {
            sp.displayClientMessage(Component.translatable("gymmod.msg.too_hungry"), true);
            return InteractionResult.FAIL;
        }

        GymStats before = sp.getAttachedOrCreate(GymMod.STATS);
        int lvlBefore = before.level(stat);
        int gained = Math.max(1, (int) Math.round(baseXp * before.xpMultiplier()));
        GymStats after = before.withXp(stat, gained);
        sp.setAttached(GymMod.STATS, after);

        sp.causeFoodExhaustion(exhaustion);
        sp.getCooldowns().addCooldown(sp.getItemInHand(hand), cooldownTicks);

        int lvlAfter = after.level(stat);
        if (lvlAfter > lvlBefore) {
            Progression.apply(sp);
            sp.displayClientMessage(Component.translatable("gymmod.msg.level_up", Component.translatable(stat.key), lvlAfter), true);
        } else {
            sp.displayClientMessage(Component.translatable("gymmod.msg.gain", Component.translatable(stat.key), gained,
                    String.format("%.2f", before.xpMultiplier())), true);
        }
        return InteractionResult.SUCCESS;
    }
}
