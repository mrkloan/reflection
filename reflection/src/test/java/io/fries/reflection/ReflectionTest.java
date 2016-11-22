package io.fries.reflection;

import io.fries.reflection.metadata.ClassMetadata;
import io.fries.reflection.metadata.ResourceMetadata;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReflectionTest {
	
	@Spy
	private static Set<ResourceMetadata> resources = new HashSet<>();
	
	@InjectMocks
	private Reflection reflection;
	
	@BeforeClass
	public static void setup() {
		// Resources
		resources.add(new ResourceMetadata("some-resource.properties", null));
		resources.add(new ResourceMetadata("META-INF/superfile.xml", null));
		
		// Classes
		resources.add(new ClassMetadata("com/test/Example.class", null));
		resources.add(new ClassMetadata("com/test/ExampleUtils.class", null));
		resources.add(new ClassMetadata("com/test/handlers/DefaultHandler.class", null));
		resources.add(new ClassMetadata("com/test/handlers/meta/DefaultMetaHandler.class", null));
		resources.add(new ClassMetadata("com/test/handlers/meta/DefaultMetaHandler$Builder.class", null));
	}
	
	@Test
	public void getResources() {
		assertThat(reflection.getResources())
				.extracting(ResourceMetadata::getResource)
				.containsExactlyInAnyOrder(
					resources.stream()
							 .map(ResourceMetadata::getResource)
							 .toArray(size -> new String[resources.size()])
				);
	}
	
	@Test
	public void getSimpleResources() {
		assertThat(reflection.getSimpleResources())
				.allMatch(res -> !(res instanceof ClassMetadata))
				.extracting(ResourceMetadata::getResource)
				.containsExactlyInAnyOrder(
					"some-resource.properties",
					"META-INF/superfile.xml"
				);
	}
	
	@Test
	public void getClasses() {
		assertThat(reflection.getClasses())
				.extracting(ClassMetadata::getResource)
				.containsExactlyInAnyOrder(
					"com/test/Example.class",
					"com/test/ExampleUtils.class",
					"com/test/handlers/DefaultHandler.class",
					"com/test/handlers/meta/DefaultMetaHandler.class",
					"com/test/handlers/meta/DefaultMetaHandler$Builder.class"
				);
	}
	
	@Test
	public void getTopLevelClasses() {
		assertThat(reflection.getTopLevelClasses())
				.extracting(ClassMetadata::getResource)
				.containsExactlyInAnyOrder(
					"com/test/Example.class",
					"com/test/ExampleUtils.class",
					"com/test/handlers/DefaultHandler.class",
					"com/test/handlers/meta/DefaultMetaHandler.class"
				);
	}
	
	@Test
	public void getTopLevelClassesWithPackage() {
		assertThat(reflection.getTopLevelClasses("com.test.handlers"))
				.extracting(ClassMetadata::getResource)
				.containsExactlyInAnyOrder(
					"com/test/handlers/DefaultHandler.class"
				);
	}
	
	@Test
	public void getTopLevelClassesRecursively() {
		assertThat(reflection.getTopLevelClassesRecursively("com.test.handlers"))
				.extracting(ClassMetadata::getResource)
				.containsExactlyInAnyOrder(
					"com/test/handlers/DefaultHandler.class",
					"com/test/handlers/meta/DefaultMetaHandler.class"
				);
	}
	
	@Test
	public void getTopLevelClassesWithPackageWithoutMock() {
		Reflection reflection = Reflection.scan();
		
		assertThat(reflection.getTopLevelClasses("io.fries.reflection"))
				.extracting(ClassMetadata::getName)
				.containsExactlyInAnyOrder(
					"io.fries.reflection.Reflection",
					"io.fries.reflection.ReflectionTest"
				);
	}
}