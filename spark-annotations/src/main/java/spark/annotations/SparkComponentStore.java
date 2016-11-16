package spark.annotations;

import java.util.HashMap;
import java.util.Map;

public final class SparkComponentStore {

	private static final Map<Class<?>, Object> sparkComponents = new HashMap<>();

	public static void put(Object o) {
		sparkComponents.put(o.getClass(), o);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(Class<T> componentClass) {
		return (T) sparkComponents.get(componentClass);
	}
}
