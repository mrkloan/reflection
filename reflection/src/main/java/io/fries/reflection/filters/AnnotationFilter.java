package io.fries.reflection.filters;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Accept only the classes annotated with a certain set of annotations.
 *
 * @version 1.0
 * @since 1.0
 */
public class AnnotationFilter implements Filter {
	
	private enum Mode { ANY, ALL }
	
	private Mode mode;
	private final Set<Class<? extends Annotation>> annotations;
	
	/**
	 * @param annotations The array of annotation classes required for each resource.
	 */
	@SafeVarargs
	public AnnotationFilter(Class<? extends Annotation> ...annotations) {
		if(annotations.length == 0)
			throw new IllegalArgumentException("Filtered annotations list cannot be empty.");
		
		this.mode = Mode.ANY;
		this.annotations = new HashSet<>();
		Arrays.stream(annotations).forEach(this.annotations::add);
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(ClassLoader classLoader, String resourceName) {
		ResourceMetadata resourceMetadata = ResourceMetadata.create(resourceName, classLoader);
		
		if(!(resourceMetadata instanceof ClassMetadata))
			return false;
		
		try {
			ClassMetadata classMetadata = (ClassMetadata)resourceMetadata;
			Class<?> resourceClass = classMetadata.load();
			
			return mode == Mode.ALL ? annotations.stream().allMatch(resourceClass::isAnnotationPresent)
									: annotations.stream().anyMatch(resourceClass::isAnnotationPresent);
		}
		catch(IllegalStateException e) {
			return false;
		}
	}
	
	/**
	 * @return This {@link AnnotationFilter} instance.
	 */
	public AnnotationFilter allRequired() {
		this.mode = Mode.ALL;
		return this;
	}
}
