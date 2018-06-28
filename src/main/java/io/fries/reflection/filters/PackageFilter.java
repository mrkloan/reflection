package io.fries.reflection.filters;

/**
 * Accept only the resource of a specific package.
 *
 * @version 1.0
 * @since 1.0
 */
public class PackageFilter implements Filter {
	
	private final String packageName;
	private Mode mode;
	/**
	 * @param packageName The name of the base package.
	 */
	public PackageFilter(final String packageName) {
		if(packageName == null)
			throw new IllegalArgumentException("Package name cannot be null. Please use an empty string (\"\") if you meant the root package.");
		
		this.mode = Mode.STRICT;
		this.packageName = packageName;
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(final ClassLoader classLoader, final String resourceName) {
		final int lastSeparator = resourceName.lastIndexOf('/');
		
		if(packageName.isEmpty())
			return lastSeparator == -1;
		
		final String resourcePackage = resourceName.substring(0, lastSeparator).replace('/', '.');
		
		return mode == Mode.STRICT
			? resourcePackage.equals(packageName)
			: resourcePackage.startsWith(packageName);
	}
	
	/**
	 * @return This {@link PackageFilter} instance.
	 */
	public PackageFilter allowSubpackages() {
		this.mode = Mode.ALLOW_SUBPACKAGES;
		return this;
	}
	
	private enum Mode {STRICT, ALLOW_SUBPACKAGES}
}
