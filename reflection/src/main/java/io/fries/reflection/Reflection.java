package io.fries.reflection;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;
import io.fries.reflection.scanners.DefaultScanner;
import io.fries.reflection.scanners.Scanner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entry point of the Reflection API.
 *
 * Use the {@link #scan(ClassLoader)} method to process all the resources in your current {@link ClassLoader},
 * or the {@link #scan(Scanner)} method to process a {@link ClassLoader}'s resources using a specific {@link Scanner}.
 *
 * Then use the various {@code get()} methods to gather the resources you need!
 *
 * No reference to any resource is created during this process: only their metadata are, which you can then use
 * to access them and thus create a reference to this specific resource.
 *
 * @version 1.0
 * @since 1.0
 */
public class Reflection {
	
	private final Set<ResourceMetadata> resources;
	
	/**
	 * Create a new {@link Reflection} object holding a {@link Set} of {@link ResourceMetadata} gathered by
	 * a specific {@link Scanner} object when calling the {@link #scan(Scanner)} method.
	 * @param resources The {@link Set<ResourceMetadata>} which were gathered by a specific {@link Scanner}.
	 */
	private Reflection(Set<ResourceMetadata> resources) {
		this.resources = resources;
	}
	
	/**
	 * When the {@code scan()} method is called without parameters, the current {@link Thread}'s {@link ClassLoader} is used.
	 * @return A new {@link Reflection} instance holding a set of {@link ResourceMetadata}.
	 */
	public static Reflection scan() {
		return scan(Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * @param classLoader The {@link ClassLoader} object which is to be scanned using the {@link DefaultScanner}.
	 * @return A new {@link Reflection} instance holding a set of {@link ResourceMetadata}.
	 */
	public static Reflection scan(ClassLoader classLoader) {
		return scan(new DefaultScanner(classLoader));
	}
	
	/**
	 * @param scanner The {@link Scanner} which will be ran to gather the required resources.
	 * @return A new {@link Reflection} instance holding a set of {@link ResourceMetadata}.
	 */
	public static Reflection scan(Scanner scanner) {
		if(scanner == null)
			throw new IllegalArgumentException("Scanner parameter cannot be null.");
		
		scanner.run();
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
				.filter(res -> res instanceof ClassMetadata)
				.map(res -> (ClassMetadata)res)
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
	 * @param packagePrefix The prefix of all the target packages.
	 * @return A set containing all the top level {@link ClassMetadata} whose package name starts with {@code packagePrefix}.
	 */
	public Set<ClassMetadata> getTopLevelClassesRecursively(String packagePrefix) {
		return getTopLevelClasses().stream()
				.filter(c -> c.getPackage().startsWith(packagePrefix))
				.collect(Collectors.toSet());
	}
}
