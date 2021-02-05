/* TODO: See if this can be made to work on pure Forge.
package rs.expand.pixelmonbroadcasts.bridges;

import com.happyzleaf.pixelmonoverlay.api.OverlayService;
import com.pixelmonmod.pixelmon.api.overlay.notice.EnumOverlayLayout;
import com.pixelmonmod.pixelmon.client.gui.custom.overlays.OverlayGraphicType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.service.ChangeServiceProviderEvent;
import org.spongepowered.api.item.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

*/
/**
 * @author happyzleaf
 * @since 10/03/2019
 *
 * Slightly tweaked by XpanD for use in Pixelmon Broadcasts.
 *//*

public class PixelmonOverlayAPI implements EventListener<ChangeServiceProviderEvent>
{
	private OverlayService service;

	PixelmonOverlayAPI(Object plugin)
	{
		service = Sponge.getServiceManager().provideUnchecked(OverlayService.class);
		Sponge.getEventManager().registerListener(plugin, ChangeServiceProviderEvent.class, this);
	}

	@Override
	public void handle(ChangeServiceProviderEvent event)
	{
		if (event.getService().equals(OverlayService.class))
			service = (OverlayService) event.getNewProviderRegistration().getProvider();
	}

	void display(EnumOverlayLayout layout, OverlayGraphicType type, List<String> lines,
				 @Nullable Long duration, @Nullable String spec, @Nullable ItemStack itemStack)
	{
		service.show(service.create(layout, type, lines, duration, spec, itemStack));
	}
}*/
