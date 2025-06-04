package me.chancesd.pvpmanager.utils;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class PaginatedChatMenu {
	private final Player player;
	private final String[] pages;
	private int currentPage;

	public PaginatedChatMenu(final Player player, final String[] pages) {
		this.player = player;
		this.pages = pages;
		this.currentPage = 0;
	}

	public void displayPage(final int pageNumber) {
		if (pageNumber >= 0 && pageNumber < pages.length) {
			player.sendMessage(pages[pageNumber]);
			currentPage = pageNumber;
		} else {
			player.sendMessage("Page number out of range.");
		}
	}

	public void open() {
		displayPage(currentPage);
		sendPageNavigation();
	}

	private void sendPageNavigation() {
		final TextComponent navigation = new TextComponent("");
		final TextComponent prevPage = new TextComponent("<< Prev ");
		final TextComponent nextPage = new TextComponent(" Next >>");
		prevPage.setColor(ChatColor.GRAY);
		nextPage.setColor(ChatColor.GRAY);

		// Create click events to navigate to the previous and next pages
		if (currentPage > 0) {
			final ClickEvent prevClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/prev");
			prevPage.setClickEvent(prevClick);
		}
		if (currentPage < pages.length - 1) {
			final ClickEvent nextClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/next");
			nextPage.setClickEvent(nextClick);
		}

		// Add the components together
		navigation.addExtra(prevPage);
		navigation.addExtra(nextPage);

		player.spigot().sendMessage(navigation);
	}

	public void nextPage() {
		if (currentPage < pages.length - 1) {
			currentPage++;
			displayPage(currentPage);
			sendPageNavigation();
		}
	}

	public void prevPage() {
		if (currentPage > 0) {
			currentPage--;
			displayPage(currentPage);
			sendPageNavigation();
		}
	}
}
