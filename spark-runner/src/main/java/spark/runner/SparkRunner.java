package spark.runner;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;
import spark.Spark;
import spark.runner.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.Set;

public final class SparkRunner {

	private final Logger logger;
	private final Class applicationClass;

	private ResourceBundle applicationProperties;
	private Set<Class<?>> sparkComponents;

	/**
	 * Create a new SparkRunner object which will take care of all the application initialisation and configuration.
	 * @param applicationClass The base class of this Spark application.
	 */
	private SparkRunner(Class applicationClass, Logger logger) {
		this.logger = logger;
		this.applicationClass = applicationClass;

		try {
			initApplication();

			this.applicationProperties = initResourceBundle();
			configureApplication(applicationProperties);

			this.sparkComponents = scanApplicationComponents();
			storeComponents(sparkComponents);
			processInjections(sparkComponents);
		}
		catch(SparkRunnerException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @return A new instance of the main application class.
	 * @throws SparkRunnerException When the main application class cannot be instantiated.
	 */
	private Object initApplication() throws SparkRunnerException {
		Object app = createClassInstance(applicationClass);
		return SparkComponentStore.put(app);
	}

	/**
	 * @return The instance of the application's ResourceBundle, used to configure Spark
	 * 		   and store some application specific properties.
	 * @throws SparkRunnerException When the application's ResourceBundle cannot be accessed.
	 */
	private ResourceBundle initResourceBundle() throws SparkRunnerException {
		SparkApplication sparkApplication = (SparkApplication) applicationClass.getAnnotation(SparkApplication.class);
		ResourceBundle resourceBundle = ResourceBundle.getBundle(sparkApplication.resourceBundle());

		if(resourceBundle == null)
			throw new SparkRunnerException("Application ResourceBundle cannot be null.");
		return SparkComponentStore.put(resourceBundle);
	}

	/**
	 * TODO
	 */
	private void configureApplication(ResourceBundle applicationProperties) {

	}

	/**
	 * @return A set containing all components' class using the Application's package as the base package for scanning.
	 */
	private Set<Class<?>> scanApplicationComponents() {
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

		return reflections.getTypesAnnotatedWith(SparkComponent.class);
	}

	/**
	 * For each SparkComponent class, a new instance is created and stored in order to be injected.
	 * @param sparkComponents The set containing all components' class.
	 */
	private void storeComponents(Set<Class<?>> sparkComponents) {
		for(Class<?> componentClass : sparkComponents) {
			try {
				Object component = createClassInstance(componentClass);
				SparkComponentStore.put(component);
			}
			catch(SparkRunnerException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * Once the application's components have been instantiated and stored, we can proceed with the injections.
	 * For each components' class, we search its fields for an injection annotation and set its value with the
	 * right component. If the component is a SparkController, then its methods will also be scanned in order to
	 * inject its route into Spark Routes' lambda expressions.
	 * @param sparkComponents The set containing all components' class.
	 */
	private void processInjections(Set<Class<?>> sparkComponents) {
		for(Class<?> componentClass : sparkComponents) {
			Object component = SparkComponentStore.get(componentClass);

			injectFields(component, componentClass);
			if(componentClass.isAnnotationPresent(SparkController.class))
				injectRoutes(component, componentClass);
		}
	}

	/**
	 * Search the component's fields for an injection annotation and set its value with the right component.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for fields reflection.
	 */
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
			}
			catch(IllegalAccessException e) {
				logger.error(e.getMessage(), e);
			}
			finally {
				field.setAccessible(accessible);
			}
		}
	}

	/**
	 * Scan the component's methods in order to inject its route into Spark Routes' lambda expressions.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectRoutes(Object component, Class<?> componentClass) {
		Method[] methods = componentClass.getDeclaredMethods();

		for(Method method : methods) {
			SparkRoute sparkRoute;

			if((sparkRoute = method.getAnnotation(SparkRoute.class)) == null)
				continue;

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

	/**
	 * @param component The SparkComponent object from which the method will be invoked.
	 * @param sparkRoute The component's SparkRoute annotation containing the route's metadata.
	 * @param method The class' method to be invoked when the given route is reached.
	 * @return A Spark Route object to be bound to a certain endpoint.
	 */
	private Route createSparkRoute(Object component, SparkRoute sparkRoute, Method method) {
		return (req, res) -> {
			if(!sparkRoute.accept().isEmpty())
				res.header("Accept", sparkRoute.accept());
			res.type(sparkRoute.contentType());

			return method.invoke(component, req, res);
		};
	}

	/**
	 * @param c The class we wish to instantiate.
	 * @return An instantiated object using either an empty class constructor or the default Object constructor.
	 * @throws SparkRunnerException If none of the two instantiation methods were successful.
	 */
	private Object createClassInstance(Class<?> c) throws SparkRunnerException {
		try {
			return c.getConstructor().newInstance();
		}
		catch(InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			/* Do nothing */
		}

		try {
			return c.newInstance();
		}
		catch(InstantiationException | IllegalAccessException e) {
			throw new SparkRunnerException(e.getMessage(), e);
		}
	}

	/**
	 * @return The application's ResourceBundle.
	 */
	public ResourceBundle getApplicationProperties() {
		return applicationProperties;
	}

	/**
	 * @return The set containing all components' class.
	 */
	public Set<Class<?>> getSparkComponents() {
		return sparkComponents;
	}

	/**
	 * @see SparkRunner#startApplication(Class, Logger)
	 */
	public static SparkRunner startApplication(Class<?> applicationClass) {
		return SparkRunner.startApplication(applicationClass, null);
	}

	/**
	 * @param applicationClass The application class, used as an entry point for package scanning
	 *                         and runtime annotations instantiations.
	 * @param logger An optional logger to be used by the SparkRunner instance.
	 *               If none is provided, a default one will be used.
	 * @return A new SparkRunner instance that can be used to access reflected classes and various application metadata.
	 */
	public static SparkRunner startApplication(Class<?> applicationClass, Logger logger) {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication.");

		logger = (logger == null) ? LoggerFactory.getLogger(SparkRunner.class) : logger;
		return new SparkRunner(applicationClass, logger);
	}
}
