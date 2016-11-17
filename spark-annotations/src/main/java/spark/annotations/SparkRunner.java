package spark.annotations;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import spark.ResponseTransformer;
import spark.Route;
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

			injectFields(component, componentClass);
			if(componentClass.isAnnotationPresent(SparkController.class))
				injectRoutes(component, componentClass);
		}
	}

	private void injectFields(Object component, Class<?> componentClass) {
		Field[] fields = componentClass.getDeclaredFields();

		for(Field field : fields) {
			if(!field.isAnnotationPresent(SparkInject.class))
				continue;

			boolean accessible = field.isAccessible();

			try {
				Object value = SparkComponentStore.get(field.getType());

				field.setAccessible(true);
				field.set(component, value);

				System.out.println(component + "." + field.getName() + " = " + value);
			}
			catch(IllegalAccessException e) {
				e.printStackTrace();
			}
			finally {
				field.setAccessible(accessible);
			}
		}
	}

	/**
	 * TODO: ResponseTransformers ? @SparkBefore, @SparkAfter.
	 * @param component
	 * @param componentClass
	 */
	private void injectRoutes(Object component, Class<?> componentClass) {
		Method[] methods = componentClass.getDeclaredMethods();

		for(Method method : methods) {
			SparkRoute sparkRoute;

			if((sparkRoute = method.getAnnotation(SparkRoute.class)) == null)
				continue;

			System.out.println("Initializing route: " + method.getName() + " (" + sparkRoute.path() + ")...");
			Route routeLambda = createSparkRoute(component, sparkRoute, method);
			method.setAccessible(true);

			switch(sparkRoute.method()) {
				case POST:
					Spark.post(sparkRoute.path(), routeLambda);
					break;
				case PUT:
					Spark.put(sparkRoute.path(), routeLambda);
					break;
				case PATCH:
					Spark.patch(sparkRoute.path(), routeLambda);
					break;
				case DELETE:
					Spark.delete(sparkRoute.path(), routeLambda);
					break;
				case HEAD:
					Spark.head(sparkRoute.path(), routeLambda);
					break;
				case TRACE:
					Spark.trace(sparkRoute.path(), routeLambda);
					break;
				case CONNECT:
					Spark.connect(sparkRoute.path(), routeLambda);
					break;
				case OPTIONS:
					Spark.options(sparkRoute.path(), routeLambda);
					break;
				default:
					Spark.get(sparkRoute.path(), routeLambda);
			}
		}
	}

	private Route createSparkRoute(Object component, SparkRoute sparkRoute, Method method) {
		return (req, res) -> {
			try {
				if(!sparkRoute.accept().isEmpty())
					res.header("Accept", sparkRoute.accept());
				res.type(sparkRoute.contentType());

				return method.invoke(component, req, res);
			}
			catch(Exception e) {
				e.printStackTrace();
				return null;
			}
		};
	}

	public static SparkRunner startApplication(Class<?> applicationClass) {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication");
		return new SparkRunner(applicationClass);
	}
}
