package spark.annotations;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import spark.Spark;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.Set;

public final class SparkRunner {

	private final Class applicationClass;

	public SparkRunner(Class applicationClass) {
		this.applicationClass = applicationClass;

		initResourceBundle();
		initComponents();
	}

	private void initResourceBundle() {
		SparkApplication sparkApplication = (SparkApplication) applicationClass.getAnnotation(SparkApplication.class);
		ResourceBundle resourceBundle = ResourceBundle.getBundle(sparkApplication.resourceBundle());

		if(resourceBundle != null)
			SparkComponentStore.put(resourceBundle);
	}

	private void initComponents() {
		Reflections reflections = new Reflections(
			new ConfigurationBuilder()
				.setScanners(
					new SubTypesScanner(false),
					new ResourcesScanner(),
					new TypeAnnotationsScanner())
				.setUrls(ClasspathHelper.forClassLoader(new ClassLoader[] {
					ClasspathHelper.contextClassLoader(),
					ClasspathHelper.staticClassLoader()
				}))
				.filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(applicationClass.getPackage().getName())))
		);

		Set<Class<?>> sparkComponents = reflections.getTypesAnnotatedWith(SparkComponent.class);
		storeComponents(sparkComponents);
		processInjections(sparkComponents);
	}

	private void storeComponents(Set<Class<?>> sparkComponents) {
		for(Class<?> componentClass : sparkComponents) {
			try {
				Object component = componentClass.newInstance();
				SparkComponentStore.put(component);

				System.out.println("Stored component: " + component);
			}
			catch(InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private void processInjections(Set<Class<?>> sparkComponents) {
		for(Class<?> componentClass : sparkComponents) {
			Object component = SparkComponentStore.get(componentClass);

			// Inject components in annotated fields
			Field[] fields = componentClass.getDeclaredFields();

			for(Field field : fields) {
				boolean accessible = field.isAccessible();
				field.setAccessible(true);

				if(field.isAnnotationPresent(SparkInject.class)) {
					try {
						Object value = SparkComponentStore.get(field.getType());
						field.set(component, value);

						System.out.println(component + "." + field.getName() + " = " + value);
					}
					catch(IllegalAccessException e) {
						e.printStackTrace();
					}
				}

				field.setAccessible(accessible);
			}

			// Inject annotated route methods
			if(componentClass.isAnnotationPresent(SparkController.class)) {
				Method[] methods = componentClass.getDeclaredMethods();

				for(Method method : methods) {
					SparkRoute route;
					boolean accessible = method.isAccessible();
					method.setAccessible(true);

					if((route = method.getAnnotation(SparkRoute.class)) != null) {
						switch (route.method()) {
							default:
								Spark.get(route.path(), (req, res) -> method.invoke(component, req, res));
						}
					}

					method.setAccessible(accessible);
				}
			}
		}
	}

	public static SparkRunner startApplication(Class<?> applicationClass) {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication");
		return new SparkRunner(applicationClass);
	}
}
