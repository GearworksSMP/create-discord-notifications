package com.gearworks.notifier.mixin.rentaplate;

import com.gearworks.notifier.WarpPlatePairDuck;
import com.gearworks.rentaplate.data.WarpPlatePair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WarpPlatePair.class)
public class WarpPlatePairMixin implements WarpPlatePairDuck {
	@Unique
	private boolean notifier$notifiedOfExpiry = false;

	@Override
	public boolean notifier$hasNotifiedOfExpiry() {
		return this.notifier$notifiedOfExpiry;
	}

	@Override
	public void notifier$setNotifiedOfExpiry(boolean notified) {
		this.notifier$notifiedOfExpiry = notified;
	}
	
	@Inject(method = "setExpiryTime", at = @At("TAIL"), remap = false)
	private void notifier$onSetExpiryTime(long expiryTime, CallbackInfo ci) {
		this.notifier$setNotifiedOfExpiry(false);
	}
}
