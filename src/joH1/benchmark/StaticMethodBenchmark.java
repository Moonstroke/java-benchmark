package joH1.benchmark;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class StaticMethodBenchmark extends MethodBenchmark<Void> {


	/**
	 * {@inheritDoc}
	 */
	public StaticMethodBenchmark(Method method) {
		super(null, method);
		if(!Modifier.isStatic(method.getModifiers()))
			throw new IllegalArgumentException("Method \"" + method.getName() + "\" is not static");
	}

	/**
	 * {@inheritDoc}
	 */
	public StaticMethodBenchmark(Class<?> cls, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
		super(null, cls.getDeclaredMethod(methodName, paramTypes));
	}


}

