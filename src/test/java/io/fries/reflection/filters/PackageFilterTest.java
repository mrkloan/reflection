package io.fries.reflection.filters;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PackageFilterTest {
	
	@Test
	public void validPackage() {
		final Filter filter = PackageFilter.of("com.example");
		
		final boolean accepted = filter.accept(null, "com/example/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void invalidPackage() {
		final Filter filter = PackageFilter.of("com.example");
		
		final boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void validSubPackage() {
		final Filter filter = PackageFilter.withSubpackages("com.example");
		
		final boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullPackage() {
		PackageFilter.of(null);
	}
	
	@Test
	public void validEmptyPackage() {
		final Filter filter = PackageFilter.of("");
		
		final boolean accepted = filter.accept(null, "resource.properties");
		
		assertThat(accepted).isTrue();
	}

	@Test
	public void should_not_accept_a_resource_from_a_parent_package() {
		final Filter filter = PackageFilter.of("com.example");

		final boolean accepted = filter.accept(null, "resource.properties");

		assertThat(accepted).isFalse();
	}
}