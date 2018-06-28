package io.fries.reflection.metadata;

import java.net.URL;
import java.util.NoSuchElementException;

/**
 * Simple object storing a resource's metadata.
 * Can be used to access the resource through the {@link #getUrl()} method.
 *
 * @version 1.0
 * @since 1.0
 */
public class ResourceMetadata {
	
	final ClassLoader classLoader;
	private final String resourceName;
	
	/**
	 * Create a new {@link ResourceMetadata} object referencing a resource in the current class path.
	 *
	 * @param resourceName The complete name of this resource.
	 * @param classLoader  The {@link ClassLoader} object to which this resource is bound.
	 */
	public ResourceMetadata(final String resourceName, final ClassLoader classLoader) {
		this.resourceName = resourceName;
		this.classLoader = classLoader;
	}
	
	/**
	 * Check if the provided resource is a class file in order to instantiate the correct {@link ResourceMetadata} object.
	 *
	 * @param resourceName The complete name of the resource
	 * @param classLoader  The {@link ClassLoader} object to which the resource is bound.
	 *
	 * @return A newly instantiated {@link ResourceMetadata} object.
	 */
	public static ResourceMetadata create(final String resourceName, final ClassLoader classLoader) {
		return (resourceName.endsWith(ClassMetadata.CLASS_FILE_EXTENSION))
			? new ClassMetadata(resourceName, classLoader)
			: new ResourceMetadata(resourceName, classLoader);
	}
	
	/**
	 * @return The URL to this resource in the current class path.
	 */
	public URL getUrl() {
		final URL url = classLoader.getResource(resourceName);
		
		if(url == null)
			throw new NoSuchElementException(resourceName);
		
		return url;
	}
	
	/**
	 * @return The complete name of the resource.
	 */
	public String getResource() {
		return resourceName;
	}
	
	@Override
	public int hashCode() {
		return resourceName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if(!(obj instanceof ResourceMetadata))
			return false;
		
		final ResourceMetadata resource = (ResourceMetadata) obj;
		return resourceName.equals(resource.resourceName) && classLoader == resource.classLoader;
	}
	
	@Override
	public String toString() {
		return resourceName;
	}
}
