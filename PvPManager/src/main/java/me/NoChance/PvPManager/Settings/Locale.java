package me.NoChance.PvPManager.Settings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Locale {
	EN("messages.properties"),
	RU("messages_ru.properties"),
	ZH("messages_zh.properties"),
	ZH_TW("messages_zh_TW.properties"),
	ES("messages_es.properties"),
	DE("messages_de.properties"),
	PT_BR("messages_pt_BR.properties"),
	IT("messages_it.properties");

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
