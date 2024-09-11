package com.gearworks.notifier.mixin.client.rentaplate;

import com.gearworks.notifier.NotificationButton;
import com.gearworks.rentaplate.screen.WarpPlateRentScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WarpPlateRentScreen.class)
public abstract class WarpPlateRentScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {
	public WarpPlateRentScreenMixin(AbstractContainerMenu container, Inventory inv, Component title) {
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
