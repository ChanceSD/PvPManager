package me.NoChance.PvPManager.Dependencies.Hooks;

import me.NoChance.PvPManager.PvPManager;
import me.NoChance.PvPManager.Dependencies.DependencyException;
import me.NoChance.PvPManager.Dependencies.Hook;
import me.NoChance.PvPManager.Dependencies.API.BaseDependency;

public class PlaceHolderAPIHook extends BaseDependency {

	private final PlaceHolderAPI placeHolderAPI;

	public PlaceHolderAPIHook(final Hook hook) {
		super(hook);
		this.placeHolderAPI = new PlaceHolderAPI(PvPManager.getInstance());
		if (!placeHolderAPI.register() && !placeHolderAPI.isRegistered())
			throw new DependencyException("Error registering placeholder expansion, placeholders disabled!", hook);
	}

	public PlaceHolderAPI getPlaceHolder() {
		return placeHolderAPI;
	}

}
