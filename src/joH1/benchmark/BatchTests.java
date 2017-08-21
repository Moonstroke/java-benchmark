package joH1.benchmark;

class BatchTests {

	private int value;


	private BatchTests() {
		value = 42;
	}


	private int getValue() {
		return value;
	}

	private void throwException() throws Exception {
		throw new Exception("crac");
	}

	private static boolean returnTrue() {
		return true;
	}

	private static void throwStatic() throws NullPointerException {
		throw new NullPointerException();
	}

	public static void main(String[] args) {

		BatchTests batch = new BatchTests();

		try {
			MethodBenchmark<BatchTests> bm_getValue = new MethodBenchmark<>(batch, "getValue");
			MethodBenchmark<BatchTests> bm_throwException = new MethodBenchmark<>(batch, "throwException");

			StaticMethodBenchmark sbm_returnTrue = new StaticMethodBenchmark(BatchTests.class, "returnTrue");
			StaticMethodBenchmark sbm_throwStatic = new StaticMethodBenchmark(BatchTests.class, "throwStatic");


			Object[][] noargs = new Object[][] {new Object[] {}};

			bm_getValue.testNoException(noargs, new Integer[] {42});
			bm_throwException.testException(noargs, IoUtils.checks(e -> e.getMessage().equals("crac")));

			sbm_returnTrue.testNoException(noargs, new Boolean[] {Boolean.TRUE});
			sbm_throwStatic.testException(noargs, IoUtils.checks(e -> e.getClass().equals(NullPointerException.class)));
		} catch(Throwable e) {
			e.printStackTrace();
		}
	}
}

