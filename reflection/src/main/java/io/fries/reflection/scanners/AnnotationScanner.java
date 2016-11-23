package io.fries.reflection.scanners;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A custom {@link Scanner} that will only accept classes annotated with any of the annotations specified during the
 * scanner instantiation.
 *
 * PLEASE NOTE that the use of this scanner will force-load all the scanned classes in their respective {@code ClassLoader}.
 *
 * @version 1.0
 * @since 1.0
 */
public class AnnotationScanner extends Scanner {
	
	private final Set<Class<? extends Annotation>> annotations;
	
	/**
	 * Instantiate a new {@link DefaultScanner} object by calling the {@link Scanner} constructor.
	 * @param classLoader The base {@link ClassLoader} for which the {@link DefaultScanner} has been called.
	 * @param annotations A variable list of Annotation classes which are required for the resource to be accepted.
	 */
	@SafeVarargs
	public AnnotationScanner(ClassLoader classLoader, Class<? extends Annotation> ...annotations) {
		super(classLoader);
		
		if(annotations.length == 0)
			throw new IllegalArgumentException("AnnotationScanner cannot scan without annotations.");
		
		this.annotations = new HashSet<>();
		Arrays.stream(annotations).forEach(this.annotations::add);
	}
	
	/**
	 * @param classLoader The {@link ClassLoader} object the resource is attached to.
	 * @param resourceName The complete name the resource.
	 * @return Return {@code true} if the resource is a class and is annotated with all the scanner {@link #annotations}.
	 */
	@Override
	protected boolean acceptResource(ClassLoader classLoader, String resourceName) {
		ResourceMetadata resourceMetadata = ResourceMetadata.create(resourceName, classLoader);
		
		if(!(resourceMetadata instanceof ClassMetadata))
			return false;
		
		try {
			ClassMetadata classMetadata = (ClassMetadata)resourceMetadata;
			Class<?> resourceClass = classMetadata.load();
			
			return annotations.stream().allMatch(resourceClass::isAnnotationPresent);
		}
		catch(IllegalStateException e) {
			return false;
		}
	}
}
