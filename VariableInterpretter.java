import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableInterpretter {

	public static class Variable {
		public String NAME = "";
		public Object VALUE = null;
		public char[] random_chars_for_generate = new char[] { '_', '-' };

		public Variable(String name, Object value) {
			NAME = name;
			VALUE = value;
		}

		public Variable(Object value) {
			VALUE = value;

			NAME = generateRandomName(value);
		}

		public String generateRandomName(Object obj) {
			String gen = getType().toLowerCase();
			int center = Math.abs((gen.length() - 1) / 2);
			StringBuilder builder = new StringBuilder(gen);
			char rnd = random_chars_for_generate[rnd(0, random_chars_for_generate.length - 1)];
			builder.setCharAt(center, rnd);
			gen = builder.toString();
			int rnd2 = rnd(0, 10);
			gen += String.valueOf(rnd2);
			return gen;
		}

		private static int rnd(int min, int max) {
			max -= min;
			return (int) (Math.random() * ++max) + min;
		}

		public final boolean isInteger() {
			try {
				Integer.parseInt(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public final boolean isLong() {
			try {
				Long.parseLong(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public final boolean isFloat() {
			try {
				Float.parseFloat(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public final boolean isDouble() {
			try {
				Double.parseDouble(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));
				return true;
			} catch (Exception e) {
				return false;
			}
		}

		public String getConstructorParameters() {
			Class<?> cl = VALUE.getClass();
			Constructor[] constructors = cl.getConstructors();
			if (constructors != null) {
				if (constructors.length > 0) {
					String res = "";
					int index = 0;
					if (constructors.length == 2) {
						index = 1;
					} else {
						if (constructors.length <= 1) {
							index = 0;
						} else {
							index = Math.abs((constructors.length - 1) / 2);
						}

					}
					Constructor constructor = constructors[index];
					Class[] ptypes = constructor.getParameterTypes();
					for (Class c : ptypes) {
						res += "," + c.getName();
					}
					return res.replaceFirst(",", "");
				} else {
					return "";
				}
			} else {
				return "";
			}
		}

		public String importConstructorParameters() {
			if (getConstructorParameters().equals("")) {
				return "";
			} else {
				String p = getConstructorParameters();
				if (p.contains(",")) {
					String[] split = p.split(",");
					String res = "";
					for (String s : split) {
						res += "\nimport " + s + ";";
					}
					return res.replaceFirst("\n", "");
				} else {
					return "import " + p + ";";
				}
			}
		}

		public final int toInteger() {
			return Integer.parseInt(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));

		}

		public final long toLong() {
			return Long.parseLong(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));

		}

		public final float toFloat() {
			return Float.parseFloat(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));

		}

		public final double toDouble() {
			return Double.parseDouble(String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", ""));
		}

		@Override
		public String toString() {
			return getType() != "String" ? String.valueOf(VALUE).replace(" ", "").replace("\n", "").replace("\t", "")
					: String.valueOf(VALUE);
		}

		public final String getType() {
			Class<?> cl = VALUE.getClass();

			String fullname = cl.getName().replace(".", "/");
			if (fullname.contains("/")) {
				String[] split = fullname.split("/");
				return split.length > 0 ? split[split.length - 1] : "Object";
			} else {
				return "Object";
			}
		}

		public final String getImport() {
			Class<?> cl = VALUE.getClass();
			return "import " + cl.getName() + ";\n" + importConstructorParameters();
		}

		public final String defaultCode() {
			return NAME + " = " + toString() + ";";
		}

		public final String generateJavaCode() {
			String type = getType();
			if (type.equals("String")) {
				return "import " + getImport() + ";"
						+ "\n\npublic class Main{\n        public static void main(String[] args){\n            "
						+ getType() + " " + NAME + " = \"" + toString() + "\";\n        }\n}";

			} else {
				return getImport() + ";"
						+ "\n\npublic class Main{\n        public static void main(String[] args){\n            " + type
						+ " " + NAME + " = " + "new " + type + "(" + getConstructorParameters() + ")"
						+ ";\n        }\n}";
			}
		}

	}

	public HashMap<String, String> CONSTANTS = new HashMap<>();
	public char VARIABLE_CHAR = '$';
	public final String VARIABLE_FINDER_REGEX = "\\w*\\s*=.*;";

	public VariableInterpretter() {
		Variable PI = new Variable("PI", Math.PI);
		Variable E = new Variable("E", Math.E);
		addConstant(PI, E);
	}

	public VariableInterpretter(Variable... constants) {
		Variable PI = new Variable("PI", Math.PI);
		Variable E = new Variable("E", Math.E);
		addConstant(PI, E);
		addConstant(constants);
	}

	public final HashMap<String, Variable> parse(CharSequence text) throws Exception {
		String txt = text.toString();
		HashMap<String, Variable> parsed = new HashMap<String, Variable>();
		Pattern pattern = Pattern.compile(VARIABLE_FINDER_REGEX);
		Matcher matcher = pattern.matcher(txt);
		while (matcher.find()) {
			String line = txt.substring(matcher.start(), matcher.end());
			String[] split = line.split("=");
			if (split.length >= 2) {
				String name = split[0].trim();
				if (name.contains(" ") || name.contains("\n") || name.contains("\t")) {
					throw new IllegalArgumentException("Name cannot contain anything other than letters");
				}
				if (!name.equals("")) {
					String rawValue = split[1];
					rawValue = substitute(rawValue);
					if (rawValue.length() >= 2) {
						StringBuilder builder = new StringBuilder(rawValue);
						builder.setCharAt(rawValue.length() - 1, ' ');
						String value = builder.toString().trim();
						parsed.put(name, new Variable(name, value));

					}
				} else {
					throw new IllegalArgumentException("Name is empty");
				}
			}
		}
		return parsed.size() > 0 ? parsed : null;
	}

	public void addConstant(Variable... vars) {
		if (vars != null) {
			for (Variable v : vars) {
				if (v != null) {
					CONSTANTS.put(v.NAME, v.toString());
				}
			}
		}
	}

	public void addConstant(ArrayList<Variable> vars) {
		if (vars != null && vars.size() > 0) {
			for (int i = 0; i < vars.size(); i++) {
				Variable v = vars.get(i);
				if (v != null) {
					CONSTANTS.put(v.NAME, v.toString());
				}
			}
		}
	}

	public void addConstant(HashMap<String, Variable> vars) {
		if (vars != null && vars.size() > 0) {
			for (String key : vars.keySet()) {
				Variable v = vars.get(key);
				String name = v.NAME.replace(" ", "");
				if (!name.equals("")) {
					CONSTANTS.put(name, v.toString());
				}
			}
		}
	}

	public String substitute(CharSequence text) {
		String res = text.toString();
		for (String key : CONSTANTS.keySet()) {
			res = res.replace(String.valueOf(VARIABLE_CHAR) + key, CONSTANTS.get(key));
		}
		return res;
	}
}
