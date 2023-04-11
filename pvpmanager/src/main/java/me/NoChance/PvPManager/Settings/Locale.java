package me.NoChance.PvPManager.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Locale {
	BG("messages_bg.properties"),
	DE("messages_de.properties"),
	ES("messages_es.properties"),
	FR("messages_fr.properties"),
	IT("messages_it.properties"),
	PL("messages_pl.properties"),
	PT_BR("messages_pt_BR.properties"),
	RU("messages_ru.properties"),
	ZH_TW("messages_zh_TW.properties"),
	ZH("messages_zh.properties"),
	EN("messages.properties");

	private final String fileName;

	Locale(final String fileName) {
		this.fileName = fileName;
	}

	public static List<String> asStringList() {
		final List<Locale> list = Arrays.asList(values());
		return list.stream().map(Locale::name).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return fileName;
	}
}
