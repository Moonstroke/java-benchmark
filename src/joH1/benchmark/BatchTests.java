package joH1.benchmark;

import static joH1.benchmark.IOUtils.checks;


class BatchTests {

	private int value;


	private BatchTests() {
		value = 42;
	}


	protected int getValue() {
		return value;
	}

	protected void throwException() throws Exception {
		throw new Exception("crac");
	}

	protected static boolean returnTrue() {
		return true;
	}

	protected static void throwStatic() throws NullPointerException {
		throw new NullPointerException();
	}


	public static void main(String[] args) {

		BatchTests batch = new BatchTests();

		try {
			MethodBenchmark<BatchTests> bm_getValue = new MethodBenchmark<>(batch, "getValue");
			MethodBenchmark<BatchTests> bm_throwException = new MethodBenchmark<>(batch, "throwException");

			StaticMethodBenchmark sbm_returnTrue = new StaticMethodBenchmark(BatchTests.class, "returnTrue");
			StaticMethodBenchmark sbm_throwStatic = new StaticMethodBenchmark(BatchTests.class, "throwStatic");

			final Object[][] noargs = {{}};

			bm_getValue.testNoException(noargs, new Integer[] {42});
			bm_throwException.testException(noargs, checks(e -> e.getMessage().equals("crac")));

			sbm_returnTrue.testNoException(noargs, new Boolean[] {Boolean.TRUE});
			sbm_throwStatic.testException(noargs, checks(e -> e.getClass().equals(NullPointerException.class)));

			bm_getValue.timeNanos(new Object[] {}, null, true);
			sbm_returnTrue.meanTime(new Object[] {}, 100, false, null, false);

			// Failing tests
			sbm_returnTrue.log("Should fail");
			sbm_returnTrue.testException(noargs, checks(e -> e instanceof Exception));
			sbm_returnTrue.log("Should fail");
			sbm_throwStatic.testNoException(noargs, new Integer[] {42});

		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
}

