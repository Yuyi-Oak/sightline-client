package top.scfd.sightline;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientHotkeys {
    public enum HudAnchor {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(
        Identifier.fromNamespaceAndPath(SightlineClient.MOD_ID, "sightline")
    );
    private static final KeyMapping TOGGLE_HUD = new KeyMapping(
        "key.sightline.toggle_hud",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_H,
        CATEGORY
    );
    private static final KeyMapping TOGGLE_CAMERA_VIEW = new KeyMapping(
        "key.sightline.toggle_view",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_V,
        CATEGORY
    );
    private static final KeyMapping TOGGLE_HUD_MODE = new KeyMapping(
        "key.sightline.toggle_hud_mode",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_J,
        CATEGORY
    );
    private static final KeyMapping TOGGLE_HUD_ANCHOR = new KeyMapping(
        "key.sightline.toggle_hud_anchor",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_K,
        CATEGORY
    );
    private static final KeyMapping HUD_OPACITY_DOWN = new KeyMapping(
        "key.sightline.hud_opacity_down",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_U,
        CATEGORY
    );
    private static final KeyMapping HUD_OPACITY_UP = new KeyMapping(
        "key.sightline.hud_opacity_up",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_I,
        CATEGORY
    );

    private static boolean hudEnabled = true;
    private static boolean hudCompact;
    private static HudAnchor hudAnchor = HudAnchor.TOP_LEFT;
    private static int hudOpacityAlpha = 0x88;

    private ClientHotkeys() {
    }

    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    public static boolean isHudCompact() {
        return hudCompact;
    }

    public static HudAnchor hudAnchor() {
        return hudAnchor;
    }

    public static int hudOpacityAlpha() {
        return hudOpacityAlpha;
    }

    public static String hudModeTranslationKey() {
        return hudCompact ? "hud.sightline.hud_mode.compact" : "hud.sightline.hud_mode.full";
    }

    public static String hudAnchorTranslationKey() {
        return translationFor(hudAnchor);
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE_HUD);
        event.register(TOGGLE_CAMERA_VIEW);
        event.register(TOGGLE_HUD_MODE);
        event.register(TOGGLE_HUD_ANCHOR);
        event.register(HUD_OPACITY_DOWN);
        event.register(HUD_OPACITY_UP);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_HUD.consumeClick()) {
            hudEnabled = !hudEnabled;
        }
        while (TOGGLE_CAMERA_VIEW.consumeClick()) {
            toggleCameraView();
        }
        while (TOGGLE_HUD_MODE.consumeClick()) {
            toggleHudMode();
        }
        while (TOGGLE_HUD_ANCHOR.consumeClick()) {
            cycleHudAnchor();
        }
        while (HUD_OPACITY_DOWN.consumeClick()) {
            adjustHudOpacity(-16);
        }
        while (HUD_OPACITY_UP.consumeClick()) {
            adjustHudOpacity(16);
        }
    }

    private static void toggleCameraView() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options == null || minecraft.player == null) {
            return;
        }
        CameraType current = minecraft.options.getCameraType();
        CameraType next = switch (current) {
            case FIRST_PERSON -> CameraType.THIRD_PERSON_BACK;
            case THIRD_PERSON_BACK -> CameraType.THIRD_PERSON_FRONT;
            case THIRD_PERSON_FRONT -> CameraType.FIRST_PERSON;
        };
        minecraft.options.setCameraType(next);
        minecraft.gui.setOverlayMessage(
            Component.translatable("hud.sightline.view_mode", Component.translatable(translationFor(next))),
            false
        );
    }

    private static String translationFor(CameraType type) {
        return switch (type) {
            case FIRST_PERSON -> "hud.sightline.view.first";
            case THIRD_PERSON_BACK -> "hud.sightline.view.third_back";
            case THIRD_PERSON_FRONT -> "hud.sightline.view.third_front";
        };
    }

    private static void toggleHudMode() {
        Minecraft minecraft = Minecraft.getInstance();
        hudCompact = !hudCompact;
        if (minecraft.gui != null) {
            minecraft.gui.setOverlayMessage(
                Component.translatable(
                    "hud.sightline.hud_mode",
                    Component.translatable(hudModeTranslationKey())
                ),
                false
            );
        }
    }

    private static void cycleHudAnchor() {
        Minecraft minecraft = Minecraft.getInstance();
        hudAnchor = switch (hudAnchor) {
            case TOP_LEFT -> HudAnchor.TOP_RIGHT;
            case TOP_RIGHT -> HudAnchor.BOTTOM_RIGHT;
            case BOTTOM_RIGHT -> HudAnchor.BOTTOM_LEFT;
            case BOTTOM_LEFT -> HudAnchor.TOP_LEFT;
        };
        if (minecraft.gui != null) {
            minecraft.gui.setOverlayMessage(
                Component.translatable(
                    "hud.sightline.hud_anchor",
                    Component.translatable(translationFor(hudAnchor))
                ),
                false
            );
        }
    }

    private static String translationFor(HudAnchor anchor) {
        return switch (anchor) {
            case TOP_LEFT -> "hud.sightline.hud_anchor.top_left";
            case TOP_RIGHT -> "hud.sightline.hud_anchor.top_right";
            case BOTTOM_LEFT -> "hud.sightline.hud_anchor.bottom_left";
            case BOTTOM_RIGHT -> "hud.sightline.hud_anchor.bottom_right";
        };
    }

    private static void adjustHudOpacity(int delta) {
        Minecraft minecraft = Minecraft.getInstance();
        int next = Math.max(0x20, Math.min(0xE0, hudOpacityAlpha + delta));
        if (next == hudOpacityAlpha) {
            return;
        }
        hudOpacityAlpha = next;
        int percent = (int) Math.round((hudOpacityAlpha / 255.0) * 100.0);
        if (minecraft.gui != null) {
            minecraft.gui.setOverlayMessage(Component.translatable("hud.sightline.opacity", percent), false);
        }
    }
}
