package top.scfd.sightline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = SightlineClient.MOD_ID, value = Dist.CLIENT)
public final class HudOverlay {
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_WIDTH = 156;
    private static final int PANEL_HEIGHT_FULL = 90;
    private static final int PANEL_HEIGHT_COMPACT = 46;
    private static final Pattern AMMO_PATTERN = Pattern.compile(".*\\[(\\d+)/(\\d+)]\\s*$");

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
        AmmoState ammo = resolveAmmo(minecraft);
        WeaponState weapon = resolveWeapon(minecraft);
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
        if (ClientHotkeys.isReticleEnabled()) {
            drawReticle(minecraft, guiWidth, guiHeight, gui, ammo);
        }
        if (compact) {
            gui.drawString(
                minecraft.font,
                ammo.asComponent(),
                panelX + 4,
                panelY + 34,
                colorForAmmo(ammo),
                false
            );
            return;
        }
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.weapon", weapon.label()),
            panelX + 4,
            panelY + 34,
            colorForWeapon(weapon),
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.fps", fps),
            panelX + 4,
            panelY + 44,
            colorForFps(fps),
            false
        );
        gui.drawString(
            minecraft.font,
            Component.translatable("hud.sightline.ping", ping),
            panelX + 4,
            panelY + 54,
            colorForPing(ping),
            false
        );
        gui.drawString(
            minecraft.font,
            ammo.asComponent(),
            panelX + 4,
            panelY + 64,
            colorForAmmo(ammo),
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
            panelY + 74,
            0xD5D5D5,
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

    private static int colorForAmmo(AmmoState ammo) {
        if (!ammo.available()) {
            return 0xA0A0A0;
        }
        if (ammo.magazine() <= 0) {
            return 0xFF4D4D;
        }
        if (ammo.magazine() <= 5) {
            return 0xFFD166;
        }
        return 0xFFFFFF;
    }

    private static int colorForWeapon(WeaponState weapon) {
        return weapon.available() ? 0xFFFFFF : 0xA0A0A0;
    }

    private static AmmoState resolveAmmo(Minecraft minecraft) {
        if (minecraft.player == null) {
            return AmmoState.unavailable();
        }
        String name = minecraft.player.getMainHandItem().getHoverName().getString();
        Matcher matcher = AMMO_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return AmmoState.unavailable();
        }
        try {
            int mag = Integer.parseInt(matcher.group(1));
            int reserve = Integer.parseInt(matcher.group(2));
            return new AmmoState(true, mag, reserve);
        } catch (NumberFormatException ignored) {
            return AmmoState.unavailable();
        }
    }

    private static WeaponState resolveWeapon(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.getMainHandItem().isEmpty()) {
            return WeaponState.unavailable();
        }
        String rawName = minecraft.player.getMainHandItem().getHoverName().getString();
        String withoutAmmo = rawName.replaceAll("\\s*\\[(\\d+)/(\\d+)]\\s*$", "").trim();
        if (withoutAmmo.isBlank()) {
            return WeaponState.unavailable();
        }
        return new WeaponState(true, withoutAmmo);
    }

    private static void drawReticle(
        Minecraft minecraft,
        int guiWidth,
        int guiHeight,
        net.minecraft.client.gui.GuiGraphics gui,
        AmmoState ammo
    ) {
        if (minecraft.player == null) {
            return;
        }
        var velocity = minecraft.player.getDeltaMovement();
        double horizontalSpeed = Math.hypot(velocity.x, velocity.z);
        double verticalSpeed = Math.abs(velocity.y);
        boolean airborneLike = verticalSpeed > 0.08;
        boolean lowAmmo = ammo.available() && ammo.magazine() > 0 && ammo.magazine() <= 5;
        boolean emptyAmmo = ammo.available() && ammo.magazine() <= 0;
        int gap = 2 + (int) Math.min(8.0, horizontalSpeed * 20.0 + (airborneLike ? 3.0 : 0.0));
        int length = 3;

        int centerX = guiWidth / 2;
        int centerY = guiHeight / 2;
        int color;
        if (emptyAmmo) {
            color = 0xCCFF4D4D;
        } else if (lowAmmo) {
            color = 0xCCFFD166;
        } else if (airborneLike) {
            color = 0xCCFF8A66;
        } else if (horizontalSpeed > 0.10) {
            color = 0xCCECD06F;
        } else {
            color = 0xCCFFFFFF;
        }
        gui.fill(centerX - gap - length, centerY, centerX - gap, centerY + 1, color);
        gui.fill(centerX + gap, centerY, centerX + gap + length, centerY + 1, color);
        gui.fill(centerX, centerY - gap - length, centerX + 1, centerY - gap, color);
        gui.fill(centerX, centerY + gap, centerX + 1, centerY + gap + length, color);
    }

    private record AmmoState(boolean available, int magazine, int reserve) {
        private static AmmoState unavailable() {
            return new AmmoState(false, 0, 0);
        }

        private Component asComponent() {
            if (!available) {
                return Component.translatable("hud.sightline.ammo.none");
            }
            return Component.translatable("hud.sightline.ammo", magazine, reserve);
        }
    }

    private record WeaponState(boolean available, String label) {
        private static WeaponState unavailable() {
            return new WeaponState(false, "--");
        }
    }
}
