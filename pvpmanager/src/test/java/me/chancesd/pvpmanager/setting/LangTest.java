package me.chancesd.pvpmanager.setting;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import me.chancesd.pvpmanager.InstanceCreator;
import me.chancesd.pvpmanager.setting.lang.Replacement;

@ExtendWith(InstanceCreator.class)
public class LangTest {

	@Test
	void testLocalePlaceholderValidation() {
		final Pattern placeholderPattern = Pattern.compile("\\{([^}]+)\\}");
		for (final Locale locale : Locale.values()) {
			final Properties properties = new Properties();
			try (InputStream input = getClass().getClassLoader().getResourceAsStream("locale/" + locale.fileName())) {
				if (input == null) {
					fail("Locale file not found: " + locale.fileName());
					continue;
				}
				properties.load(input);
			} catch (final IOException e) {
				fail("Error loading locale file: " + locale.fileName(), e);
				continue;
			}

			for (final Lang lang : Lang.values()) {
				final String messageKey = lang.getMessageKey();
				final String rawMessage = properties.getProperty(messageKey);
				if (rawMessage == null) {
					// fail("Missing message key '" + messageKey + "' in locale " + locale);
					continue;
				}
				final String message = new String(rawMessage.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
				final Replacement[] replacements = lang.getReplacements();
				final Set<String> expectedPlaceholders = Arrays.stream(replacements)
						.map(Replacement::getPlaceholder)
						.collect(java.util.stream.Collectors.toSet());
				// {prefix} is always allowed as it's replaced globally
				expectedPlaceholders.add(Replacement.PREFIX.getPlaceholder());

				// Check that all expected replacements are present
				for (final Replacement replacement : replacements) {
					assertTrue(message.contains(replacement.getPlaceholder()),
							"Missing replacement " + replacement.getPlaceholder() + " in lang " + lang.getMessageKey() + " for locale " + locale);
				}

				// Check for unrecognized placeholders in the message
				final Matcher matcher = placeholderPattern.matcher(message);
				final Set<String> foundPlaceholders = new HashSet<>();
				while (matcher.find()) {
					foundPlaceholders.add("{" + matcher.group(1) + "}");
				}
				for (final String found : foundPlaceholders) {
					if (!expectedPlaceholders.contains(found)) {
						fail("Unrecognized replacement '" + found + "' found in message for lang " + lang.getMessageKey() + " in locale " + locale);
					}
				}
			}
		}
	}

}
