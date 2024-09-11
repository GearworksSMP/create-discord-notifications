package com.gearworks.notifier;

import com.gearworks.notifier.data.NotificationSettings;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class NotificationSettingsScreen extends Screen {
	private final Screen parent;
	private EditBox discordUsername;
	private CycleButton<Boolean> receivePurchaseNotifications;
	private CycleButton<Boolean> receiveOutOfStockNotifications;
	private CycleButton<Boolean> receiveWarpPlateExpiryNotifications;

	public NotificationSettingsScreen(Screen parent) {
		super(Component.translatable("text.discord_notifier.settings.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();

		this.discordUsername = this.addRenderableWidget(new EditBox(this.font, this.width / 2 - 100, this.height / 6, 200, 20, Component.translatable("text.discord_notifier.settings.discord_username")));
		this.discordUsername.setMaxLength(32);
		this.discordUsername.setHint(Component.translatable("text.discord_notifier.settings.discord_username_prompt"));
		this.discordUsername.setValue(DiscordNotifierClient.currentSettings.discordUsername().orElse(""));

		this.receivePurchaseNotifications = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("text.discord_notifier.settings.on"), Component.translatable("text.discord_notifier.settings.off")).create(this.width / 2 - 100, this.height / 6 + 24, 200, 20, Component.translatable("text.discord_notifier.settings.purchase_notifications")));
		this.receivePurchaseNotifications.setValue(DiscordNotifierClient.currentSettings.receivePurchaseNotifications());

		this.receiveOutOfStockNotifications = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("text.discord_notifier.settings.on"), Component.translatable("text.discord_notifier.settings.off")).create(this.width / 2 - 100, this.height / 6 + 48, 200, 20, Component.translatable("text.discord_notifier.settings.out_of_stock_notification")));
		this.receiveOutOfStockNotifications.setValue(DiscordNotifierClient.currentSettings.receiveOutOfStockNotifications());

		this.receiveWarpPlateExpiryNotifications = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("text.discord_notifier.settings.on"), Component.translatable("text.discord_notifier.settings.off")).create(this.width / 2 - 100, this.height / 6 + 72, 200, 20, Component.translatable("text.discord_notifier.settings.warp_plate_notifications")));
		this.receiveWarpPlateExpiryNotifications.setValue(DiscordNotifierClient.currentSettings.receiveWarpPlateExpiryNotifications());

		this.addRenderableWidget(Button.builder(Component.translatable("text.discord_notifier.settings.save"), button -> this.onClose()).size(100, 20).pos(this.width / 2 - 50, this.height / 6 + 96).build());
	}

	@Override
	public void onClose() {
		super.onClose();
		this.minecraft.setScreen(this.parent);
		NotificationSettings newSettings = new NotificationSettings(this.discordUsername.getValue().isEmpty() ? Optional.empty() : Optional.of(this.discordUsername.getValue()), this.receivePurchaseNotifications.getValue(), this.receiveOutOfStockNotifications.getValue(), this.receiveWarpPlateExpiryNotifications.getValue());
		ClientPlayNetworking.send(DiscordNotifier.PACKET_C2S_UPDATE_NOTIFICATION_SETTINGS, newSettings.toBuf());
		DiscordNotifierClient.currentSettings = newSettings;
	}
}
