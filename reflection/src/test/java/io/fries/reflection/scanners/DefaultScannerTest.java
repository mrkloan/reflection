package io.fries.reflection.scanners;

import io.fries.reflection.Reflection;
import io.fries.reflection.metadata.ClassMetadata;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

public class DefaultScannerTest {
	
	@Test
	public void getTopLevelClasses() {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Scanner scanner = new DefaultScanner(classLoader);
		Reflection reflection = Reflection.scan(scanner);
		
		Set<ClassMetadata> classes = reflection.getTopLevelClasses("io.fries.reflection");
		
		assertThat(classes)
				.extracting(ClassMetadata::getName)
				.containsExactlyInAnyOrder(
					"io.fries.reflection.Reflection",
					"io.fries.reflection.ReflectionTest"
				);
	}
}