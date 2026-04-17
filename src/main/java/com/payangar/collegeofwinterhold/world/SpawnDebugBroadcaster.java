package com.payangar.collegeofwinterhold.world;

import com.payangar.collegeofwinterhold.config.ModServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * Optional debug aid gated by {@code debug.spawnAnnouncements}: announces
 * group spawns in chat with a clickable TP link (mirroring vanilla
 * {@code /locate}) and tags every member with a short Glowing effect so
 * testers can find freshly-spawned groups from far away.
 */
public final class SpawnDebugBroadcaster {

    private static final int GLOW_TICKS = 600;

    private SpawnDebugBroadcaster() {}

    public static boolean isEnabled() {
        return ModServerConfig.DEBUG_SPAWN_ANNOUNCEMENTS.get();
    }

    /**
     * Broadcasts a spawn announcement to every player on the server. The
     * coordinates are a bracketed, clickable component that suggests
     * {@code /tp @s X Y Z} — same UX as vanilla {@code /locate}.
     */
    public static void announce(ServerLevel server, String label, BlockPos pos) {
        if (!isEnabled()) return;

        String tpCommand = "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        MutableComponent coords = ComponentUtils.wrapInSquareBrackets(
                Component.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ())
        ).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, tpCommand))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("chat.coordinates.tooltip")))
        );

        MutableComponent message = Component.literal("[CoW debug] ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(label + " at ").withStyle(ChatFormatting.GRAY))
                .append(coords);

        server.getServer().getPlayerList().broadcastSystemMessage(message, false);
    }

    /** Applies a 30 s Glowing effect. Hidden from the HUD/particles (debug-only). */
    public static void glow(LivingEntity entity) {
        if (!isEnabled()) return;
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_TICKS, 0, false, false));
    }
}
