package io.fries.reflection.filters;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

/**
 * Accept only the classes annotated with a certain set of annotations.
 *
 * @version 1.0
 * @since 1.0
 */
public class AnnotationFilter implements Filter {
	
	private final Mode mode;
	private final Set<Class<? extends Annotation>> annotations;
	
	/**
	 * @param annotations The array of annotation classes required for each resource.
	 */
	@SafeVarargs
	private AnnotationFilter(final Mode mode, final Class<? extends Annotation>... annotations) {
		if(annotations == null || annotations.length == 0)
			throw new IllegalArgumentException("Filtered annotations list cannot be empty.");
		
		this.mode = mode;
		this.annotations = stream(annotations).collect(toSet());
	}
	
	@SafeVarargs
	public static AnnotationFilter any(final Class<? extends Annotation>... annotations) {
		return new AnnotationFilter(Mode.ANY, annotations);
	}
	
	@SafeVarargs
	public static AnnotationFilter all(final Class<? extends Annotation>... annotations) {
		return new AnnotationFilter(Mode.ALL, annotations);
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(final ClassLoader classLoader, final String resourceName) {
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
	
	private enum Mode {ANY, ALL}
}
