package com.gearworks.notifier;

import com.gearworks.notifier.data.NotificationSettings;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class DiscordNotifierClient implements ClientModInitializer {
	public static NotificationSettings currentSettings;

	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, ctx2) -> dispatcher.register(ClientCommandManager.literal("notifications").then(ClientCommandManager.literal("settings").executes(ctx -> {
			ctx.getSource().getClient().tell(() -> ctx.getSource().getClient().setScreen(new NotificationSettingsScreen(ctx.getSource().getClient().screen)));
			return 0;
		}))));

		ClientPlayNetworking.registerGlobalReceiver(DiscordNotifier.PACKET_S2C_NOTIFICATION_SETTINGS, (client, handler, buf, responseSender) -> {
			NotificationSettings settings = NotificationSettings.fromBuf(buf);
			client.tell(() -> {
				currentSettings = settings;
			});
		});
	}
}
