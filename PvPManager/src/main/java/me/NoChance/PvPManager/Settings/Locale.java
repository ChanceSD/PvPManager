package me.NoChance.PvPManager.Settings;

public enum Locale {
	EN("messages.properties"),
	RU("messages_ru.properties"),
	ZH("messages_zh.properties"),
	ZH_TW("messages_zh_tw.properties"),
	ES("messages_es.properties"),
	DE("messages_de.properties"),
	PT_BR("messages_pt_br.properties"),
	IT("messages_it.properties");

	private final String fileName;

	Locale(final String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return fileName;
	}
}
