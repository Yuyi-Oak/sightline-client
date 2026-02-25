package top.scfd.sightline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = SightlineClient.MOD_ID, value = Dist.CLIENT)
public final class HudOverlay {
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_WIDTH = 140;
    private static final int PANEL_HEIGHT_FULL = 68;
    private static final int PANEL_HEIGHT_COMPACT = 34;

    private HudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        if (!ClientHotkeys.isHudEnabled()) {
            return;
        }
        int fps = Minecraft.getInstance().getFps();
        int ping = resolvePing(minecraft);
        int health = (int) Math.ceil(minecraft.player.getHealth());
        int maxHealth = (int) Math.ceil(minecraft.player.getMaxHealth());
        int armor = minecraft.player.getArmorValue();
        boolean compact = ClientHotkeys.isHudCompact();
        int panelHeight = compact ? PANEL_HEIGHT_COMPACT : PANEL_HEIGHT_FULL;
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        int panelX = switch (ClientHotkeys.hudAnchor()) {
            case TOP_LEFT, BOTTOM_LEFT -> PANEL_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> Math.max(PANEL_MARGIN, guiWidth - PANEL_WIDTH - PANEL_MARGIN);
        };
        int panelY = switch (ClientHotkeys.hudAnchor()) {
            case TOP_LEFT, TOP_RIGHT -> PANEL_MARGIN;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> Math.max(PANEL_MARGIN, guiHeight - panelHeight - PANEL_MARGIN);
        };
        int panelAlpha = ClientHotkeys.hudOpacityAlpha();
        int panelColor = (panelAlpha << 24);
        int headerAlpha = Math.min(0xFF, panelAlpha + 0x44);
        int headerColor = (headerAlpha << 24) | 0x202020;

        var gui = event.getGuiGraphics();
        gui.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + panelHeight, panelColor);
        gui.fill(panelX, panelY, panelX + PANEL_WIDTH, panelY + 10, headerColor);
        gui.drawString(minecraft.font, Component.translatable("hud.csmc.title"), panelX + 4, panelY + 2, 0xFFFFFF, false);
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.health", health),
            panelX + 4,
            panelY + 14,
            colorForHealth(health, maxHealth),
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.armor", armor),
            panelX + 4,
            panelY + 24,
            colorForArmor(armor),
            false
        );
        if (compact) {
            return;
        }
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.fps", fps),
            panelX + 4,
            panelY + 34,
            colorForFps(fps),
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.ping", ping),
            panelX + 4,
            panelY + 44,
            colorForPing(ping),
            false
        );
        int opacityPercent = (int) Math.round((ClientHotkeys.hudOpacityAlpha() / 255.0) * 100.0);
        gui.drawString(
            minecraft.font,
            Component.translatable(
                "hud.sightline.layout",
                Component.translatable(ClientHotkeys.hudModeTranslationKey()),
                Component.translatable(ClientHotkeys.hudAnchorTranslationKey()),
                opacityPercent
            ),
            panelX + 4,
            panelY + 54,
            0xD5D5D5,
            false
        );

        if (ClientHotkeys.isReticleEnabled()) {
            drawReticle(guiWidth, guiHeight, gui);
        }
    }

    private static int resolvePing(Minecraft minecraft) {
        if (minecraft.getConnection() == null || minecraft.player == null) {
            return -1;
        }
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    private static int colorForHealth(int health, int maxHealth) {
        if (maxHealth <= 0) {
            return 0xFF6B6B;
        }
        double ratio = (double) Math.max(0, health) / maxHealth;
        if (ratio < 0.3) {
            return 0xFF4D4D;
        }
        if (ratio < 0.6) {
            return 0xFFB347;
        }
        return 0x7DFF8A;
    }

    private static int colorForArmor(int armor) {
        if (armor <= 0) {
            return 0xA0A0A0;
        }
        if (armor < 50) {
            return 0x7AB8FF;
        }
        return 0x50E3FF;
    }

    private static int colorForFps(int fps) {
        if (fps < 30) {
            return 0xFF4D4D;
        }
        if (fps < 60) {
            return 0xFFD166;
        }
        return 0x7DFF8A;
    }

    private static int colorForPing(int ping) {
        if (ping < 0) {
            return 0xA0A0A0;
        }
        if (ping < 60) {
            return 0x7DFF8A;
        }
        if (ping < 120) {
            return 0xFFD166;
        }
        if (ping < 200) {
            return 0xFF9A4D;
        }
        return 0xFF4D4D;
    }

    private static void drawReticle(int guiWidth, int guiHeight, net.minecraft.client.gui.GuiGraphics gui) {
        int centerX = guiWidth / 2;
        int centerY = guiHeight / 2;
        int color = 0xCCFFFFFF;
        gui.fill(centerX - 3, centerY, centerX - 1, centerY + 1, color);
        gui.fill(centerX + 1, centerY, centerX + 3, centerY + 1, color);
        gui.fill(centerX, centerY - 3, centerX + 1, centerY - 1, color);
        gui.fill(centerX, centerY + 1, centerX + 1, centerY + 3, color);
    }
}
