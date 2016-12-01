package io.fries.reflection.metadata;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceMetadataTest {
	
	private static final String CLASS_RESOURCE = "com/test/Main.class";
	private static final String SIMPLE_RESOURCE = "com/test/application.properties";
	
	@Test
	public void createClass() {
		ResourceMetadata resourceMetadata = ResourceMetadata.create(CLASS_RESOURCE, null);
		assertThat(resourceMetadata).isInstanceOf(ClassMetadata.class);
	}
	
	@Test
	public void createResource() {
		ResourceMetadata resourceMetadata = ResourceMetadata.create(SIMPLE_RESOURCE, null);
		assertThat(resourceMetadata).isNotInstanceOf(ClassMetadata.class);
	}
}