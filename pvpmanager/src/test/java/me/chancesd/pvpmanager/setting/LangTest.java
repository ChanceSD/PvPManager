package me.chancesd.pvpmanager.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.setting.lang.Replacement;

@ExtendWith(InstanceCreator.class)
public class LangTest {

	@Test
	void allEnabled() {
		for (final Lang lang : Lang.values()) {
			final Replacement[] replacements = lang.getReplacements();
			for (final Replacement replacement : replacements) {
				assertTrue(lang.msg().contains(replacement.getPlaceholder()), "Missing replacement " + replacement + " in lang " + lang.getMessageKey());
			}
		}
	}

}
