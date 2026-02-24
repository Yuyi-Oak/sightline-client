package top.scfd.sightline;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(SightlineClient.MOD_ID)
public final class SightlineClient {
    public static final String MOD_ID = "sightline";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SightlineClient(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener(ClientHotkeys::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.addListener(ClientHotkeys::onClientTick);
        LOGGER.info("Sightline client loaded");
    }
}
