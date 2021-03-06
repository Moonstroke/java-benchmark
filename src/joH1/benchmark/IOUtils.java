package joH1.benchmark;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.function.Predicate;

public class IOUtils {


	/**
	 * This is a {@code static} class
	 */
	private IOUtils() {}


	/**
	 * Wraps a string between quotes, or call {@link Object#toString} on an object.
	 *
	 * @param o the object to return as a String
	 *
	 * @return a String with integrated quotes (for printing) or the object's toString()
	 */
	protected static String toQuotedString(Object o) {
		return o == null ? "null" : o instanceof String ? '"' + (String)o + '"' : o.toString();
	}

	protected static <U> int printCall(PrintStream out, Method m, U[] args, Function<U, String> f) {
		StringBuilder builder = new StringBuilder(100);
		builder.append(m.getDeclaringClass().getSimpleName()).append('.')
		      .append(m.getName()).append('(');
		final int k = args.length;
		if(k != 0) {
			builder.append(f.apply(args[0]));
			for(int i = 1; i < k; ++i)
				builder.append(", ").append(f.apply(args[i]));
		}
		builder.append(')');
		out.println(builder.toString());
		return builder.length();
	}

	/**
	 * Prints a header line before execution of the method.
	 */
	protected static void printHeader(PrintStream out, Method m) {
		out.print("Testing method: ");

		final int l = 8 + printCall(out, m, m.getParameterTypes(), (Class<?> c) -> c.getSimpleName());
		for(int i = 0; i < l; ++i)
			out.print('-');
		out.println();
	}

	@SafeVarargs
	@SuppressWarnings({"varargs", "rawtypes", "unchecked"})
	protected static <X extends Exception> Predicate<X>[] checks(Predicate<X>... checks) {
		final int l = checks.length;
		Predicate<X>[] copy = new Predicate[l];
		System.arraycopy(checks, 0, copy, 0, l);
		return copy;
	}


}
