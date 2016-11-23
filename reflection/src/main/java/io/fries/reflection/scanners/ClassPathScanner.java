package io.fries.reflection.scanners;

import io.fries.reflection.filters.Filter;
import io.fries.reflection.metadata.ResourceMetadata;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Scan the resources of a {@link ClassLoader}'s classpath and store their simple metadata.
 *
 * @version 1.0
 * @since 1.0
 */
public final class ClassPathScanner implements Runnable {
	
	private final ClassLoader classLoader;
	private final Set<Filter> filters;
	
	private final Set<File> scannedUris;
	private final Map<ClassLoader, Set<String>> resources;
	
	/**
	 * Create a new {@link ClassPathScanner} object that will scan the provided {@link ClassLoader}'s classpath and apply some
	 * {@link Filter}s to its resources.
	 * @param classLoader The base {@link ClassLoader} for which the {@link ClassPathScanner} has been called.
	 * @param filters A {@link Set} of specific filters to apply to the scanned resources.
	 */
	public ClassPathScanner(ClassLoader classLoader, Set<Filter> filters) {
		this.classLoader = classLoader;
		this.filters = filters;
		
		this.scannedUris = new HashSet<>();
		this.resources = new HashMap<>();
	}
	
	/**
	 * Run a full scan of the provided {@link #classLoader} attribute.
	 */
	@Override
	public void run() {
		getClassPathEntries(classLoader).forEach(this::scan);
	}
	
	private void scan(File file, ClassLoader classLoader) {
		// If the current file has already been scanned, abort this method call.
		if(!scannedUris.add(file))
			return;
		
		try {
			if (!file.exists())
				return;
			
			if(file.isDirectory())
				scanDirectory(file, classLoader, "");
			else
				scanJar(file, classLoader);
		}
		catch(SecurityException e) { /* If the file is protected and cannot be accessed */ }
	}
	
	/**
	 * For each JAR file, scan the class path resources defined in its Manifest file and re-run a full scan on them
	 * before calling the {@link #scanJarFile(JarFile, ClassLoader)} on itself.
	 * @param file The JAR file which is to be scanned.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	private void scanJar(File file, ClassLoader classLoader) {
		JarFile jarFile = null;
		
		try {
			jarFile = new JarFile(file);
			
			getClassPathFromManifest(file, jarFile.getManifest()).forEach(classPathEntry -> scan(classPathEntry, classLoader));
			scanJarFile(jarFile, classLoader);
		}
		catch(IOException e) { /* Not a JAR file */ }
		finally {
			try {
				if (jarFile != null)
					jarFile.close();
			}
			catch(IOException ex) { /* Ignore the closure error */ }
		}
	}
	
	/**
	 * List all files inside of the given directory.
	 * If the file is itself another directory, proceed with a recursive call to this method with an updated {@code packagePrefix}.
	 * Otherwise, if it is not a Manifest file, it is added as a resource in the scanner's resources set.
	 * @param dir The directory to scan.
	 * @param classLoader The {@link ClassLoader} the provided directory is attached to.
	 * @param packagePrefix The current name of the package we're in, constructed through recursive calls.
	 */
	private void scanDirectory(File dir, ClassLoader classLoader, String packagePrefix) {
		File[] files = dir.listFiles();
		
		if(files == null)
			return;
		
		for(File file : files) {
			String resourceName = packagePrefix + file.getName();
			
			if(file.isDirectory())
				scanDirectory(file, classLoader, resourceName + '/');
			else if(filters.isEmpty() || filters.stream().allMatch(filter -> filter.accept(classLoader, resourceName)))
				addResource(classLoader, resourceName);
		}
	}
	
	/**
	 * For each {@link java.util.jar.JarEntry} that is not a directory nor a Manifest file,
	 * add the resource to the scanner's resources set.
	 * @param jarFile The JAR file to scan.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	private void scanJarFile(JarFile jarFile, ClassLoader classLoader) {
		jarFile.stream()
				.filter(entry -> !entry.isDirectory() && !entry.getName().equals(JarFile.MANIFEST_NAME))
				.filter(entry ->
					filters.isEmpty() || filters.stream().allMatch(filter -> filter.accept(classLoader, entry.getName()))
				)
				.forEach(entry -> addResource(classLoader, entry.getName()));
	}
	
	/**
	 * Scan the whole {@link ClassLoader} object and its parent in order to map their files to the correct
	 * {@link ClassLoader} instance.
	 * @param classLoader The {@link ClassLoader} object which content is to be scanned.
	 * @return A {@link Map} attaching a {@link ClassLoader} object to each of its components.
	 */
	private Map<File, ClassLoader> getClassPathEntries(ClassLoader classLoader) {
		Map<File, ClassLoader> entries = new LinkedHashMap<>();
		
		// Scan ClassLoader parent first
		ClassLoader parent = classLoader.getParent();
		if(parent != null)
			entries.putAll(getClassPathEntries(parent));
		
		if(classLoader instanceof URLClassLoader) {
			URLClassLoader urlClassLoader = (URLClassLoader)classLoader;
			
			Arrays.stream(urlClassLoader.getURLs())
				  .filter(entry -> entry.getProtocol().equals("file"))
				  .map(entry -> new File(entry.getFile()))
				  .filter(file -> !entries.containsKey(file))
				  .forEach(file -> entries.put(file, classLoader));
		}
		
		return entries;
	}
	
	/**
	 * @param jarFile The JAR file from which we wish to load the class path content.
	 * @param manifest The Manifest of the provided JAR file.
	 * @return A new {@link Set<File>} containing
	 */
	private Set<File> getClassPathFromManifest(File jarFile, Manifest manifest) {
		String classPath;
		
		if(manifest == null
		|| (classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH.toString())) == null)
			return new HashSet<>();

		return Arrays.stream(classPath.split(" "))
			  		 .filter(path -> !path.isEmpty())
			  		 .map(path -> getClassPathEntry(jarFile, path))
			  		 .filter(url -> url != null && url.getProtocol().equals("file"))
			  		 .map(url -> new File(url.getFile()))
			  		 .collect(Collectors.toSet());
	}
	
	/**
	 * @param file The JAR file referencing the provided {@code path} in its class path.
	 * @param path The path to a specific class path entry.
	 * @return A new {@link URL} object to the provided class path entry,
	 * or {@code null} if a {@link MalformedURLException} is thrown.
	 */
	private URL getClassPathEntry(File file, String path) {
		try {
			return new URL(file.toURI().toURL(), path);
		}
		catch(MalformedURLException e) {
			return null;
		}
	}
	
	/**
	 * Add a new resource to the scanner's resources set by attaching it to the provided {@link ClassLoader}.
	 * If the provided {@link ClassLoader} is not present, a new set is created and attached to it.
	 * @param classLoader The {@link ClassLoader} object to which the new resource is attached.
	 * @param resourceName The complete name of the new resource.
	 * @return {@code true} if the resource could be added to the resources set; {@code false} otherwise.
	 */
	private boolean addResource(ClassLoader classLoader, String resourceName) {
		if(!resources.containsKey(classLoader))
			resources.put(classLoader, new LinkedHashSet<>());
		return resources.get(classLoader).add(resourceName);
	}
	
	/**
	 * @return A copy of the scanner's resources set.
	 */
	public Set<ResourceMetadata> getResources() {
		Set<ResourceMetadata> set = new LinkedHashSet<>();
		
		resources.forEach((classLoader, names) ->
			names.forEach(res -> set.add(ResourceMetadata.create(res, classLoader)))
		);
		
		return set;
	}
}