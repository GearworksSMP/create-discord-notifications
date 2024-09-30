package com.gearworks.notifier;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiscordBot implements Runnable {
	public static final DiscordBot INSTANCE = new DiscordBot();
	private final Queue<MessageEvent> messageEventQueue;
	private boolean started = false;
	private int retryCount = 0;
	private JDA api;
	private boolean shouldRun = true;

	private DiscordBot() {
		this.messageEventQueue = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void run() {
		if (this.started) {
			return;
		}

		this.started = true;
		DiscordNotifier.LOGGER.info("Starting Discord bot");

		this.api = JDABuilder
				.createDefault(NotifierConfig.INSTANCE.getBotToken())
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.addEventListeners()
				.build();

		try {
			this.api.awaitReady();
			this.api.getGuildById(NotifierConfig.INSTANCE.getServerId()).findMembers((m) -> true)
					.onError((e) -> DiscordNotifier.LOGGER.error("Failed to precache members", e))
					.onSuccess((m) -> DiscordNotifier.LOGGER.info("Precached {} members", m.size()));

			this.pollEvents();
		} catch (Exception e) {
			DiscordNotifier.LOGGER.error("Encountered exception in bot thread", e);
			this.started = false;
			this.retryCount++;

			if (this.retryCount < 5) {
				this.run();
			}
		}
	}
	
	public void stop() {
		DiscordNotifier.LOGGER.info("Stopping bot thread");
		this.shouldRun = false;
	}

	public Optional<String> getId(String username) {
		return this.api.getUsersByName(username, false).stream().findFirst().map(ISnowflake::getId);
	}

	public void addMessage(MessageEvent messageEvent) {
		this.messageEventQueue.add(messageEvent);
	}

	private void pollEvents() {
		while (this.started && this.shouldRun) {
			MessageEvent messageEvent = this.messageEventQueue.poll();

			if (messageEvent != null) {
				EmbedBuilder builder = new EmbedBuilder();
				messageEvent.buildMessage(builder);
				TextChannel channel = this.api.getTextChannelById(messageEvent.getChannelId());

				if (channel != null) {
					MessageCreateBuilder data = new MessageCreateBuilder()
							.setEmbeds(builder.build());

					Optional<String> mentionedUser = messageEvent.getMentionedUser();
					// If mentionedUser == serverId, then it is an @everyone ping. It shouldn't ever be (it would fail earlier), but this is a final sanity check.
					if (NotifierConfig.INSTANCE.allowPings() && mentionedUser.isPresent() && !mentionedUser.get().equals(NotifierConfig.INSTANCE.getServerId())) {
						data.setContent(messageEvent.getMentionedUser().map((u) -> "<@" + u + ">").get());
					}

					channel.sendMessage(data.build()).queue();
				} else {
					DiscordNotifier.LOGGER.error("Failed to find channel with ID {}", messageEvent.getChannelId());
				}
			}
		}
	}
}
