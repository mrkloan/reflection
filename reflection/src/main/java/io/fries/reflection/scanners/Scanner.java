package io.fries.reflection.scanners;

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
 * An abstract class used to create new implementations of runtime {@link ClassLoader} scanners.
 * This abstract class is the one that is manipulated by the {@code Reflection#scan(File, ClassLoader)} method.
 *
 * @version 1.0
 * @since 1.0
 */
public abstract class Scanner implements Runnable {
	
	private final ClassLoader classLoader;
	private final Set<File> scannedUris;
	private final Map<ClassLoader, Set<String>> resources;
	
	/**
	 * Create a new {@link Scanner} object through one of its subclasses.
	 * Depending on the logic implemented by its {@link #scanDirectory(File, ClassLoader)} and {@link #scanJarFile(JarFile, ClassLoader)}
	 * methods, this {@link Scanner} will behave differently and return substantially different resources set.
	 * @param classLoader The base {@link ClassLoader} for which the {@link Scanner} has been called.
	 */
	Scanner(ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.scannedUris = new HashSet<>();
		this.resources = new HashMap<>();
	}
	
	/**
	 * Each {@link Scanner} subclass has to define its own logic when it comes to scanning directories.
	 * @param dir The directory to scan.
	 * @param classLoader The {@link ClassLoader} the provided directory is attached to.
	 */
	protected abstract void scanDirectory(File dir, ClassLoader classLoader);
	
	/**
	 * Each {@link Scanner} subclass has to define its own logic when it comes to scanning jar files.
 	 * @param jarFile The JAR file to scan.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	protected abstract void scanJarFile(JarFile jarFile, ClassLoader classLoader);
	
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
				scanDirectory(file, classLoader);
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
	protected boolean addResource(ClassLoader classLoader, String resourceName) {
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
