package io.fries.reflection.metadata;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassMetadataTest {
	
	private static final String CLASS_NAME = "com/example/handlers/DefaultHandler.class";
	private static final String INNER_CLASS_NAME = "com/example/handlers/DefaultHandler$Builder.class";
	private static final String LOADABLE_CLASS = "io/fries/reflection/metadata/ClassMetadata.class";
	
	@Test
	public void getName() {
		final ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		final String className = classMetadata.getName();
		
		assertThat(className).isEqualTo("com.example.handlers.DefaultHandler");
	}
	
	@Test
	public void getNameOfInnerClass() {
		final ClassMetadata classMetadata = new ClassMetadata(INNER_CLASS_NAME, null);
		
		final String className = classMetadata.getName();
		
		assertThat(className).isEqualTo("com.example.handlers.DefaultHandler$Builder");
	}
	
	@Test
	public void getPackage() {
		final ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		final String classPackage = classMetadata.getPackage();
		
		assertThat(classPackage).isEqualTo("com.example.handlers");
	}
	
	@Test
	public void getSimpleName() {
		final ClassMetadata classMetadata = new ClassMetadata(CLASS_NAME, null);
		
		final String simpleName = classMetadata.getSimpleName();
		
		assertThat(simpleName).isEqualTo("DefaultHandler");
	}
	
	@Test
	public void getSimpleNameOfInnerClass() {
		final ClassMetadata classMetadata = new ClassMetadata(INNER_CLASS_NAME, null);
		
		final String simpleName = classMetadata.getSimpleName();
		
		assertThat(simpleName).isEqualTo("Builder");
	}
	
	@Test
	@SuppressWarnings("ConstantConditions")
	public void load() {
		final ClassMetadata classMetadata = new ClassMetadata(LOADABLE_CLASS, Thread.currentThread().getContextClassLoader());
		
		final Optional<Class<?>> capture = classMetadata.load();
		
		assertThat(capture).isPresent();
		assertThat(capture.get().getSimpleName()).isEqualTo("ClassMetadata");
		assertThat(capture.get().getPackage().getName()).isEqualTo("io.fries.reflection.metadata");
	}
}