package joH1.benchmark;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


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


	/**
	 * Constructor
	 */
	public MethodBenchmark(T instance, Method method) {
		if(Modifier.isPrivate(method.getModifiers()))
			throw new IllegalArgumentException("Method \"" + method.getName() + "\" is private");
		this.instance = instance;
		this.method = method;
	}

	/**
	 * This constructor retrieves the method through reflection with the provided method name and
	 * parameter types. The method <strong>must</strong> be declared.
	 *
	 * @throws NoSuchMethodException if the method could not be found
	 * @throws SecurityException if a {@linkplain SecurityManager security manager} is present and
	 *                           denies access to the method
	 *
	 * @see Class#getDeclaredMethod
	 */
	public MethodBenchmark(T instance, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
		this(instance, instance.getClass().getDeclaredMethod(methodName, paramTypes));
	}


	public void setOutputStream(PrintStream out) {
		this.out = out;
	}


	/**
	 * A test for a single invocation of the method, expecting either:
	 * <ul>
	 *     <li>a usual return value; in this case {@code checkException} <strong>must</strong> be
	 *         {@code null} ({@code null} can be a valid return value for the invocation, so
	 *         {@code expected} <strong>may</strong> be {@code null})
	 *     </li>
	 *     <li>the throwing of an exception, in this case {@code expected} <strong>must</strong> be
	 *         {@code null}, and {@code checkException} must <strong>not</strong> be {@code null}.
	 *     </li>
	 * </ul>
	 *
	 * @param args the arguments to be passed to the method
	 * @param expected the result one is expected to obtain on invocation of the method with
	 *                 these arguments. <strong>Must</strong> be {@code null} if an exception is
	 *                 expected
	 * @param checkException a {@link Predicate} that checks the exception that was thrown is
	 *                       correct. <strong>Must</strong> be {@code null} if no exception is expected.
	 *                       For example: {@code e -> e instanceof IndexOutOfBoundsException} is
	 *                       a fair enough predicate if tested on {@link java.util.ArrayList#get}
	 *
	 * @throws AssertionError if the value obtained is not {@linkplain Object#equals equals} to the
	 *                        expected value
	 * @throws IllegalArgumentException if both {@code expected} or {@code checkException} are
	 *                               {@code != null}
	 * @throws Throwable any exception caught while unexpected (ie {@code checkException} is
	 *                   {@code null} or returned {@code false})
	 */
	@SuppressWarnings("unchecked")
	public <X extends Exception> void singleTest(Object[] args, Object expected, Predicate<X> checkException)
	throws AssertionError, Throwable {

		if(expected != null && checkException != null)
			throw new IllegalArgumentException("You can't in the same time expect a result and an exception!");

		IOUtils.printCall(out, method, args, o -> o.toString());

		out.print("Expected: ");
		if(checkException != null)
			out.println("Exception");
		else
			out.println(IOUtils.toQuotedString(expected));

		out.print("Got     : ");
		Object got = null;
		try {
			got = method.invoke(instance, args);
			if(checkException != null)
				throw new AssertionError("Should have thrown an exception");

			assertEquals(got, expected);
			out.println(IOUtils.toQuotedString(got));
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Oops, method \"" + method.getName() + "\" is private", e);
		} catch(InvocationTargetException e) {
			Throwable t = e.getCause();
			if(checkException == null)
				throw t;
			else {
				assert checkException.test((X)t) : "Exception caught did not pass the test";
				out.println("Exception");
			}
		}
		out.println("OK");
		out.println();
	}

	/**
	 * Tests the method with the provided values and checks that it returned an expected value.
	 * An exception is <strong>not supposed to be thrown</strong> in this method: any exception caught will be
	 * rethrown.
	 *
	 * @param args      an array of arguments to be passed to the invoked method
	 * @param expecteds the expected results for each invocation
	 *
	 * @return the number of tests passed
	 *
	 * @throws AssertionError if the value returned by invocation does not return the expected value
	 * @throws IllegalArgumentException if {@code expecteds.length != args.length}
	 * @throws Throwable every exception thrown during invocation of the method
	 */
	public int testNoException(Object[][] args, Object[] expecteds) throws AssertionError, Throwable {
		IOUtils.printHeader(out, method);

		final int n = args.length;
		if(expecteds.length != n)
			throw new IllegalArgumentException("expecteds.length != " + n);

		for(int i = 0; i < n; ++i) {
			singleTest(args[i], expecteds[i], null);
		}
		return n;
	}

	/**
	 * Tests the method with the provided arguments and expects an exception.
	 * Not catching an exception is <strong>not</strong> nominal execution and an {@link AssertionError} will
	 * be thrown.
	 *
	 * @param args   an array of arguments to be passed to the invoked method
	 * @param checks the checks to evaluate the caught exception with
	 *
	 * @return the number of tests passed
	 *
	 * @throws AssertionError if an exception is not caught or if it does not pass the {@code check}
	 * @throws IllegalArgumentException if {@code checks.length != args.length}
	 * @throws IllegalStateException if the invocation of {@link singleTest} throws an exception
	 *                               other than {@code AssertionError}
	 */
	public <X extends Exception> int testException(Object[][] args, Predicate<X>[] checks) throws AssertionError {
		IOUtils.printHeader(out, method);

		final int n = args.length;
		if(checks.length != n)
			throw new IllegalArgumentException("checks.length != " + n);

		for(int i = 0; i < n; ++i) {
			try {
				singleTest(args[i], null, checks[i]);
			} catch(AssertionError e) {
				throw e;
			} catch(Throwable t) {
				throw new IllegalStateException("Unexpected exception caught", t);
			}
		}
		return n;
	}

	/**
	 * Checks that two objects are {@linkplain Object#equals equal}.<br>
	 * If they are not, an {@link AssertionError} is thrown.
	 *
	 * @param o1 The first object to compare
	 * @param o2 The second object to compare
	 *
	 * @throws AssertionError if the two objects are not {@link Object#equals}
	 */
	public <E> void assertEquals(E o1, E o2) throws AssertionError {
		assert (o1 == null && o2 == null) || o1.equals(o2)
		       : IOUtils.toQuotedString(o1) + " != " + IOUtils.toQuotedString(o2);
	}

	/**
	 * Writes a message in the stream with log circumstances: name of the method, of its class.
	 *
	 * @param msg The message to print
	 */
	public void log(String msg) {
		out.print(method.getDeclaringClass().getSimpleName());
		out.print('#');
		out.print(method.getName());
		out.print(": ");

		out.println(msg);
	}

	/**
	 * Executes the method with the provided parameters.
	 *
	 * @param params The arguments to give the method on invocation
	 *
	 * @return The value returned by the invocation
	 *
	 * @throws IllegalArgumentException if the provided arguments don't match the arguments' types
	 *                                  and number of the method
	 * @throws Throwable any method thrown by the invocation
	 */
	@SuppressWarnings("unchecked")
	public <E> E exec(Object... params) throws Throwable {

		IOUtils.printCall(out, method, params, IOUtils::toQuotedString);
		E res = null;
		try {
			res = (E)method.invoke(instance, params);
		} catch(IllegalAccessException e) {
			throw new IllegalStateException("Oops, method \"" + method.getName() + "\" is private", e);
		} catch(InvocationTargetException e) {
			throw e.getCause();
		}
		out.println("Returned: " + IOUtils.toQuotedString(res));
		return res;
	}


	/**
	 * Executes the method once with the given arguments, and returns the execution time,
	 * in seconds
	 *
	 * @param args The arguments to pass to the method
	 * @param dateFormatter A DateFormat to format the execution time with.
	                        Can be {@code null}
	 * @param doOutput if {@code true}, will print values in the output stream {@link out}
	 *
	 * @return The time the execution of the method took (in ms)
	 *
	 * @see timeNanos returns exec time in ns instead of ms; can be more precise for short
	 *                methods. Use that rather than this
	 */
	public long time(Object[] args, DateFormat dateFormatter, boolean doOutput) {

		long before, after, diff;

		before = System.currentTimeMillis();
		try {
			method.invoke(instance, args);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
		after = System.currentTimeMillis();

		 diff = after - before;

		if(doOutput) {
			if(dateFormatter == null)
				dateFormatter = new SimpleDateFormat("ss.SSS");

			IOUtils.printHeader(out, method);

			out.format("Time before = %d: ", before);
			out.println(dateFormatter.format(before));
			out.format("Time after  = %d: ", after);
			out.println(dateFormatter.format(after));
			out.format("Difference  = %d: ", diff);
			out.println(dateFormatter.format(diff));
			out.println();
		}
		return diff;
	}

	/**
	 * Executes the method once with the given arguments, and returns the execution time,
	 * in nanoseconds
	 *
	 * @param args The arguments to pass to the method
	 * @param dateFormatter A DateFormat to format the execution time with.
	                        Can be {@code null}
	 * @param doOutput if {@code true}, will print values in the output stream {@link out}
	 *
	 * @return The time the execution of the method took (in ns)
	 */
	public long timeNanos(Object[] args, DateFormat dateFormatter, boolean doOutput) {

		long before, after, diff;

		before = System.nanoTime();
		try {
			method.invoke(instance, args);
		} catch(IllegalAccessException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
		after = System.nanoTime();

		 diff = after - before;

		if(doOutput) {
			if(dateFormatter == null)
				dateFormatter = new SimpleDateFormat("ss.SSS");

			IOUtils.printHeader(out, method);

			out.format("Time before = %d: ", before);
			out.println(dateFormatter.format(before));
			out.format("Time after  = %d: ", after);
			out.println(dateFormatter.format(after));
			out.format("Difference  = %d: ", diff);
			out.println(dateFormatter.format(diff));
			out.println();
		}
		return diff;
	}

	/**
	 * Executes the method {@code times} times with the given arguments (the same every time), and returns the average
	 * execution time
	 *
	 * @param args The arguments to pass to the method
	 * @param useNanos if {@true}, will call {@link timeNanos} rather than {@link times}
	 * @param dateFormatter A DateFormat to format the execution time with.
	                        Can be {@code null}
	 * @param allOutput if {@code true}, will print values in the output stream {@link out}
	 *
	 * @return The average time the execution of the method took out of {times} calls
	 */
	public double meanTime(Object[] args, int times, boolean useNanos, DateFormat dateFormatter, Boolean allOutput) {

		long sum = 0L;
		double average;
		int i = times;
		boolean loopOutput = allOutput == null ? false : allOutput;

		if(useNanos)
			while(i --> 0)
				sum += timeNanos(args, dateFormatter, loopOutput);
		else
			while(i --> 0)
				sum += time(args, dateFormatter, loopOutput);

		average = ((double)sum) / ((double)times);

		if(allOutput != null) {
			IOUtils.printHeader(out, method);

			out.format("Executed method %d times, for a total of %d %sseconds%n", times, sum, useNanos ? "nano" : "milli");
			out.format("Mean execution time = %f%n%n", average);
		}
		return average;
	}

}
