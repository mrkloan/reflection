package io.fries.reflection;

import io.fries.reflection.filters.Filter;
import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;
import io.fries.reflection.scanners.ClassPathScanner;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entry point of the Reflection API.
 *
 * Use the {@link #of(ClassLoader)} method to create a new {@link Builder} object, used to configure
 * the reflection process. You can add a set of specific {@link Filter}s which will be used by the {@link ClassPathScanner}
 * to decide whether or not it should keep a resource's metadata.
 *
 * Once your {@link Builder} is correctly configured, simply call the {@link Builder#scan()} method to start the
 * reflection process and return a fully instantiated {@link Reflection} object!
 *
 * Then use the various {@code get} methods to gather the resources you need.
 *
 * @version 1.0
 * @since 1.0
 */
public class Reflection {
	
	private final Set<ResourceMetadata> resources;
	
	/**
	 * Create a new {@link Reflection} object holding a {@link Set} of {@link ResourceMetadata}.
	 * Only the {@link Builder} class is allowed to create a new instance of this class.
	 * @param resources The {@link Set<ResourceMetadata>} which were gathered by a specific {@link ClassPathScanner}.
	 */
	private Reflection(Set<ResourceMetadata> resources) {
		this.resources = resources;
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
				.filter(res -> res instanceof ClassMetadata)
				.map(res -> (ClassMetadata)res)
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param packageName The name of the target package.
	 * @return A set containing all the {@link ClassMetadata} in the provided package.
	 */
	public Set<ClassMetadata> getClasses(String packageName) {
		return getClasses().stream()
				.filter(c -> c.getPackage().equals(packageName))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 * @return A set containing all the {@link ClassMetadata} whose package name starts with {@code packagePrefix}.
	 */
	public Set<ClassMetadata> getClassesRecursively(String packagePrefix) {
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
	 * @return A set containing all the top level {@link ClassMetadata} in the provided package.
	 */
	public Set<ClassMetadata> getTopLevelClasses(String packageName) {
		return getTopLevelClasses().stream()
				.filter(c -> c.getPackage().equals(packageName))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 * @return A set containing all the top level {@link ClassMetadata} whose package name starts with {@code packagePrefix}.
	 */
	public Set<ClassMetadata> getTopLevelClassesRecursively(String packagePrefix) {
		return getTopLevelClasses().stream()
				.filter(c -> c.getPackage().startsWith(packagePrefix))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @return A {@link Set} of loaded types.
	 */
	public Set<Class<?>> getTypes() {
		return getClasses().stream()
				.map(c -> {
					try { return c.load(); }
					catch(IllegalStateException e) { return null; }
				}).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param packageName The name of the target package.
	 * @return A {@link Set} of loaded types in the provided package.
	 */
	public Set<Class<?>> getTypes(String packageName) {
		return getClasses(packageName).stream()
				.map(c -> {
					try { return c.load(); }
					catch(IllegalStateException e) { return null; }
				}).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param packagePrefix The prefix of all the targeted packages.
	 * @return A {@link Set} of loaded types whose package name starts with {@code packagePrefix}.
	 */
	public Set<Class<?>> getTypesRecursively(String packagePrefix) {
		return getClassesRecursively(packagePrefix).stream()
				.map(c -> {
					try { return c.load(); }
					catch(IllegalStateException e) { return null; }
				}).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation The Annotation class that must be present in the returned types.
	 * @return A {@link Set} of loaded types all annotated with the provided {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypes(Class<? extends Annotation> annotation) {
		return getTypes().stream()
				.filter(c -> c.isAnnotationPresent(annotation))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation The Annotation class that must be present in the returned types.
	 * @param packageName The name of the target package.
	 * @return A {@link Set} of loaded types in the provided package and annotated with {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypes(Class<? extends Annotation> annotation, String packageName) {
		return getTypes(packageName).stream()
				.filter(c -> c.isAnnotationPresent(annotation))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param annotation The Annotation class that must be present in the returned types.
	 * @param packagePrefix The prefix of all the targeted packages.
	 * @return A {@link Set} of loaded types whose package name starts with {@code packagePrefix} and are annotated with {@code annotation}.
	 */
	public Set<Class<?>> getAnnotatedTypesRecursively(Class<? extends Annotation> annotation, String packagePrefix) {
		return getTypesRecursively(packagePrefix).stream()
				.filter(c -> c.isAnnotationPresent(annotation))
				.collect(Collectors.toSet());
	}
	
	/**
	 * @param classLoader The base {@link ClassLoader} from which the reflection process will begin.
	 * @return A {@link Builder} object used for the configuration of the reflection process.
	 */
	public static Builder of(ClassLoader classLoader) {
		return new Builder(classLoader);
	}
	
	/**
	 * Configuration object for the {@link Reflection} class.
	 *
	 * @version 1.0
	 * @since 1.0
	 */
	public static class Builder {
		
		private final ClassLoader classLoader;
		private final Set<Filter> filters;
		
		/**
		 * @param classLoader The mandatory {@link ClassLoader} object.
		 */
		private Builder(ClassLoader classLoader) {
			if(classLoader == null)
				throw new IllegalArgumentException("ClassLoader cannot be null.");
			
			this.classLoader = classLoader;
			this.filters = new HashSet<>();
		}
		
		/**
		 * @param filter A filter to apply on the scanned resources.
		 * @return This {@link Builder} instance.
		 */
		public Builder filter(Filter filter) {
			filters.add(filter);
			return this;
		}
		
		/**
		 * Create a new {@link ClassPathScanner} to proceed with the effective reflection.
		 * @return Return the resulting {@link Reflection} object.
		 */
		public Reflection scan() {
			ClassPathScanner classPathScanner = new ClassPathScanner(classLoader, filters);
			classPathScanner.run();
			
			return new Reflection(classPathScanner.getResources());
		}
	}
}
