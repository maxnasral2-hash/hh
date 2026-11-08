package com.gymmod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GymMod implements ModInitializer {
    public static final String MOD_ID = "gymmod";

    public static final AttachmentType<GymStats> STATS = AttachmentRegistry.create(id("stats"), b -> b
            .initializer(() -> GymStats.EMPTY)
            .persistent(GymStats.CODEC)
            .copyOnDeath());

    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MOD_ID, path); }

    // кто сейчас доедает еду (чтобы поймать момент «съел»)
    private final Map<UUID, ItemStack> eating = new HashMap<>();

    @Override
    public void onInitialize() {
        ModItems.init();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Progression.apply(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldP, newP, alive) -> Progression.apply(newP));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                UUID id = p.getUUID();
                if (p.isUsingItem() && p.getUseItem().has(DataComponents.FOOD)) {
                    if (p.getUseItemRemainingTicks() <= 1) eating.put(id, p.getUseItem().copy());
                    else eating.remove(id);
                } else {
                    ItemStack done = eating.remove(id);
                    if (done != null) onEat(p, done);
                }
            }
        });

        // Индикатор питательной ценности на еде + описание снарядов
        ItemTooltipCallback.EVENT.register((stack, ctx, flag, lines) -> {
            Nutrition.Macros m = Nutrition.of(stack);
            if (m != null) {
                lines.add(Component.translatable("gymmod.tooltip.macros", m.protein(), m.carbs(), m.fat()));
                lines.add(Component.translatable("gymmod.tooltip.value", Nutrition.bar(m), m.value()));
            }
            if (stack.getItem() instanceof ExerciseItem e) {
                lines.add(Component.translatable("gymmod.tooltip.trains", Component.translatable(e.stat.key), e.baseXp));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) ->
                dispatcher.register(Commands.literal("gym").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    GymStats s = p.getAttachedOrCreate(STATS);
                    double q = s.dietQuality();
                    ctx.getSource().sendSuccess(() -> Component.translatable("gymmod.cmd.stats",
                            s.level(GymStats.Stat.STRENGTH), s.level(GymStats.Stat.AGILITY), s.level(GymStats.Stat.ENDURANCE),
                            q < 0 ? "—" : Math.round(q * 100) + "%", String.format("x%.2f", s.xpMultiplier())), false);
                    return 1;
                })));
    }

    private void onEat(ServerPlayer p, ItemStack eaten) {
        Nutrition.Macros m = Nutrition.of(eaten);
        if (m == null) return;
        GymStats s = p.getAttachedOrCreate(STATS).withMeal(m);
        p.setAttached(STATS, s);
        p.displayClientMessage(Component.translatable("gymmod.msg.ate", String.format("x%.2f", s.xpMultiplier())), true);
    }
}
