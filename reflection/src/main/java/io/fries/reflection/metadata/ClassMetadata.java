package io.fries.reflection.metadata;

/**
 * Simple object storing a class' metadata.
 * Can be used to load the class into its {@link ClassLoader} using the {@link #load()} method.
 *
 * @version 1.0
 * @since 1.0
 */
public class ClassMetadata extends ResourceMetadata {
	
	public static final String CLASS_FILE_EXTENSION = ".class";
	
	private final String className;
	private final String packageName;
	
	/**
	 * Create a new {@code ClassMetadata} object.
	 * While {@link #load()} has not been called, the class is not loaded into the class loader
	 * and thus no reference to it is held in any way.
	 * @param resourceName The complete name of this resource.
	 * @param classLoader The {@code ClassLoader} object to which this resource is bound.
	 */
	public ClassMetadata(String resourceName, ClassLoader classLoader) {
		super(resourceName, classLoader);
		
		this.className = resourceName.substring(0, resourceName.length() - CLASS_FILE_EXTENSION.length()).replace('/', '.');
		this.packageName = (className.contains(".")) ? className.substring(0, className.lastIndexOf('.')) : "";
	}
	
	/**
	 * Load the current class into its class loader.
	 * @return The {@code Class<?>} object resulting of the class loader operation.
	 */
	public Class<?> load() {
		try {
			return classLoader.loadClass(className);
		}
		catch(ClassNotFoundException e) {
			throw new IllegalStateException(e);
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
		int lastDollar = className.lastIndexOf('$');
		
		if(lastDollar != -1)
			return className.substring(lastDollar + 1);
		return packageName.isEmpty() ? className : className.substring(packageName.length() + 1);
	}
	
	@Override
	public String toString() {
		return className;
	}
}
