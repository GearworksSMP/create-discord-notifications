package com.gearworks.notifier.mixin.client.numismatics;

import com.gearworks.notifier.NotificationButton;
import dev.ithundxr.createnumismatics.content.vendor.VendorScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VendorScreen.class)
public abstract class VendorScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
	public VendorScreenMixin(AbstractContainerMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}

	@Inject(
			method = "init",
			at = @At("TAIL")
	)
	private void notifier$addNotificationButton(CallbackInfo ci) {
		this.addRenderableWidget(new NotificationButton(this.width - 25, 5));
	}
}
