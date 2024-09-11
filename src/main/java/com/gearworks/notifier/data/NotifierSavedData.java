package com.gearworks.notifier.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NotifierSavedData extends SavedData {
	private final Map<UUID, NotificationSettings> settings;

	private NotifierSavedData(Map<UUID, NotificationSettings> settings) {
		this.settings = settings;
	}

	private static NotifierSavedData get(MinecraftServer server) {
		return server.getLevel(ServerLevel.OVERWORLD).getDataStorage().computeIfAbsent(NotifierSavedData::createFromTag, () -> new NotifierSavedData(new HashMap<>()), "notifier");
	}

	public static NotificationSettings getSettings(MinecraftServer server, UUID player) {
		return get(server).getSettings(player);
	}

	public static NotificationSettings getSettings(ServerPlayer player) {
		return get(Objects.requireNonNull(player.getServer())).getSettings(player.getUUID());
	}

	public static void setSettings(ServerPlayer player, NotificationSettings settings) {
		get(Objects.requireNonNull(player.getServer())).settings.put(player.getUUID(), settings);
		get(player.getServer()).setDirty();
	}

	private NotificationSettings getSettings(UUID player) {
		return settings.getOrDefault(player, new NotificationSettings(Optional.empty(), false, false, false));
	}

	private static NotifierSavedData createFromTag(CompoundTag tag) {
		Map<UUID, NotificationSettings> settings = new HashMap<>();
		ListTag settingsList = tag.getList("settings", CompoundTag.TAG_COMPOUND);

		for (int i = 0; i < settingsList.size(); i++) {
			CompoundTag settingsTag = settingsList.getCompound(i);
			UUID player = settingsTag.getUUID("player");

			String username = null;

			if (settingsTag.contains("discordUsername")) {
				username = settingsTag.getString("discordUsername");
			}

			boolean receivePurchaseNotifications = settingsTag.getBoolean("receivePurchaseNotifications");
			boolean receiveOutOfStockNotifications = settingsTag.getBoolean("receiveOutOfStockNotifications");
			boolean receiveWarpPlateExpiryNotifications = settingsTag.getBoolean("receiveWarpPlateExpiryNotifications");

			settings.put(player, new NotificationSettings(Optional.ofNullable(username), receivePurchaseNotifications, receiveOutOfStockNotifications, receiveWarpPlateExpiryNotifications));
		}

		return new NotifierSavedData(settings);
	}

	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		ListTag pairs = new ListTag();

		for (Map.Entry<UUID, NotificationSettings> entry : settings.entrySet()) {
			CompoundTag settingsTag = new CompoundTag();
			settingsTag.putUUID("player", entry.getKey());
			entry.getValue().discordUsername().ifPresent(username -> settingsTag.putString("discordUsername", username));
			settingsTag.putBoolean("receivePurchaseNotifications", entry.getValue().receivePurchaseNotifications());
			settingsTag.putBoolean("receiveOutOfStockNotifications", entry.getValue().receiveOutOfStockNotifications());
			settingsTag.putBoolean("receiveWarpPlateExpiryNotifications", entry.getValue().receiveWarpPlateExpiryNotifications());
			pairs.add(settingsTag);
		}

		tag.put("settings", pairs);
		return tag;
	}
}
