package joH1.benchmark;

import java.lang.reflect.Method;


public class StaticMethodBenchmark extends MethodBenchmark<Void> {


	public StaticMethodBenchmark(Method method) {
		super(null, method);
	}


}

