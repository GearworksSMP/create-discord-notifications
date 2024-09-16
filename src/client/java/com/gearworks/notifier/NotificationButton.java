package com.gearworks.notifier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class NotificationButton extends Button {
	private static final ResourceLocation IMAGE = new ResourceLocation(DiscordNotifier.MOD_ID, "textures/gui/notification_button.png");
	private static final ResourceLocation HOVERED_IMAGE = new ResourceLocation(DiscordNotifier.MOD_ID, "textures/gui/notification_button_highlighted.png");

	public NotificationButton(int x, int y) {
		super(x, y, 20, 20, Component.translatable("text.discord_notifier.settings.title"), (button) -> Minecraft.getInstance().setScreen(new NotificationSettingsScreen(Minecraft.getInstance().screen)), Button.DEFAULT_NARRATION);
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.renderTexture(guiGraphics, this.isHoveredOrFocused() ? HOVERED_IMAGE : IMAGE, this.getX(), this.getY(), 0, 0, 0, this.width, this.height, 20, 20);
	}
}
