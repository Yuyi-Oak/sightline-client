package top.scfd.sightline;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(SightlineClient.MOD_ID)
public final class SightlineClient {
    public static final String MOD_ID = "sightline";
    private static final Logger LOGGER = LogUtils.getLogger();

    public SightlineClient() {
        LOGGER.info("Sightline client loaded");
    }
}
