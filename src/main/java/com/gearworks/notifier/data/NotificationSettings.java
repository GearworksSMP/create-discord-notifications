package com.gearworks.notifier.data;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Optional;

public record NotificationSettings(Optional<String> discordUsername, boolean receivePurchaseNotifications,
                                   boolean receiveOutOfStockNotifications,
                                   boolean receiveWarpPlateExpiryNotifications) {
	public FriendlyByteBuf toBuf() {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeBoolean(discordUsername.isPresent());
		discordUsername.ifPresent(buf::writeUtf);
		buf.writeBoolean(receivePurchaseNotifications);
		buf.writeBoolean(receiveOutOfStockNotifications);
		buf.writeBoolean(receiveWarpPlateExpiryNotifications);
		return buf;
	}

	public static NotificationSettings fromBuf(FriendlyByteBuf buf) {
		Optional<String> discordUsername = buf.readBoolean() ? Optional.of(buf.readUtf()) : Optional.empty();
		boolean receivePurchaseNotifications = buf.readBoolean();
		boolean receiveOutOfStockNotifications = buf.readBoolean();
		boolean receiveWarpPlateExpiryNotifications = buf.readBoolean();
		return new NotificationSettings(discordUsername, receivePurchaseNotifications, receiveOutOfStockNotifications, receiveWarpPlateExpiryNotifications);
	}
}
