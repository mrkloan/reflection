package spark.runner;

import io.fries.reflection.Reflection;
import io.fries.reflection.scanners.AnnotationScanner;
import io.fries.reflection.scanners.Scanner;
import spark.ResponseTransformer;
import spark.Route;
import spark.Spark;
import spark.runner.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ResourceBundle;
import java.util.Set;

public final class SparkRunner {

	private final Class applicationClass;

	/**
	 * Create a new SparkRunner object which will take care of all the application initialisation and configuration.
	 * @param applicationClass The base class of this Spark application.
	 */
	private SparkRunner(Class applicationClass) throws SparkRunnerException {
		this.applicationClass = applicationClass;

		Object application = initApplication();

		// Configure the application
		ResourceBundle applicationProperties = initResourceBundle();
		new SparkConfiguration(applicationProperties).run();

		// Gather all the application classes using reflection
		Reflection reflection = getReflectionEngine();

		Set<Class<?>> components = scanApplicationComponents(reflection);
		storeComponents(components);

		Set<Class<?>> webSockets = scanApplicationWebSockets(reflection);
		storeComponents(webSockets);

		// Process injections in the main Application class
		injectFields(application, applicationClass);
		injectExceptions(application, applicationClass);

		// WebSockets need to be initialized first
		processWebSocketsInjection(webSockets);

		// Then we can register other components
		processComponentsInjection(components);

		// Wait for the server initialization before proceeding
		Spark.awaitInitialization();
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
	private Reflection getReflectionEngine() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Scanner scanner = new AnnotationScanner(classLoader, SparkComponent.class, SparkController.class, SparkWebSocket.class);
		return Reflection.scan(scanner);
	}

	/**
	 * @param reflection The reflection engine.
	 * @return A set of classes annotated using either the {@code SparkComponent} or {@code SparkController} annotations.
	 */
	private Set<Class<?>> scanApplicationComponents(Reflection reflection) {
		Set<Class<?>> components = reflection.getAnnotatedTypesRecursively(SparkComponent.class, applicationClass.getPackage().getName());
		Set<Class<?>> controllers = reflection.getAnnotatedTypesRecursively(SparkController.class, applicationClass.getPackage().getName());
		components.addAll(controllers);

		return components;
	}

	/**
	 * @param reflection The reflection engine.
	 * @return A set of classes annotated using the {@code SparkWebSocket} annotation.
	 */
	private Set<Class<?>> scanApplicationWebSockets(Reflection reflection) {
		return reflection.getAnnotatedTypesRecursively(SparkWebSocket.class, applicationClass.getPackage().getName());
	}

	/**
	 * For each SparkComponent class, a new instance is created and stored in order to be injected.
	 * @param sparkComponents The set containing all component classes.
	 */
	private void storeComponents(Set<Class<?>> sparkComponents) throws SparkRunnerException {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;

		for(Class<?> componentClass : sparkComponents) {
			Object component = createClassInstance(componentClass);
			SparkComponentStore.put(component);
		}
	}

	/**
	 * Once the application's components have been instantiated and stored, we can proceed with the injections.
	 * For each component classes, we search its fields for an injection annotation and set its value with the
	 * right component.
	 * If the component is a SparkWebSocket, then its instance will be bound to the specified path.
	 * @param sparkWebSockets The set containing all web socket classes.
	 */
	private void processWebSocketsInjection(Set<Class<?>> sparkWebSockets) throws SparkRunnerException {
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
	private void processComponentsInjection(Set<Class<?>> sparkComponents) throws SparkRunnerException {
		if(sparkComponents == null || sparkComponents.isEmpty())
			return;

		for(Class<?> componentClass : sparkComponents) {
			Object component = SparkComponentStore.get(componentClass);

			injectFields(component, componentClass);
			injectExceptions(component, componentClass);

			SparkController sparkController;
			if((sparkController = componentClass.getAnnotation(SparkController.class)) != null) {
				injectFilters(component, sparkController, componentClass);
				injectRoutes(component, sparkController, componentClass);
			}
		}
	}

	/**
	 * Search the component's fields for an injection annotation and set its value with the right component.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for fields reflection.
	 */
	private void injectFields(Object component, Class<?> componentClass) throws SparkRunnerException {
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
				throw new SparkRunnerException("Unable to inject value in field " + field.getName() + " of component " + componentClass.getName(), e);
			}
			finally {
				field.setAccessible(accessible);
			}
		}
	}

	/**
	 * Scan the component's methods in order to inject its exception handlers into the Spark engine.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectExceptions(Object component, Class<?> componentClass) {
		Method[] methods = componentClass.getDeclaredMethods();

		for(Method method : methods) {
			SparkException sparkException;

			if ((sparkException = method.getAnnotation(SparkException.class)) == null)
				continue;

			method.setAccessible(true);
			Spark.exception(sparkException.value(), (ex, req, res) -> {
				try {
					method.invoke(component, ex, req, res);
				}
				catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			});
		}
	}

	/**
	 * Scan the component's methods in order to inject its filters into the Spark engine.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectFilters(Object component, SparkController sparkController, Class<?> componentClass) throws SparkRunnerException {
		Method[] methods = componentClass.getDeclaredMethods();

		try {
			String controllerPath = formatPath(sparkController.path());

			for (Method method : methods) {
				SparkFilter sparkFilter;

				if((sparkFilter = method.getAnnotation(SparkFilter.class)) == null)
					continue;

				String filterPath = controllerPath + formatPath(sparkFilter.path());
				if(filterPath.isEmpty())
					filterPath = "*";

				method.setAccessible(true);

				switch(sparkFilter.filter()) {
					case BEFORE:
						Spark.before(filterPath, (req, res) -> method.invoke(component, req, res));
						break;
					case AFTER:
						Spark.after(filterPath, (req, res) -> method.invoke(component, req, res));
						break;
				}
			}
		}
		catch(URISyntaxException e) {
			throw new SparkRunnerException("Spark component path is invalid.", e);
		}
	}

	/**
	 * Scan the component's methods in order to inject its routes into Spark Routes' lambda expressions.
	 * @param component The stored instance of a given component.
	 * @param componentClass The component's class used for methods reflection.
	 */
	private void injectRoutes(Object component, SparkController sparkController, Class<?> componentClass) throws SparkRunnerException {
		Method[] methods = componentClass.getDeclaredMethods();

		try {
			String controllerPath = formatPath(sparkController.path());

			for(Method method : methods) {
				SparkRoute sparkRoute;

				if((sparkRoute = method.getAnnotation(SparkRoute.class)) == null)
					continue;

				String routePath = controllerPath + formatPath(sparkRoute.path());
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
		}
		catch(URISyntaxException e) {
			throw new SparkRunnerException("Spark component path is invalid.", e);
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

	private String formatPath(String path) throws URISyntaxException {
		URI uri = new URI(path);

		String wellFormedUri = uri.toString().replaceAll("/{2,}", "/");
		if(wellFormedUri.endsWith("/"))
			wellFormedUri = wellFormedUri.substring(0, wellFormedUri.length() - 1);

		return wellFormedUri;
	}

	/**
	 * @param applicationClass The application class, used as an entry point for package scanning
	 *                         and runtime annotations instantiations.
	 */
	public static void startApplication(Class<?> applicationClass) throws SparkRunnerException {
		if(!applicationClass.isAnnotationPresent(SparkApplication.class))
			throw new IllegalStateException("Application class must be annotated using @SparkApplication.");
		new SparkRunner(applicationClass);
	}
}
