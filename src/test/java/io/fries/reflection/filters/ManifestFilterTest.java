package io.fries.reflection.filters;

import org.junit.Test;

import java.util.jar.JarFile;

import static org.assertj.core.api.Assertions.*;

public class ManifestFilterTest {
	
	@Test
	public void accept() {
		Filter filter = new ManifestFilter();
		
		boolean accepted = filter.accept(null, "application.properties");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void deny() {
		Filter filter = new ManifestFilter();
		
		boolean accepted = filter.accept(null, JarFile.MANIFEST_NAME);
		
		assertThat(accepted).isFalse();
	}
	
}