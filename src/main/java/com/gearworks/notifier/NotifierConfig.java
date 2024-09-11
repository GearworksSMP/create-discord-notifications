package com.gearworks.notifier;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class NotifierConfig {
	public static final NotifierConfig INSTANCE = new NotifierConfig(FabricLoader.getInstance().getConfigDir().resolve("discord_notifier.properties").toFile());
	private static final String SERVER_NAME = "server_display_name";
	private static final String SERVER_ID = "server_id";
	private static final String ALLOW_PINGS = "allow_pings";
	private static final String BOT_TOKEN = "bot_token";
	private static final String NOTIFICATION_CHANNEL = "notification_channel";
	private static final String PURCHASE_CHANNEL = "purchase_channel";
	private final Properties properties;

	public NotifierConfig(File file) {
		this.properties = new Properties();

		try {
			if (file.exists()) {
				this.properties.load(new FileReader(file));
			} else {
				file.createNewFile();
				this.properties.put(SERVER_NAME, "");
				this.properties.put(SERVER_ID, "");
				this.properties.put(ALLOW_PINGS, "true");
				this.properties.put(BOT_TOKEN, "");
				this.properties.put(NOTIFICATION_CHANNEL, "");
				this.properties.put(PURCHASE_CHANNEL, "");
				this.properties.store(new FileWriter(file), "");
			}
		} catch (IOException e) {
			DiscordNotifier.LOGGER.error("Failed to create config file", e);
		}
	}

	public String getServerName() {
		return this.properties.getProperty(SERVER_NAME);
	}

	public String getServerId() {
		return this.properties.getProperty(SERVER_ID);
	}

	public boolean allowPings() {
		return Boolean.parseBoolean(this.properties.getProperty(ALLOW_PINGS, "true"));
	}
	
	public String getBotToken() {
		return this.properties.getProperty(BOT_TOKEN);
	}

	public String getNotificationChannel() {
		return this.properties.getProperty(NOTIFICATION_CHANNEL);
	}

	public String getPurchaseChannel() {
		return this.properties.getProperty(PURCHASE_CHANNEL);
	}
}
