package com.gearworks.notifier.mixin.rentaplate;

import com.gearworks.rentaplate.data.WarpPlatePair;
import com.gearworks.rentaplate.data.WarpPlatesSavedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = WarpPlatesSavedData.class)
public interface WarpPlatesSavedDataAccessor {
	@Accessor(remap = false)
	List<WarpPlatePair> getPairs();
}
