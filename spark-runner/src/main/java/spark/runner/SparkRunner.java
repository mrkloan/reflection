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
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;
import spark.runner.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ResourceBundle;
import java.util.Set;

public final class SparkRunner implements Runnable {

	private final Logger logger;
	private final Class applicationClass;

	/**
	 * Create a new SparkRunner object which will take care of all the application initialisation and configuration.
	 * @param applicationClass The base class of this Spark application.
	 */
	private SparkRunner(Class applicationClass, Logger logger) {
		this.logger = logger;
		this.applicationClass = applicationClass;
	}

	@Override
	public void run() {
		try {
			initApplication();

			// Configure the application
			ResourceBundle applicationProperties = initResourceBundle();
			new SparkConfiguration(applicationProperties).run();

			// Gather all the application classes using reflection
			Reflections reflections = getReflectionEngine();

			Set<Class<?>> components = scanApplicationComponents(reflections);
			storeComponents(components);

			Set<Class<?>> webSockets = scanApplicationWebSockets(reflections);
			storeComponents(webSockets);

			// WebSockets need to be initialized first
			processWebSocketsInjection(webSockets);

			// Then we can register other components
			processComponentsInjection(components);

			// Wait for the server initialization before proceeding
			Spark.awaitInitialization();
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
	 * @return The configured reflection engine used to gather classes using their annotations.
	 */
	private Reflections getReflectionEngine() {
		return new Reflections(
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
	}

	/**
	 * @param reflections The reflection engine.
	 * @return A set of classes annotated using either the {@code SparkComponent} or {@code SparkController} annotations.
	 */
	private Set<Class<?>> scanApplicationComponents(Reflections reflections) {
		Set<Class<?>> components = reflections.getTypesAnnotatedWith(SparkComponent.class);
		Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(SparkController.class);
		components.addAll(controllers);

		return components;
	}

	/**
	 * @param reflections The reflection engine.
	 * @return A set of classes annotated using the {@code SparkWebSocket} annotation.
	 */
	private Set<Class<?>> scanApplicationWebSockets(Reflections reflections) {
		return reflections.getTypesAnnotatedWith(SparkWebSocket.class);
	}

	/**
	 * For each SparkComponent class, a new instance is created and stored in order to be injected.
	 * @param sparkComponents The set containing all component classes.
	 */
	private void storeComponents(Set<Class<?>> sparkComponents) {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;

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
	 * For each component classes, we search its fields for an injection annotation and set its value with the
	 * right component.
	 * If the component is a SparkWebSocket, then its instance will be bound to the specified path.
	 * @param sparkWebSockets The set containing all web socket classes.
	 */
	private void processWebSocketsInjection(Set<Class<?>> sparkWebSockets) {
		if(sparkWebSockets == null || sparkWebSockets.isEmpty())
			return;

		for(Class<?> webSocketClass : sparkWebSockets) {
			Object webSocket = SparkComponentStore.get(webSocketClass);

			injectFields(webSocket, webSocketClass);

			SparkWebSocket sparkWebSocket;
			if((sparkWebSocket = webSocketClass.getAnnotation(SparkWebSocket.class)) != null) {
				Spark.webSocket(sparkWebSocket.path(), webSocket);
			}
		}

		Spark.init();
	}

	/**
	 * Once the application's components have been instantiated and stored, we can proceed with the injections.
	 * For each component classes, we search its fields for an injection annotation and set its value with the
	 * right component.
	 * If the component is a SparkController, then its methods will also be scanned in order to
	 * inject its routes into Spark Routes' lambda expressions.
	 * @param sparkComponents The set containing all component classes.
	 */
	private void processComponentsInjection(Set<Class<?>> sparkComponents) {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;

		for(Class<?> componentClass : sparkComponents) {
			Object component = SparkComponentStore.get(componentClass);

			injectFields(component, componentClass);

			SparkController sparkController;
			if((sparkController = componentClass.getAnnotation(SparkController.class)) != null) {
				injectRoutes(component, sparkController, componentClass);
			}
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
	private void injectRoutes(Object component, SparkController sparkController, Class<?> componentClass) {
		Method[] methods = componentClass.getDeclaredMethods();

		String controllerPath = sparkController.path();
		if(controllerPath.endsWith("/"))
			controllerPath = controllerPath.substring(0, controllerPath.length() - 1);

		for(Method method : methods) {
			SparkRoute sparkRoute;

			if((sparkRoute = method.getAnnotation(SparkRoute.class)) == null)
				continue;

			try {
				String routePath = controllerPath + sparkRoute.path();
				Route routeLambda = createSparkRoute(component, sparkRoute, method);
				ResponseTransformer routeTransformer = (ResponseTransformer)createClassInstance(sparkRoute.transformer());

				method.setAccessible(true);

				switch (sparkRoute.method()) {
					case POST:
						Spark.post(routePath, routeLambda, routeTransformer);
						break;
					case PUT:
						Spark.put(routePath, routeLambda, routeTransformer);
						break;
					case PATCH:
						Spark.patch(routePath, routeLambda, routeTransformer);
						break;
					case DELETE:
						Spark.delete(routePath, routeLambda, routeTransformer);
						break;
					case HEAD:
						Spark.head(routePath, routeLambda, routeTransformer);
						break;
					case TRACE:
						Spark.trace(routePath, routeLambda, routeTransformer);
						break;
					case CONNECT:
						Spark.connect(routePath, routeLambda, routeTransformer);
						break;
					case OPTIONS:
						Spark.options(routePath, routeLambda, routeTransformer);
						break;
					default:
						Spark.get(routePath, routeLambda, routeTransformer);
				}
			}
			catch(SparkRunnerException e) {
				logger.error(e.getMessage(), e);
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
	 * @see SparkRunner#startApplication(Class, Logger)
	 */
	public static void startApplication(Class<?> applicationClass) {
		SparkRunner.startApplication(applicationClass, null);
	}

	/**
	 * @param applicationClass The application class, used as an entry point for package scanning
	 *                         and runtime annotations instantiations.
	 * @param logger An optional logger to be used by the SparkRunner instance.
	 *               If none is provided, a default one will be used.
	 */
	public static void startApplication(Class<?> applicationClass, Logger logger) {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication.");

		logger = (logger == null) ? LoggerFactory.getLogger(SparkRunner.class) : logger;
		new SparkRunner(applicationClass, logger).run();
	}
}
