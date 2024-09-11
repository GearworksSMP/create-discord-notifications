package com.gearworks.notifier.mixin.numismatics;

import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(value = VendorBlockEntity.class)
public interface VendorBlockEntityAccessor {
	@Accessor(remap = false)
	@Nullable UUID getOwner();
}
