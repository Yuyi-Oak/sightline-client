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
    private static final int PANEL_X = 8;
    private static final int PANEL_Y = 8;
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

        var gui = event.getGuiGraphics();
        gui.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + panelHeight, 0x88000000);
        gui.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + 10, 0xCC202020);
        gui.drawString(minecraft.font, Component.translatable("hud.csmc.title"), PANEL_X + 4, PANEL_Y + 2, 0xFFFFFF, false);
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.health", health),
            PANEL_X + 4,
            PANEL_Y + 14,
            0xFF6B6B,
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.armor", armor),
            PANEL_X + 4,
            PANEL_Y + 24,
            0x6BCBFF,
            false
        );
        if (compact) {
            return;
        }
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.fps", fps),
            PANEL_X + 4,
            PANEL_Y + 34,
            0xC8FF72,
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.ping", ping),
            PANEL_X + 4,
            PANEL_Y + 44,
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
