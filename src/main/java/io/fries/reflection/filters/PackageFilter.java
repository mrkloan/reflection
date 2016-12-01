package io.fries.reflection.filters;

/**
 * Accept only the resource of a specific package.
 *
 * @version 1.0
 * @since 1.0
 */
public class PackageFilter implements Filter {
	
	private enum Mode { STRICT, ALLOW_SUBPACKAGES }
	
	private Mode mode;
	private final String packageName;
	
	/**
	 * @param packageName The name of the base package.
	 */
	public PackageFilter(String packageName) {
		if(packageName == null)
			throw new IllegalArgumentException("Package name cannot be null. Please use an empty string (\"\") if you meant the root package.");
		
		this.mode = Mode.STRICT;
		this.packageName = packageName;
	}
	
	/**
	 * @see Filter#accept(ClassLoader, String)
	 */
	@Override
	public boolean accept(ClassLoader classLoader, String resourceName) {
		int lastSeparator = resourceName.lastIndexOf('/');
		
		if(packageName.isEmpty())
			return lastSeparator == -1;
		
		String resourcePackage = resourceName.substring(0, lastSeparator).replace('/', '.');
		
		return mode == Mode.STRICT ? resourcePackage.equals(packageName)
								   : resourcePackage.startsWith(packageName);
	}
	
	/**
	 * @return This {@link PackageFilter} instance.
	 */
	public PackageFilter allowSubpackages() {
		this.mode = Mode.ALLOW_SUBPACKAGES;
		return this;
	}
}
