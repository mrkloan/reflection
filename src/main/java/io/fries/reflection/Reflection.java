package io.fries.reflection;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;
import io.fries.reflection.scanners.Scanner;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entry point of the Reflection API.
 * <p>
 * Use the {@link Reflection#of(Scanner)} method to create a new {@link Reflection} object.
 * <p>
 * Then use the various {@code get} methods to gather the resources you need.
 *
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class Reflection {
	
	private final Set<ResourceMetadata> resources;
	
	/**
	 * Create a new {@link Reflection} object holding a {@link Set} of {@link ResourceMetadata}.
	 * Only the {@link Reflection#of(Scanner)} method can be used to create a new instance of this class.
	 *
	 * @param resources The {@link Set<ResourceMetadata>} which were gathered by a specific {@link Scanner}.
	 */
	private Reflection(final Set<ResourceMetadata> resources) {
		this.resources = resources;
	}
	
	/**
	 * @param scanner The {@link Scanner} instance to which the reflection process is delegated.
	 *
	 * @return A {@link Reflection} instance initialized using the {@link Scanner} resources.
	 */
	public static Reflection of(final Scanner scanner) {
		return new Reflection(scanner.getResources());
	}
	
	/**
	 * @return A copy of the {@link #resources} attributes.
	 */
	public Set<ResourceMetadata> getResources() {
		return new HashSet<>(resources);
	}
	
	/**
	 * @return A set containing all the {@link ResourceMetadata} that are not classes.
	 */
	public Set<ResourceMetadata> getSimpleResources() {
		return getResources().stream()
			.filter(res -> !(res instanceof ClassMetadata))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @return A set containing all the {@link ClassMetadata} from the reflected resources.
	 */
	public Set<ClassMetadata> getClasses() {
		return getResources().stream()
			.filter(ClassMetadata.class::isInstance)
			.map(ClassMetadata.class::cast)
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packageName The name of the target package.
	 *
	 * @return A set containing all the {@link ClassMetadata} in the provided package.
	 */
	public Set<ClassMetadata> getClasses(final String packageName) {
		return getClasses().stream()
			.filter(c -> c.getPackage().equals(packageName))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 *
	 * @return A set containing all the {@link ClassMetadata} whose package name starts with {@code packagePrefix}.
	 */
	public Set<ClassMetadata> getClassesRecursively(final String packagePrefix) {
		return getClasses().stream()
			.filter(c -> c.getPackage().startsWith(packagePrefix))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @return A set containing all the top level {@link ClassMetadata} from the reflected resources (which mean no
	 * inner class is included).
	 */
	public Set<ClassMetadata> getTopLevelClasses() {
		return getClasses().stream()
			.filter(c -> c.getName().indexOf('$') == -1)
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packageName The name of the target package.
	 *
	 * @return A set containing all the top level {@link ClassMetadata} in the provided package.
	 */
	public Set<ClassMetadata> getTopLevelClasses(final String packageName) {
		return getTopLevelClasses().stream()
			.filter(c -> c.getPackage().equals(packageName))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 *
	 * @return A set containing all the top level {@link ClassMetadata} whose package name starts with {@code packagePrefix}.
	 */
	public Set<ClassMetadata> getTopLevelClassesRecursively(final String packagePrefix) {
		return getTopLevelClasses().stream()
			.filter(c -> c.getPackage().startsWith(packagePrefix))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @return A {@link Set} of loaded types.
	 */
	public Set<Class<?>> getTypes() {
		return getClasses().stream()
			.map(ClassMetadata::load)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packageName The name of the target package.
	 *
	 * @return A {@link Set} of loaded types in the provided package.
	 */
	public Set<Class<?>> getTypes(final String packageName) {
		return getClasses(packageName).stream()
			.map(ClassMetadata::load)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 *
	 * @return A {@link Set} of loaded types whose package name starts with {@code packagePrefix}.
	 */
	public Set<Class<?>> getTypesRecursively(final String packagePrefix) {
		return getClassesRecursively(packagePrefix).stream()
			.map(ClassMetadata::load)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation The Annotation class that must be present in the returned types.
	 *
	 * @return A {@link Set} of loaded types all annotated with the provided {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypes(final Class<? extends Annotation> annotation) {
		return getTypes().stream()
			.filter(c -> c.isAnnotationPresent(annotation))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation  The Annotation class that must be present in the returned types.
	 * @param packageName The name of the target package.
	 *
	 * @return A {@link Set} of loaded types in the provided package and annotated with {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypes(final Class<? extends Annotation> annotation, final String packageName) {
		return getTypes(packageName).stream()
			.filter(c -> c.isAnnotationPresent(annotation))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation    The Annotation class that must be present in the returned types.
	 * @param packagePrefix The prefix of all the targeted packages.
	 *
	 * @return A {@link Set} of loaded types whose package name starts with {@code packagePrefix} and are annotated with {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypesRecursively(final Class<? extends Annotation> annotation, final String packagePrefix) {
		return getTypesRecursively(packagePrefix).stream()
			.filter(c -> c.isAnnotationPresent(annotation))
			.collect(Collectors.toSet());
	}
}
