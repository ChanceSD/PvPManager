package me.chancesd.pvpmanager.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;

@ExtendWith(InstanceCreator.class)
public class LangTest {

	@Test
	void allEnabled() {
		for (final Lang lang : Lang.values()) {
			final String[] replacements = lang.getReplacements();
			for (final String replacement : replacements) {
				assertTrue(lang.msg().contains(replacement), "Missing replacement " + replacement + " in lang " + lang.getMessageKey());
			}
		}
	}

}
