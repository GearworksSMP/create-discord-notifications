package com.gearworks.notifier;

import net.dv8tion.jda.api.EmbedBuilder;

import java.util.Optional;

public interface MessageEvent {
	default Optional<String> getMentionedUser() {
		return Optional.empty();
	}

	void buildMessage(EmbedBuilder builder);

	String getChannelId();
}
