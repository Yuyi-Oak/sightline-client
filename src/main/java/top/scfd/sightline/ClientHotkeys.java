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

    private static boolean hudEnabled = true;
    private static boolean hudCompact;

    private ClientHotkeys() {
    }

    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    public static boolean isHudCompact() {
        return hudCompact;
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE_HUD);
        event.register(TOGGLE_CAMERA_VIEW);
        event.register(TOGGLE_HUD_MODE);
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
                    Component.translatable(hudCompact ? "hud.sightline.hud_mode.compact" : "hud.sightline.hud_mode.full")
                ),
                false
            );
        }
    }
}
