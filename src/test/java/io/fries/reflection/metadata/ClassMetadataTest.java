package io.fries.reflection.metadata;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassMetadataTest {
	
	private static final String CLASS_NAME = "com/example/handlers/DefaultHandler.class";
	private static final String INNER_CLASS_NAME = "com/example/handlers/DefaultHandler$Builder.class";
	private static final String LOADABLE_CLASS = "io/fries/reflection/metadata/ClassMetadata.class";
	
	@Test
	public void getName() {
		ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		String className = classMetadata.getName();
		
		assertThat(className).isEqualTo("com.example.handlers.DefaultHandler");
	}
	
	@Test
	public void getNameOfInnerClass() {
		ClassMetadata classMetadata = new ClassMetadata(INNER_CLASS_NAME, null);
		
		String className = classMetadata.getName();
		
		assertThat(className).isEqualTo("com.example.handlers.DefaultHandler$Builder");
	}
	
	@Test
	public void getPackage() {
		ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		String classPackage = classMetadata.getPackage();
		
		assertThat(classPackage).isEqualTo("com.example.handlers");
	}
	
	@Test
	public void getSimpleName() {
		ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		String simpleName = classMetadata.getSimpleName();
		
		assertThat(simpleName).isEqualTo("DefaultHandler");
	}
	
	@Test
	public void getSimpleNameOfInnerClass() {
		ClassMetadata classMetadata = new ClassMetadata(INNER_CLASS_NAME, null);
		
		String simpleName = classMetadata.getSimpleName();
		
		assertThat(simpleName).isEqualTo("Builder");
	}
	
	@Test
	public void load() {
		ClassMetadata classMetadata = new ClassMetadata(LOADABLE_CLASS, Thread.currentThread().getContextClassLoader());
		
		Class<?> capture = classMetadata.load();
		
		assertThat(capture.getSimpleName()).isEqualTo("ClassMetadata");
		assertThat(capture.getPackage().getName()).isEqualTo("io.fries.reflection.metadata");
	}
}