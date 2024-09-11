package com.gearworks.notifier.mixin.rentaplate;

import com.gearworks.notifier.WarpPlatePairDuck;
import com.gearworks.rentaplate.data.WarpPlate;
import com.gearworks.rentaplate.data.WarpPlatePair;
import com.gearworks.rentaplate.data.WarpPlatesSavedData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WarpPlatesSavedData.class)
public class WarpPlatesSavedDataMixin {
	@ModifyExpressionValue(
			method = "save",
			at = @At(
					value = "NEW",
					target = "()Lnet/minecraft/nbt/CompoundTag;"
			)
	)
	private CompoundTag notifier$saveNotifierField(CompoundTag original, @Local WarpPlatePair pair) {
		original.putBoolean("notifiedOfExpiry", ((WarpPlatePairDuck) (Object) pair).notifier$hasNotifiedOfExpiry());
		return original;
	}

	@WrapOperation(
			method = "createFromTag",
			at = @At(
					value = "NEW",
					target = "(IJLcom/gearworks/rentaplate/data/WarpPlate;Lcom/gearworks/rentaplate/data/WarpPlate;)Lcom/gearworks/rentaplate/data/WarpPlatePair;",
					remap = false
			)
	)
	private static WarpPlatePair notifier$loadNotifierField(int id, long expiryTime, WarpPlate warpPlate, WarpPlate returnPlate, Operation<WarpPlatePair> original, @Local(ordinal = 1) CompoundTag pairTag) {
		WarpPlatePair instance = original.call(id, expiryTime, warpPlate, returnPlate);
		((WarpPlatePairDuck) (Object) instance).notifier$setNotifiedOfExpiry(pairTag.getBoolean("notifiedOfExpiry"));
		return instance;
	}
}
