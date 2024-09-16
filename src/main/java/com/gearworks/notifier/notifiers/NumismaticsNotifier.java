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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gearworks.notifier.DiscordNotifier.EMBED_FIELDS_ARE_INLINE;

public class NumismaticsNotifier implements ServerTickEvents.EndWorldTick, ServerLifecycleEvents.ServerStopping {
	public static final NumismaticsNotifier INSTANCE = new NumismaticsNotifier();
	private static final List<Tuple<PurchaseData, Integer>> purchaseQueue = new ArrayList<>();

	private NumismaticsNotifier() {
	}
	
	@Override
	public void onEndTick(ServerLevel world) {
		List<Tuple<PurchaseData, Integer>> toRemove = new ArrayList<>();
		
		for (Tuple<PurchaseData, Integer> tuple : purchaseQueue) {
			PurchaseData data = tuple.getA();
			int ticksLeft = tuple.getB();
			
			if (ticksLeft <= 0) {
				DiscordBot.INSTANCE.addMessage(data.toEvent());
				toRemove.add(tuple);
			} else {
				tuple.setB(ticksLeft - 1);
			}
		}
		
		purchaseQueue.removeAll(toRemove);
	}

	@Override
	public void onServerStopping(MinecraftServer server) {
		for (Tuple<PurchaseData, Integer> tuple : purchaseQueue) {
			DiscordBot.INSTANCE.addMessage(tuple.getA().toEvent());
		}
	}

	public static void sendPurchaseNotification(ServerLevel level, VendorBlockEntity vendor, BlockPos pos, Player purchaser, ItemStack purchasedItem, SliderStylePriceBehaviour price) {
		UUID owner = ((VendorBlockEntityAccessor) vendor).getOwner();
		if (owner == null) {
			return;
		}

		NotificationSettings settings = NotifierSavedData.getSettings(level.getServer(), owner);
		PurchaseData data = new PurchaseData(level.dimension(), pos, UsernameUtils.INSTANCE.getName(purchaser.getUUID()), UsernameUtils.INSTANCE.getName(owner), settings.discordUsername(), purchasedItem, price.getPrice(Coin.COG));
		
		for (Tuple<PurchaseData, Integer> tuple : purchaseQueue) {
			if (tuple.getA().canMerge(data)) {
				tuple.getA().merge(data);
				tuple.setB(10 * 20);
				return;
			}
		}
		
		purchaseQueue.add(new Tuple<>(data, 10 * 20));
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

	private static class PurchaseData {
		private final ResourceKey<Level> dimension;
		private final BlockPos pos;
		private final String purchaserMcUsername;
		private final String sellerMcUsername;
		@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
		private final Optional<String> sellerDiscordUsername;
		private final ItemStack sold;
		private int cost;
		
		public PurchaseData(ResourceKey<Level> dimension, BlockPos pos, String purchaserMcUsername, String sellerMcUsername, Optional<String> sellerDiscordUsername, ItemStack sold, int cost) {
			this.dimension = dimension;
			this.pos = pos;
			this.purchaserMcUsername = purchaserMcUsername;
			this.sellerMcUsername = sellerMcUsername;
			this.sellerDiscordUsername = sellerDiscordUsername;
			this.sold = sold;
			this.cost = cost;
		}
		
		public boolean canMerge(PurchaseData other) {
			return this.dimension.equals(other.dimension) && this.pos.equals(other.pos) && this.purchaserMcUsername.equals(other.purchaserMcUsername) && this.sellerMcUsername.equals(other.sellerMcUsername) && this.sold.is(other.sold.getItem());
		}
		
		public void merge(PurchaseData other) {
			this.sold.grow(other.sold.getCount());
			this.cost += other.cost;
		}
		
		public PurchaseMessageEvent toEvent() {
			return new PurchaseMessageEvent(dimension, pos, sold, cost, purchaserMcUsername, sellerMcUsername, sellerDiscordUsername);
		}
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
			builder.addField("Buyer", purchaserMcUsername, EMBED_FIELDS_ARE_INLINE);
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
