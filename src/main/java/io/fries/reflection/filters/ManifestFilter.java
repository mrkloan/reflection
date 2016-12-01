package io.fries.reflection.filters;

import java.util.jar.JarFile;

/**
 * Accept any resource except the {@code META-INF/MANIFEST.MF} files.
 *
 * @version 1.0
 * @since 1.0
 */
public class ManifestFilter implements Filter {
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(ClassLoader classLoader, String resourceName) {
		return !resourceName.equals(JarFile.MANIFEST_NAME);
	}
}
