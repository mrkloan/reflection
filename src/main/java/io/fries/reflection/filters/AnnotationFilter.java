package io.fries.reflection.filters;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Accept only the classes annotated with a certain set of annotations.
 *
 * @version 1.0
 * @since 1.0
 */
public class AnnotationFilter implements Filter {
	
	private final Set<Class<? extends Annotation>> annotations;
	private Mode mode;
	
	/**
	 * @param annotations The array of annotation classes required for each resource.
	 */
	@SafeVarargs
	public AnnotationFilter(Class<? extends Annotation>... annotations) {
		if(annotations.length == 0)
			throw new IllegalArgumentException("Filtered annotations list cannot be empty.");
		
		this.mode = Mode.ANY;
		this.annotations = new HashSet<>();
		this.annotations.addAll(asList(annotations));
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(ClassLoader classLoader, String resourceName) {
		final ResourceMetadata resourceMetadata = ResourceMetadata.create(resourceName, classLoader);
		
		if(!(resourceMetadata instanceof ClassMetadata))
			return false;
		
		final ClassMetadata classMetadata = (ClassMetadata) resourceMetadata;
		final Optional<Class<?>> loadedResource = classMetadata.load();
		
		if(!loadedResource.isPresent())
			return false;
		
		final Class<?> resourceClass = loadedResource.get();
		
		return mode == Mode.ALL
			? annotations.stream().allMatch(resourceClass::isAnnotationPresent)
			: annotations.stream().anyMatch(resourceClass::isAnnotationPresent);
	}
	
	/**
	 * @return This {@link AnnotationFilter} instance.
	 */
	public AnnotationFilter allRequired() {
		this.mode = Mode.ALL;
		return this;
	}
	
	private enum Mode {ANY, ALL}
}
