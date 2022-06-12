package me.NoChance.PvPManager.MySQL;

public class Table {
	private final String name;
	private final String usage;

	public Table(final String name, final String usage) {
		this.name = name;
		this.usage = usage;
	}

	public String getName() {
		return this.name;
	}

	public String getUsage() {
		return " (" + usage + ")";
	}

	public String getValues() {
		String v = "";
		final String[] a = usage.split(",");
		int i = 0;
		for (final String b : a) {
			i += 1;
			final String[] c = b.split(" ");
			final String f = c[0] == null || c[0].isEmpty() ? c[1] : c[0];
			v += f == null ? "" : f + (i <= a.length - 1 ? "," : "");
		}

		return "(" + v + ")";
	}
}
