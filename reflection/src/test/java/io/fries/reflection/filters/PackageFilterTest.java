package io.fries.reflection.filters;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class PackageFilterTest {
	
	@Test
	public void validPackage()  {
		Filter filter = new PackageFilter("com.example");
		
		boolean accepted = filter.accept(null, "com/example/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void invalidPackage()  {
		Filter filter = new PackageFilter("com.example");
		
		boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void validSubPackage()  {
		Filter filter = new PackageFilter("com.example").allowSubpackages();
		
		boolean accepted = filter.accept(null, "com/example/sub/resource.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullPackage()  {
		new PackageFilter(null);
	}
	
	@Test
	public void validEmptyPackage()  {
		Filter filter = new PackageFilter("");
		
		boolean accepted = filter.accept(null, "resource.properties");
		
		assertThat(accepted).isTrue();
	}
}