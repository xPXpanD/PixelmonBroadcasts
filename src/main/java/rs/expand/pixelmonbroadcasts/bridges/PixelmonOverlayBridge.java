package rs.expand.pixelmonbroadcasts.bridges;

import com.pixelmonmod.pixelmon.api.overlay.notice.EnumOverlayLayout;
import com.pixelmonmod.pixelmon.client.gui.custom.overlays.OverlayGraphicType;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author happyzleaf
 * @since 10/03/2019
 *
 * Slightly tweaked by XpanD for use in Pixelmon Broadcasts.
 */
public class PixelmonOverlayBridge
{
	private static PixelmonOverlayAPI api = null;

	public static void setup(Object plugin)
    {
		api = new PixelmonOverlayAPI(plugin);
	}

	public static void display(EnumOverlayLayout layout, OverlayGraphicType type, List<String> lines,
                               @Nullable Long duration, @Nullable String spec, @Nullable ItemStack itemStack)
    {
		if (api != null)
            api.display(layout, type, lines, duration, spec, itemStack);
	}
}