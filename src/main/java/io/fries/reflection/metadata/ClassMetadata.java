package io.fries.reflection.metadata;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * Simple object storing a class' metadata.
 * Can be used to load the class into its {@link ClassLoader} using the {@link #load()} method.
 *
 * @version 1.0
 * @since 1.0
 */
public class ClassMetadata extends ResourceMetadata {
	
	static final String CLASS_FILE_EXTENSION = ".class";
	
	private final String className;
	private final String packageName;
	
	/**
	 * Create a new {@code ClassMetadata} object.
	 * While {@link #load()} has not been called, the class is not loaded into the class loader
	 * and thus no reference to it is held in any way.
	 *
	 * @param resourceName The complete name of this resource.
	 * @param classLoader  The {@code ClassLoader} object to which this resource is bound.
	 */
	public ClassMetadata(final String resourceName, final ClassLoader classLoader) {
		super(resourceName, classLoader);
		
		this.className = resourceName.substring(0, resourceName.length() - CLASS_FILE_EXTENSION.length()).replace('/', '.');
		this.packageName = (className.contains(".")) ? className.substring(0, className.lastIndexOf('.')) : "";
	}
	
	/**
	 * Load the current class into its class loader.
	 *
	 * @return An {@link Optional} of the {@code Class<?>} object resulting of the class loader operation.
	 */
	public Optional<Class<?>> load() {
		try {
			return ofNullable(classLoader.loadClass(className));
		}
		catch(NoClassDefFoundError | ClassNotFoundException e) {
			return empty();
		}
	}
	
	/**
	 * @return The fully qualified class name.
	 */
	public String getName() {
		return className;
	}
	
	/**
	 * @return The name of the package in which the class is located.
	 */
	public String getPackage() {
		return packageName;
	}
	
	/**
	 * @return The simple name of the class (without its package name).
	 */
	public String getSimpleName() {
		final int lastDollar = className.lastIndexOf('$');
		
		return (lastDollar != -1)
			? className.substring(lastDollar + 1)
			: packageName.isEmpty() ? className : className.substring(packageName.length() + 1);
	}
	
	@Override
	public String toString() {
		return className;
	}
}
