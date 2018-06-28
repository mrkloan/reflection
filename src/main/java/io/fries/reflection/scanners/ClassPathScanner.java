package io.fries.reflection.scanners;

import io.fries.reflection.filters.Filter;
import io.fries.reflection.metadata.ResourceMetadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.jar.Attributes.Name.CLASS_PATH;

/**
 * Scan the resources of a {@link ClassLoader}'s classpath and store their simple metadata.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ClassPathScanner implements Scanner {
	
	private final ClassLoader classLoader;
	
	private final Set<Filter> filters;
	private final Set<File> scannedUris;
	private final Map<ClassLoader, Set<String>> resources;
	
	/**
	 * Create a new {@link ClassPathScanner} object that will scan the provided {@link ClassLoader}'s classpath and apply some
	 * {@link Filter}s to its resources.
	 *
	 * @param classLoader The base {@link ClassLoader} for which the {@link ClassPathScanner} has been called.
	 */
	private ClassPathScanner(final ClassLoader classLoader) {
		this.classLoader = classLoader;
		
		this.filters = new HashSet<>();
		this.scannedUris = new HashSet<>();
		this.resources = new HashMap<>();
	}
	
	/**
	 * @param classLoader The base {@link ClassLoader} for which the {@link ClassPathScanner} has been called.
	 *
	 * @return A new {@link ClassPathScanner} instance.
	 *
	 * @see ClassPathScanner#ClassPathScanner(ClassLoader)
	 */
	public static ClassPathScanner of(final ClassLoader classLoader) {
		return new ClassPathScanner(classLoader);
	}
	
	/**
	 * @param filter A filter to apply on the scanned resources.
	 *
	 * @return This {@link ClassPathScanner} instance.
	 */
	public ClassPathScanner filter(final Filter filter) {
		if(filter == null)
			throw new IllegalArgumentException("Filter cannot be null");
		
		this.filters.add(filter);
		return this;
	}
	
	/**
	 * @param filterSupplier A {@link Supplier} of any {@link Filter} instance to apply on the scanned resources.
	 *
	 * @return This {@link ClassPathScanner} instance.
	 */
	public ClassPathScanner filter(final Supplier<Filter> filterSupplier) {
		if(filterSupplier == null)
			throw new IllegalArgumentException("Filter supplier cannot be null");
		
		return filter(filterSupplier.get());
	}
	
	/**
	 * @return A copy of the scanner's resources set.
	 */
	@Override
	public Set<ResourceMetadata> getResources() {
		scanClassPath();
		
		final Set<ResourceMetadata> set = new LinkedHashSet<>();
		
		resources.forEach((classLoader, names) ->
			names.forEach(res -> set.add(ResourceMetadata.create(res, classLoader)))
		);
		
		return set;
	}
	
	/**
	 * Run a full scan of the provided {@link #classLoader} attribute.
	 */
	private void scanClassPath() {
		getClassPathEntries(classLoader).forEach(this::scan);
	}
	
	private void scan(final File file, final ClassLoader classLoader) {
		// If the current file has already been scanned, abort this method call.
		if(!scannedUris.add(file))
			return;
		
		try {
			if(!file.exists())
				return;
			
			if(file.isDirectory())
				scanDirectory(file, classLoader, "");
			else
				scanJar(file, classLoader);
		}
		catch(final SecurityException e) { /* If the file is protected and cannot be accessed */ }
	}
	
	/**
	 * For each JAR file, scan the class path resources defined in its Manifest file and re-run a full scan on them
	 * before calling the {@link #scanJarFile(JarFile, ClassLoader)} on itself.
	 *
	 * @param file        The JAR file which is to be scanned.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	private void scanJar(final File file, final ClassLoader classLoader) {
		try(final JarFile jarFile = new JarFile(file)) {
			getClassPathFromManifest(file, jarFile.getManifest()).forEach(classPathEntry -> scan(classPathEntry, classLoader));
			scanJarFile(jarFile, classLoader);
		}
		catch(final IOException e) { /* Not a JAR file */ }
	}
	
	/**
	 * List all files inside of the given directory.
	 * If the file is itself another directory, proceed with a recursive call to this method with an updated {@code packagePrefix}.
	 * Otherwise, if it is not a Manifest file, it is added as a resource in the scanner's resources set.
	 *
	 * @param dir           The directory to scan.
	 * @param classLoader   The {@link ClassLoader} the provided directory is attached to.
	 * @param packagePrefix The current name of the package we're in, constructed through recursive calls.
	 */
	private void scanDirectory(final File dir, final ClassLoader classLoader, final String packagePrefix) {
		final File[] files = dir.listFiles();
		
		if(files == null)
			return;
		
		for(File file : files) {
			final String resourceName = packagePrefix + file.getName();
			
			if(file.isDirectory())
				scanDirectory(file, classLoader, resourceName + '/');
			else if(filters.isEmpty() || filters.stream().allMatch(filter -> filter.accept(classLoader, resourceName)))
				addResource(classLoader, resourceName);
		}
	}
	
	/**
	 * For each {@link java.util.jar.JarEntry} that is not a directory nor a Manifest file,
	 * add the resource to the scanner's resources set.
	 *
	 * @param jarFile     The JAR file to scan.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	private void scanJarFile(final JarFile jarFile, final ClassLoader classLoader) {
		jarFile.stream()
			.filter(entry -> !entry.isDirectory() && !entry.getName().equals(JarFile.MANIFEST_NAME))
			.filter(entry -> filters.isEmpty() || filters.stream().allMatch(filter -> filter.accept(classLoader, entry.getName())))
			.forEach(entry -> addResource(classLoader, entry.getName()));
	}
	
	/**
	 * Scan the whole {@link ClassLoader} object and its parent in order to map their files to the correct
	 * {@link ClassLoader} instance.
	 *
	 * @param classLoader The {@link ClassLoader} object which content is to be scanned.
	 *
	 * @return A {@link Map} attaching a {@link ClassLoader} object to each of its components.
	 */
	private Map<File, ClassLoader> getClassPathEntries(final ClassLoader classLoader) {
		final Map<File, ClassLoader> entries = new LinkedHashMap<>();
		final ClassLoader parent = classLoader.getParent();
		
		if(parent != null)
			entries.putAll(getClassPathEntries(parent));
		
		if(classLoader instanceof URLClassLoader) {
			final URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
			
			stream(urlClassLoader.getURLs())
				.filter(entry -> entry.getProtocol().equals("file"))
				.map(entry -> new File(entry.getFile()))
				.filter(file -> !entries.containsKey(file))
				.forEach(file -> entries.put(file, classLoader));
		}
		
		return entries;
	}
	
	/**
	 * @param jarFile  The JAR file from which we wish to load the class path content.
	 * @param manifest The Manifest of the provided JAR file.
	 *
	 * @return A new {@link Set<File>} containing
	 */
	private Set<File> getClassPathFromManifest(final File jarFile, final Manifest manifest) {
		final String classPath;
		
		if(manifest == null || (classPath = manifest.getMainAttributes().getValue(CLASS_PATH.toString())) == null)
			return new HashSet<>();
		
		return stream(classPath.split(" "))
			.filter(path -> !path.isEmpty())
			.map(path -> getClassPathEntry(jarFile, path))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.filter(url -> url.getProtocol().equals("file"))
			.map(url -> new File(url.getFile()))
			.collect(Collectors.toSet());
	}
	
	/**
	 * @param file The JAR file referencing the provided {@code path} in its class path.
	 * @param path The path to a specific class path entry.
	 *
	 * @return A new {@link URL} object to the provided class path entry,
	 * or {@code null} if a {@link MalformedURLException} is thrown.
	 */
	private Optional<URL> getClassPathEntry(final File file, final String path) {
		try {
			return Optional.of(new URL(file.toURI().toURL(), path));
		}
		catch(final MalformedURLException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * Add a new resource to the scanner's resources set by attaching it to the provided {@link ClassLoader}.
	 * If the provided {@link ClassLoader} is not present, a new set is created and attached to it.
	 *
	 * @param classLoader  The {@link ClassLoader} object to which the new resource is attached.
	 * @param resourceName The complete name of the new resource.
	 *
	 * @return {@code true} if the resource could be added to the resources set; {@code false} otherwise.
	 */
	private boolean addResource(final ClassLoader classLoader, final String resourceName) {
		if(!resources.containsKey(classLoader))
			resources.put(classLoader, new LinkedHashSet<>());
		
		return resources.get(classLoader).add(resourceName);
	}
}
