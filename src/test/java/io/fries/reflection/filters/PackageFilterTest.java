package io.fries.reflection.filters;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PackageFilterTest {
	
	@Test
	public void validPackage() {
		final Filter filter = new PackageFilter("com.example");
		
		final boolean accepted = filter.accept(null, "com/example/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void invalidPackage() {
		final Filter filter = new PackageFilter("com.example");
		
		final boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void validSubPackage() {
		final Filter filter = new PackageFilter("com.example").allowSubpackages();
		
		final boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullPackage() {
		new PackageFilter(null);
	}
	
	@Test
	public void validEmptyPackage() {
		final Filter filter = new PackageFilter("");
		
		final boolean accepted = filter.accept(null, "resource.properties");
		
		assertThat(accepted).isTrue();
	}
}