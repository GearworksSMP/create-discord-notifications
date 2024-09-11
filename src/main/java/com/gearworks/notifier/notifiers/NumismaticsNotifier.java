package com.gearworks.notifier.notifiers;

import com.gearworks.notifier.DiscordBot;
import com.gearworks.notifier.EmbedUtil;
import com.gearworks.notifier.MessageEvent;
import com.gearworks.notifier.NotifierConfig;
import com.gearworks.notifier.data.NotificationSettings;
import com.gearworks.notifier.data.NotifierSavedData;
import com.gearworks.notifier.mixin.numismatics.VendorBlockEntityAccessor;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import dev.ithundxr.createnumismatics.content.backend.behaviours.SliderStylePriceBehaviour;
import dev.ithundxr.createnumismatics.content.vendor.VendorBlockEntity;
import dev.ithundxr.createnumismatics.util.UsernameUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

import static com.gearworks.notifier.DiscordNotifier.EMBED_FIELDS_ARE_INLINE;

public class NumismaticsNotifier {
	public static void sendPurchaseNotification(ServerLevel level, VendorBlockEntity vendor, BlockPos pos, Player purchaser, ItemStack purchasedItem, SliderStylePriceBehaviour price) {
		UUID owner = ((VendorBlockEntityAccessor) vendor).getOwner();
		if (owner == null) {
			return;
		}

		NotificationSettings settings = NotifierSavedData.getSettings(level.getServer(), owner);
		DiscordBot.INSTANCE.addMessage(new PurchaseMessageEvent(level.dimension(), pos, purchasedItem, price.getPrice(Coin.COG), UsernameUtils.INSTANCE.getName(purchaser.getUUID()), UsernameUtils.INSTANCE.getName(owner), settings.receivePurchaseNotifications() ? settings.discordUsername() : Optional.empty()));
	}

	public static void sendOutOfStockNotification(ServerLevel level, VendorBlockEntity vendor, BlockPos pos) {
		UUID owner = ((VendorBlockEntityAccessor) vendor).getOwner();
		if (owner == null) {
			return;
		}

		NotificationSettings settings = NotifierSavedData.getSettings(level.getServer(), owner);
		if (settings.discordUsername().isEmpty() || !settings.receiveOutOfStockNotifications()) {
			return;
		}

		String discordId = DiscordBot.INSTANCE.getId(settings.discordUsername().get()).orElse(null);
		if (discordId == null) {
			return;
		}

		DiscordBot.INSTANCE.addMessage(new OutOfStockMessageEvent(level.dimension(), pos, vendor.getSellingItem(), UsernameUtils.INSTANCE.getName(owner), discordId));
	}

	public record PurchaseMessageEvent(ResourceKey<Level> dimension, BlockPos pos, ItemStack sold, int cost,
	                                   String purchaserMcUsername, String sellerMcUsername,
	                                   Optional<String> sellerDiscordUsername) implements MessageEvent {
		@Override
		public Optional<String> getMentionedUser() {
			if (this.sellerDiscordUsername().isPresent()) {
				String discordId = DiscordBot.INSTANCE.getId(this.sellerDiscordUsername().get()).orElse(null);
				if (discordId != null) {
					return Optional.of(discordId);
				}
			}

			return Optional.empty();
		}

		@Override
		public void buildMessage(EmbedBuilder builder) {
			builder.setTitle("Purchase Notification");
			builder.addField("Server", NotifierConfig.INSTANCE.getServerName(), EMBED_FIELDS_ARE_INLINE);
			EmbedUtil.addLocation(builder, dimension, pos);
			builder.addField("Item", sold.getCount() + " " +sold.getHoverName().getString(), EMBED_FIELDS_ARE_INLINE);
			builder.addField("Cost", cost + " Cogs", EMBED_FIELDS_ARE_INLINE);
			builder.addField("Purchaser", purchaserMcUsername, EMBED_FIELDS_ARE_INLINE);
			builder.addField("Seller", sellerMcUsername, EMBED_FIELDS_ARE_INLINE);
		}

		@Override
		public String getChannelId() {
			return NotifierConfig.INSTANCE.getPurchaseChannel();
		}
	}

	public record OutOfStockMessageEvent(ResourceKey<Level> dimension, BlockPos pos, ItemStack sellingItem,
	                                     String sellerMcUsername, String sellerDiscordId) implements MessageEvent {
		@Override
		public Optional<String> getMentionedUser() {
			return Optional.of(sellerDiscordId);
		}

		@Override
		public void buildMessage(EmbedBuilder builder) {
			builder.setTitle("Out of Stock Notification");
			builder.addField("Server", NotifierConfig.INSTANCE.getServerName(), EMBED_FIELDS_ARE_INLINE);
			EmbedUtil.addLocation(builder, dimension, pos);
			builder.addField("Item", sellingItem.getHoverName().getString(), EMBED_FIELDS_ARE_INLINE);
			builder.addField("Seller", sellerMcUsername, EMBED_FIELDS_ARE_INLINE);
		}

		@Override
		public String getChannelId() {
			return NotifierConfig.INSTANCE.getNotificationChannel();
		}
	}
}
