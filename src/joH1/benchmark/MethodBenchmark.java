package joH1.benchmark;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Predicate;


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
	 * A test for a single invocation of the method.
	 *
	 * @param args the arguments to be passed to the method
	 * @param expected the result one is expected to obtain on invocation of the method with
	 *                 these arguments. <i>Must</i> be {@code null} if an exception is
	 *                 expected
	 * @param checkException a {@link Predicate} that checks the exception that was thrown is
	 *                       correct. <i>Must</i> be {@code null} if no exception is expected.<br>
	 *                       For example: {@code e -> e instanceof IndexOutOfBoundsException} is
	 *                       a fair enough predicate if tested on {@link java.util.ArrayList#get}
	 *
	 * @throws AssertionError if the value obtained is not {@linkplain Object#equals equals} to the
	 *                        expected value
	 * @throws IllegalStateException if <i>not</i> either of {@code expected} or
	 *                               {@code checkException} is {@code null}
	 * @throws Throwable any exception caught while unexpected (<i>ie</i> {@code checkException} is
	 *                   {@code null} or returned {@code false})
	 */
	@SuppressWarnings("unchecked")
	public <E extends Throwable> void singleTest(Object[] args, Object expected, Predicate<E> checkException)
	throws AssertionError, IllegalStateException, Throwable {

		if(!(expected != null && checkException != null))
			throw new IllegalStateException("You can't in the same time expect a result and an exception!");

		IoUtils.printCall(out, method, args, o -> o.toString());

		out.print("Expected: ");
		if(checkException != null)
			out.println("Exception");
		else
			out.println(IoUtils.toQuotedString(expected));
		Object got = null;
		try {
			got = method.invoke(instance, args);
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Oops, method \"" + method.getName() + "\" is private", e);
		} catch(InvocationTargetException e) {
			if(checkException == null)
				throw e.getCause();
			else {
				assert checkException.test((E)e) : "Exception caught did not pass the test";
			}
		}
		out.format("Got     : %s", IoUtils.toQuotedString(got));
		if(got == null || got.equals(expected)) {
			out.println("OK");
			out.println();
		} else {
			throw new AssertionError(expected + " != " + got);
		}
	}

	/**
	 * Tests the method with the provided values and checks that it returned an expected value.
	 * An exception is <i>not supposed to be thrown</i> in this method: any exception caught will be
	 * rethrown.
	 *
	 * @param args      an array of arguments to be passed to the invoked method
	 * @param expecteds the expected results for each invocation
	 *
	 * @return the number of tests passed
	 *
	 * @throws AssertionError if the value returned by invocation does not return the expected value
	 * @throws IllegalArgumentException if {@code expecteds.length != values.length}
	 * @throws Throwable every exception thrown during invocation of the method
	 */
	public int testNoException(Object[][] args, Object[] expecteds) throws AssertionError, IllegalStateException, Throwable {
		IoUtils.printHeader(out, method);

		final int n = expecteds.length;
		if(args.length != n)
			throw new IllegalArgumentException("args.length != " + n);

		for(int i = 0; i < n; ++i) {
			singleTest(args[i], expecteds[i], null);
		}
		return n;
	}

}

