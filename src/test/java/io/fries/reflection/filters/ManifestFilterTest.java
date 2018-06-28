package io.fries.reflection.filters;

import org.junit.Test;

import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.*;

public class ManifestFilterTest {
	
	@Test
	public void accept() {
		final Filter filter = new ManifestFilter();
		
		final boolean accepted = filter.accept(null, "application.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void deny() {
		final Filter filter = new ManifestFilter();
		
		final boolean accepted = filter.accept(null, JarFile.MANIFEST_NAME);
		
		assertThat(accepted).isFalse();
	}
	
}