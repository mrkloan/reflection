package io.fries.reflection.scanners;

import java.util.jar.JarFile;

/**
 * The default {@link Scanner} that will gather all the resources in the current class path.
 *
 * @version 1.0
 * @since 1.0
 */
public final class DefaultScanner extends Scanner {
	
	/**
	 * Instantiate a new {@link DefaultScanner} object by calling the {@link Scanner} constructor.
	 * @param classLoader The base {@link ClassLoader} for which the {@link DefaultScanner} has been called.
	 */
	public DefaultScanner(ClassLoader classLoader) {
		super(classLoader);
	}
	
	/**
	 * @param classLoader The {@link ClassLoader} object the resource is attached to.
	 * @param resourceName The complete name the resource.
	 * @return Return {@code true} for any resource, except the MANIFEST files.
	 */
	@Override
	protected boolean acceptResource(ClassLoader classLoader, String resourceName) {
		return (!resourceName.equals(JarFile.MANIFEST_NAME));
	}
	
}
