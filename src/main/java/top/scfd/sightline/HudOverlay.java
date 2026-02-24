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
    private static final int PANEL_HEIGHT_FULL = 58;
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
            0xFF6B6B,
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.armor", armor),
            panelX + 4,
            panelY + 24,
            0x6BCBFF,
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
            0xC8FF72,
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.ping", ping),
            panelX + 4,
            panelY + 44,
            0xFFD166,
            false
        );
    }

    private static int resolvePing(Minecraft minecraft) {
        if (minecraft.getConnection() == null || minecraft.player == null) {
            return -1;
        }
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }
}
