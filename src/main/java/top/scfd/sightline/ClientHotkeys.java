package top.scfd.sightline;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
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

    private static boolean hudEnabled = true;

    private ClientHotkeys() {
    }

    public static boolean isHudEnabled() {
        return hudEnabled;
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(TOGGLE_HUD);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (TOGGLE_HUD.consumeClick()) {
            hudEnabled = !hudEnabled;
        }
    }
}
