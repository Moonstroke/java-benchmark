package joH1.benchmark;

import java.lang.reflect.Method;


public class StaticMethodBenchmark extends MethodBenchmark<Void> {


	/**
	 * {@inheritDoc}
	 */
	public StaticMethodBenchmark(Method method) {
		super(null, method);
	}

	/**
	 * {@inheritDoc}
	 */
	public StaticMethodBenchmark(Class<?> cls, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
		super(null, cls.getDeclaredMethod(methodName, paramTypes));
	}


}

