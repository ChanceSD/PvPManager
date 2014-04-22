package me.NoChance.PvPManager.Config;

public enum Locale {
	EN("messages.properties"), RU("messages_ru.properties"), PT_BR("messages_ptBR.properties"), CH("messages_ch.properties");

	private String fileName;

	Locale(String fileName) {
		this.fileName = fileName;
	}
	
	public String toString(){
		return fileName;	
	}
}
