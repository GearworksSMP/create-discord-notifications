package com.gearworks.notifier.notifiers;

import com.gearworks.notifier.*;
import com.gearworks.notifier.data.NotificationSettings;
import com.gearworks.notifier.data.NotifierSavedData;
import com.gearworks.notifier.mixin.rentaplate.WarpPlatesSavedDataAccessor;
import com.gearworks.rentaplate.WarpPlatesConfig;
import com.gearworks.rentaplate.block.WarpPlateBlockEntity;
import com.gearworks.rentaplate.data.WarpPlatePair;
import com.gearworks.rentaplate.data.WarpPlatesSavedData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static com.gearworks.notifier.DiscordNotifier.EMBED_FIELDS_ARE_INLINE;

public class WarpPlateNotifier implements ServerTickEvents.EndWorldTick {
	@Override
	public void onEndTick(ServerLevel world) {
		WarpPlatesSavedData data = WarpPlatesSavedData.get(world);

		for (WarpPlatePair pair : ((WarpPlatesSavedDataAccessor) data).getPairs()) {
			long notificationTime = pair.expiryTime() - WarpPlatesConfig.INSTANCE.getRentRenewalTime();

			if (notificationTime < System.currentTimeMillis() && !((WarpPlatePairDuck) (Object) pair).notifier$hasNotifiedOfExpiry()) {
				ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, pair.warpPlate().dimension());
				ServerLevel dimension = world.getServer().getLevel(dimensionKey);
				if (dimension == null) {
					continue;
				}

				WarpPlateBlockEntity blockEntity = (WarpPlateBlockEntity) dimension.getBlockEntity(pair.warpPlate().pos());
				if (blockEntity == null) {
					continue;
				}

				NotificationSettings settings = NotifierSavedData.getSettings(world.getServer(), blockEntity.getRenter());
				if (settings.discordUsername().isEmpty() || !settings.receiveWarpPlateExpiryNotifications()) {
					return;
				}

				String discordId = DiscordBot.INSTANCE.getId(settings.discordUsername().get()).orElse(null);
				if (discordId == null) {
					return;
				}

				DiscordBot.INSTANCE.addMessage(new WarpPlateExpiryNotification(dimensionKey, pair.warpPlate().pos(), blockEntity.getWarpTitle(), discordId));
				((WarpPlatePairDuck) (Object) pair).notifier$setNotifiedOfExpiry(true);
			}
		}
	}

	public record WarpPlateExpiryNotification(ResourceKey<Level> dimension, BlockPos pos, String warpTitle,
	                                          String discordId) implements MessageEvent {
		@Override
		public Optional<String> getMentionedUser() {
			return Optional.of(discordId);
		}

		@Override
		public void buildMessage(EmbedBuilder builder) {
			builder.setTitle("Warp Plate Expiry Notification");
			builder.addField("Server", NotifierConfig.INSTANCE.getServerName(), EMBED_FIELDS_ARE_INLINE);
			EmbedUtil.addLocation(builder, dimension, pos);
			builder.addField("Warp Title", warpTitle, EMBED_FIELDS_ARE_INLINE);
		}

		@Override
		public String getChannelId() {
			return NotifierConfig.INSTANCE.getNotificationChannel();
		}
	}
}
