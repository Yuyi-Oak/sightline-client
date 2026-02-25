package top.scfd.sightline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EventBusSubscriber(modid = SightlineClient.MOD_ID, value = Dist.CLIENT)
public final class HudOverlay {
    private static final int PANEL_MARGIN = 8;
    private static final int PANEL_WIDTH_BASE = 156;
    private static final int PANEL_WIDTH_MAX = 256;
    private static final int PANEL_HEIGHT_FULL = 90;
    private static final int PANEL_HEIGHT_COMPACT = 56;
    private static final int PANEL_LINE_HEIGHT = 10;
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
        HandHudState hand = resolveHandState(minecraft);
        AmmoState ammo = hand.ammo();
        WeaponState weapon = hand.weapon();
        Component spectatorLine = spectatorLine(minecraft);
        boolean compact = ClientHotkeys.isHudCompact();
        int panelHeight = compact ? PANEL_HEIGHT_COMPACT : PANEL_HEIGHT_FULL;
        if (spectatorLine != null) {
            panelHeight += PANEL_LINE_HEIGHT;
        }
        int opacityPercent = (int) Math.round((ClientHotkeys.hudOpacityAlpha() / 255.0) * 100.0);
        Component weaponLine = Component.translatable("hud.sightline.weapon", weapon.label());
        Component ammoLine = ammo.asComponent();
        Component layoutLine = Component.translatable(
            "hud.sightline.layout",
            Component.translatable(ClientHotkeys.hudModeTranslationKey()),
            Component.translatable(ClientHotkeys.hudAnchorTranslationKey()),
            opacityPercent
        );
        int panelWidth = resolvePanelWidth(minecraft, compact, weaponLine, ammoLine, layoutLine, spectatorLine);
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        int panelX = switch (ClientHotkeys.hudAnchor()) {
            case TOP_LEFT, BOTTOM_LEFT -> PANEL_MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> Math.max(PANEL_MARGIN, guiWidth - panelWidth - PANEL_MARGIN);
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
        gui.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, panelColor);
        gui.fill(panelX, panelY, panelX + panelWidth, panelY + 10, headerColor);
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
                weaponLine,
                panelX + 4,
                panelY + 34,
                colorForWeapon(weapon),
                false
            );
            gui.drawString(
                minecraft.font,
                ammoLine,
                panelX + 4,
                panelY + 44,
                colorForAmmo(ammo),
                false
            );
            if (spectatorLine != null) {
                gui.drawString(
                    minecraft.font,
                    spectatorLine,
                    panelX + 4,
                    panelY + 54,
                    0x7EE6FF,
                    false
                );
            }
            return;
        }
        gui.drawString(
            minecraft.font,
            weaponLine,
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
            ammoLine,
            panelX + 4,
            panelY + 64,
            colorForAmmo(ammo),
            false
        );
        gui.drawString(
            minecraft.font,
            layoutLine,
            panelX + 4,
            panelY + 74,
            0xD5D5D5,
            false
        );
        if (spectatorLine != null) {
            gui.drawString(
                minecraft.font,
                spectatorLine,
                panelX + 4,
                panelY + 84,
                0x7EE6FF,
                false
            );
        }

    }

    private static int resolvePanelWidth(
        Minecraft minecraft,
        boolean compact,
        Component weaponLine,
        Component ammoLine,
        Component layoutLine,
        Component spectatorLine
    ) {
        int width = PANEL_WIDTH_BASE;
        width = Math.max(width, minecraft.font.width(weaponLine) + 8);
        width = Math.max(width, minecraft.font.width(ammoLine) + 8);
        if (!compact) {
            width = Math.max(width, minecraft.font.width(layoutLine) + 8);
        }
        if (spectatorLine != null) {
            width = Math.max(width, minecraft.font.width(spectatorLine) + 8);
        }
        return Math.min(PANEL_WIDTH_MAX, width);
    }

    private static Component spectatorLine(Minecraft minecraft) {
        if (minecraft.player == null || !minecraft.player.isSpectator()) {
            return null;
        }
        Entity camera = minecraft.getCameraEntity();
        if (camera == null || camera == minecraft.player) {
            return Component.translatable("hud.sightline.spectator.free");
        }
        if (camera instanceof Player targetPlayer) {
            return Component.translatable("hud.sightline.spectator.player", targetPlayer.getName());
        }
        return Component.translatable("hud.sightline.spectator.entity", camera.getName());
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

    private static HandHudState resolveHandState(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.player.getMainHandItem().isEmpty()) {
            return HandHudState.empty();
        }
        String rawName = minecraft.player.getMainHandItem().getHoverName().getString();
        Matcher matcher = AMMO_PATTERN.matcher(rawName);
        if (matcher.matches()) {
            try {
                int mag = Integer.parseInt(matcher.group(1));
                int reserve = Integer.parseInt(matcher.group(2));
                String weaponName = rawName.substring(0, matcher.start()).trim();
                WeaponState weapon = weaponName.isBlank()
                    ? WeaponState.unavailable()
                    : new WeaponState(true, weaponName);
                return new HandHudState(new AmmoState(true, mag, reserve), weapon);
            } catch (NumberFormatException ignored) {
                return new HandHudState(AmmoState.unavailable(), weaponFromRawName(rawName));
            }
        }
        return new HandHudState(AmmoState.unavailable(), weaponFromRawName(rawName));
    }

    private static WeaponState weaponFromRawName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return WeaponState.unavailable();
        }
        return new WeaponState(true, rawName.trim());
    }

    private static void drawReticle(
        Minecraft minecraft,
        int guiWidth,
        int guiHeight,
        net.minecraft.client.gui.GuiGraphics gui,
        AmmoState ammo
    ) {
        if (minecraft.player == null || minecraft.player.isSpectator()) {
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

    private record HandHudState(AmmoState ammo, WeaponState weapon) {
        private static HandHudState empty() {
            return new HandHudState(AmmoState.unavailable(), WeaponState.unavailable());
        }
    }
}
