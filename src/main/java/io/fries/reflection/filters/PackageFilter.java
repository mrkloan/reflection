package io.fries.reflection.filters;

/**
 * Accept only the resource of a specific package.
 *
 * @version 1.0
 * @since 1.0
 */
public class PackageFilter implements Filter {

	private static final int NOT_FOUND = -1;

	private final Mode mode;
	private final String packageName;
	
	/**
	 * @param packageName The name of the base package.
	 */
	private PackageFilter(final Mode mode, final String packageName) {
		if(packageName == null)
			throw new IllegalArgumentException("Package name cannot be null. Please use an empty string (\"\") if you meant the root package.");
		
		this.mode = mode;
		this.packageName = packageName;
	}
	
	public static PackageFilter of(final String packageName) {
		return new PackageFilter(Mode.STRICT, packageName);
	}
	
	public static PackageFilter withSubpackages(final String packageName) {
		return new PackageFilter(Mode.WITH_SUBPACKAGES, packageName);
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(final ClassLoader classLoader, final String resourceName) {
		final int lastSeparator = resourceName.lastIndexOf('/');
		final boolean separatorNotFound = lastSeparator == NOT_FOUND;

		if(packageName.isEmpty())
			return separatorNotFound;
		if(separatorNotFound)
			return false;

		final String resourcePackage = resourceName.substring(0, lastSeparator).replace('/', '.');
		
		return mode == Mode.STRICT
			? resourcePackage.equals(packageName)
			: resourcePackage.startsWith(packageName);
	}
	
	private enum Mode {STRICT, WITH_SUBPACKAGES}
}
