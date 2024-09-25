package com.gearworks.notifier.mixin.numismatics;

import com.gearworks.notifier.notifiers.NumismaticsNotifier;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = VendorBlockEntity.class)
public abstract class VendorBlockEntityMixin extends BlockEntity {
	public VendorBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	@Shadow(remap = false)
	protected abstract boolean hasStock();

	@Shadow(remap = false)
	private SliderStylePriceBehaviour price;

	@WrapOperation(
			method = "trySellTo",
			at = @At(
					value = "INVOKE",
					target = "Ldev/ithundxr/createnumismatics/util/ItemUtil;givePlayerItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
					ordinal = 0
			)
	)
	private void notifier$sendNotificationsOnSellOne(Player purchaser, ItemStack soldItem, Operation<Void> original) {
		this.notifier$sendNotifications(purchaser, soldItem);
		original.call(purchaser, soldItem);
	}

	@WrapOperation(
			method = "trySellTo",
			at = @At(
					value = "INVOKE",
					target = "Ldev/ithundxr/createnumismatics/util/ItemUtil;givePlayerItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)V",
					ordinal = 1
			)
	)
	private void notifier$sendNotificationsOnSellTwo(Player purchaser, ItemStack soldItem, Operation<Void> original) {
		this.notifier$sendNotifications(purchaser, soldItem);
		original.call(purchaser, soldItem);
	}

	@SuppressWarnings("DataFlowIssue")
	@Unique
	private void notifier$sendNotifications(Player purchaser, ItemStack soldItem) {
		if (purchaser.level() instanceof ServerLevel serverLevel) {
			NumismaticsNotifier.INSTANCE.sendPurchaseNotification(serverLevel, (VendorBlockEntity) (Object) this, this.getBlockPos(), purchaser, soldItem.copy(), this.price);

			if (!this.hasStock()) {
				NumismaticsNotifier.INSTANCE.sendOutOfStockNotification(serverLevel, (VendorBlockEntity) (Object) this, this.getBlockPos());
			}
		}
	}
}
