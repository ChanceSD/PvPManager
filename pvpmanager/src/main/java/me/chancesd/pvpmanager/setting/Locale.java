package me.chancesd.pvpmanager.setting;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Locale {
	BG("messages_bg.properties", "Bulgarian"),
	DE("messages_de.properties", "German"),
	ES("messages_es.properties", "Spanish"),
	FI("messages_fi.properties", "Finnish"),
	FR("messages_fr.properties", "French"),
	HR("messages_hr.properties", "Croatian"),
	IT("messages_it.properties", "Italian"),
	JA("messages_ja.properties", "Japanese"),
	KO("messages_ko.properties", "Korean"),
	NL("messages_nl.properties", "Dutch"),
	PL("messages_pl.properties", "Polish"),
	PT_BR("messages_pt_BR.properties", "Portuguese Brazilian"),
	RU("messages_ru.properties", "Russian"),
	TR("messages_tr.properties", "Turkish"),
	ZH_TW("messages_zh_TW.properties", "Chinese Traditional"),
	ZH("messages_zh.properties", "Chinese Simplified"),
	EN("messages.properties", "English");

	private final String fileName;
	private final String language;

	Locale(final String fileName, final String language) {
		this.fileName = fileName;
		this.language = language;
	}

	public static List<String> asStringList() {
		final List<Locale> list = Arrays.asList(values());
		return list.stream().map(Locale::name).collect(Collectors.toList());
	}

	public String language() {
		return language;
	}

	public String fileName() {
		return fileName;
	}

	@Override
	public String toString() {
		return this.name() + " (" + language + ")";
	}
}
