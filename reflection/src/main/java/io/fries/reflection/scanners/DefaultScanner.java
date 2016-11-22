package io.fries.reflection.scanners;

import java.io.File;
import java.util.jar.JarFile;

/**
 * The default {@link Scanner} that will gather all the resources in the current class path.
 *
 * @version 1.0
 * @since 1.0
 */
public final class DefaultScanner extends Scanner {
	
	/**
	 * Instantiate a new {@link DefaultScanner} object by calling the {@link Scanner} constructor.
	 * @param classLoader The base {@link ClassLoader} for which the {@link DefaultScanner} has been called.
	 */
	public DefaultScanner(ClassLoader classLoader) {
		super(classLoader);
	}
	
	/**
	 * @see #scanDirectory(File, ClassLoader, String)
	 */
	@Override
	protected void scanDirectory(File dir, ClassLoader classLoader) {
		scanDirectory(dir, classLoader, "");
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
			else if(!resourceName.equals(JarFile.MANIFEST_NAME))
				addResource(classLoader, resourceName);
		}
	}
	
	/**
	 * For each {@link java.util.jar.JarEntry} that is not a directory nor a Manifest file,
	 * add the resource to the scanner's resources set.
	 * @param jarFile The JAR file to scan.
	 * @param classLoader The {@link ClassLoader} the provided JAR file is attached to.
	 */
	@Override
	protected void scanJarFile(JarFile jarFile, ClassLoader classLoader) {
		jarFile.stream()
			   .filter(entry -> !entry.isDirectory() && !entry.getName().equals(JarFile.MANIFEST_NAME))
			   .forEach(entry -> addResource(classLoader, entry.getName()));
	}
}
