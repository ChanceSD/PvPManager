package me.NoChance.PvPManager.Settings;

public enum Locale {
	EN("messages.properties"),
	RU("messages_ru.properties"),
	ZH("messages_zh.properties"),
	ZHTW("messages_zhtw.properties"),
	ES("messages_es.properties"),
	DE("messages_de.properties"),
	BR("messages_br.properties");

	private final String fileName;

	Locale(final String fileName) {
		this.fileName = fileName;
	}

	@Override
	public String toString() {
		return fileName;
	}
}
