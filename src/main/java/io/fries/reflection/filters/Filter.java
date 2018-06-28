package io.fries.reflection.filters;

/**
 * Functional interface used to filter the scanned resources.
 *
 * @version 1.0
 * @since 1.0
 */
@FunctionalInterface
public interface Filter {
	
	/**
	 * @param classLoader The {@link ClassLoader} object the resource is attached to.
	 * @param resourceName The complete name of the resource.
	 * @return Return {@code true} is the resource matches the filter's criteria; otherwise return {@code false}.
	 */
	boolean accept(final ClassLoader classLoader, final String resourceName);
}
