package joH1.benchmark;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;


class MethodBenchmark<T> {

	/**
	 * The instance to test.
	 * If the method is {@code static}, may be null.
	 */
	protected final T instance;

	/**
	 * The method to be tested.
	 */
	protected final Method method;

	/**
	 * The {@linkplain OutputStream output stream} to write test results to.
	 * Defaults to {@link System#err}.
	 */
	protected PrintStream out = System.err;


	public MethodBenchmark(T instance, Method method) {
		this.instance = instance;
		this.method = method;
	}


	public void setOutputStream(PrintStream out) {
		this.out = out;
	}

	/**
	 * Tests the method with the provided values and checks that it returned an expected value.
	 * An exception is <i>not supposed to be thrown</i> in this method: any exception caught will be
	 * rethrown.
	 *
	 * @param args      an array of arguments to be passed to the invoked method
	 * @param expecteds the expected results for each invocation
	 *
	 * @throws AssertionError if the value returned by invocation does not return the expected value
	 * @throws IllegalArgumentException if {@code expecteds.length != values.length}
	 * @throws Throwable every exception thrown during invocation of the method
	 */
	public void testNoException(Object[][] args, Object[] expecteds) throws AssertionError, Throwable {
		printHeader();

		final int n = expecteds.length;
		if(args.length != n)
			throw new IllegalArgumentException("args.length != " + n);

		Object[] arg;
		Object expected, got;
		for(int i = 0; i < n; ++i) {
			arg = args[i];
			printCall(arg, o -> o.toString());
			expected = expecteds[i];
			out.format("Expected: %s", expected instanceof String ? '"' + (String)expected + '"' : expected.toString());
			try {
				got = method.invoke(instance, arg);
			} catch(IllegalAccessException e) {
				throw new IllegalStateException("Oops, method \"" + method.getName() + "\" is private", e);
			} catch(InvocationTargetException e) {
				throw e.getCause();
			}
			out.format("Got     : %s", got instanceof String ? '"' + (String)got + '"' : got.toString());
			if(expected.equals(got)) {
				out.println("OK");
				out.println();
			} else {
				throw new AssertionError(expected + " != " + got);
			}
		}
	}

	protected <U> int printCall(U[] args, Function<U, String> f) {
		StringBuilder builder = new StringBuilder(64);
		builder.append(method.getDeclaringClass().getSimpleName()).append('.')
		      .append(method.getName()).append('(');
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
	protected void printHeader() {
		StringBuilder headerBuilder = new StringBuilder(64);
		out.print("Testing ");

		final int l = 8 + printCall(method.getParameterTypes(), (Class<?> c) -> c.getSimpleName());
		for(int i = 0; i < l; ++i)
			out.print('-');
		out.println();
	}

}

