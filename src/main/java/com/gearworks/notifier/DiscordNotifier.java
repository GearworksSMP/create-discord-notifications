package com.gearworks.notifier;

import com.gearworks.notifier.data.NotificationSettings;
import com.gearworks.notifier.data.NotifierSavedData;
import com.gearworks.notifier.notifiers.NumismaticsNotifier;
import com.gearworks.notifier.notifiers.WarpPlateNotifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscordNotifier implements ModInitializer {
	public static final String MOD_ID = "discord-notifier";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final ResourceLocation PACKET_C2S_UPDATE_NOTIFICATION_SETTINGS = new ResourceLocation(MOD_ID, "c2s_update_notification_settings");
	public static final ResourceLocation PACKET_S2C_NOTIFICATION_SETTINGS = new ResourceLocation(MOD_ID, "s2c_notification_settings");
	public static final boolean EMBED_FIELDS_ARE_INLINE = true;

	@Override
	public void onInitialize() {
		new Thread(DiscordBot.INSTANCE).start();

		ServerPlayNetworking.registerGlobalReceiver(PACKET_C2S_UPDATE_NOTIFICATION_SETTINGS, (server, player, handler, buf, responseSender) -> {
			NotificationSettings settings = NotificationSettings.fromBuf(buf);
			server.execute(() -> NotifierSavedData.setSettings(player, settings));
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			NotificationSettings settings = NotifierSavedData.getSettings(handler.getPlayer());
			sender.sendPacket(PACKET_S2C_NOTIFICATION_SETTINGS, settings.toBuf());
		});

		if (FabricLoader.getInstance().isModLoaded("rentaplate")) {
			ServerTickEvents.END_WORLD_TICK.register(WarpPlateNotifier.INSTANCE);
		}
		
		if (FabricLoader.getInstance().isModLoaded("numismatics")) {
			ServerTickEvents.END_WORLD_TICK.register(NumismaticsNotifier.INSTANCE);
			ServerLifecycleEvents.SERVER_STOPPING.register(NumismaticsNotifier.INSTANCE);
		}
	}
}
