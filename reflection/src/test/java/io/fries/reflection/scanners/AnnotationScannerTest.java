package io.fries.reflection.scanners;

import io.fries.reflection.Reflection;
import io.fries.reflection.metadata.ClassMetadata;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class AnnotationScannerTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void emptyAnnotationSetShouldThrowAnException() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		
		new AnnotationScanner(classLoader);
	}
	
	@Test
	public void getTopLevelAnnotatedClasses() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Scanner scanner = new AnnotationScanner(classLoader, RunWith.class);
		Reflection reflection = Reflection.scan(scanner);
		
		Set<ClassMetadata> classes = reflection.getTopLevelClasses("io.fries.reflection");
		
		assertThat(classes)
				.extracting(ClassMetadata::getName)
				.containsExactlyInAnyOrder(
					"io.fries.reflection.ReflectionTest"
				);
	}
}