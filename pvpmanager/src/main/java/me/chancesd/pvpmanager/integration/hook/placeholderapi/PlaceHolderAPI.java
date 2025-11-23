package me.chancesd.pvpmanager.integration.hook.placeholderapi;

import java.util.List;

import org.bukkit.entity.Player;
import me.chancesd.pvpmanager.PvPManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class PlaceHolderAPI extends PlaceholderExpansion {

	private final PvPManager plugin;
	private final PlaceholderProcessor processor;

	public PlaceHolderAPI(final PvPManager plugin) {
		this.plugin = plugin;
		this.processor = new PlaceholderProcessor(plugin);
	}

	@Override
	public String onPlaceholderRequest(final Player player, final String identifier) {
		return processor.processPlaceholder(player, identifier);
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String getAuthor() {
		return plugin.getDescription().getAuthors().toString();
	}

	@Override
	public String getName() {
		return plugin.getDescription().getName();
	}

	@Override
	public String getIdentifier() {
		return "pvpmanager";
	}

	@Override
	public String getVersion() {
		return plugin.getDescription().getVersion();
	}

	@Override
	public List<String> getPlaceholders() {
		return processor.getPlaceholders();
	}

}
