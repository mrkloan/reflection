# reflection

`reflection` is a lightweight and easy to use library allowing you to scan the content of a `ClassLoader`'s
classpath at runtime.

```java
// Print all the resources of the specified classLoader to stdout
Reflection
	.of(ClassPathScanner.of(classLoader))
	.getResources()
	.forEach(System.out::println);
```

## Usage 

A single entry point is exposed to use the `reflection` library: the `Reflection` class, which requires a `Scanner`
instance to provide resources metadata.

Use the `ClassPathScanner.of(instance)` method to create a new scanner object that will perform the reflection process.
You can call the `filter` method to add a custom `Filter` (see the **Filters** section below) applied during the 
classpath scanning.

When you're all set, you just have to call the `Reflection.of(scanner)` method. It will run the reflection process and
return a fully initialized `Reflection` object.

```java
final Scanner scanner = ClassPathScanner
	.of(classLoader)
	.filter(PackageFilter.withSubpackages("com.example"))
	.filter(AnnotationFilter.all(MyAnnotation.class));

final Reflection reflection = Reflection.of(scanner);
```

You can obviously create your own implementation of the `Scanner` interface, and use it as the configuration object for
a `Reflection` instance.

The `Reflection` object exposes a set of simple methods:

 - `getResources()` return all the scanned resources metadata.
 - `getSimpleResources()` return all the non-class resources (`.properties`, `.xml`, ...) metadata.
 
 
 - `getClasses()` return all the classes metadata.
 - `getClasses(packageName)` return all classes metadata of resources located in a specific package.
 - `getClassesRecursively(packagePrefix)` return all classes metadata by recursively searching into subpackages of `packagePrefix`.
 
 
 - `getTopLevelClasses()` return only the top level classes (*inner classes* are ignored) metadata.
 - `getTopLevelClasses(String packageName)` return only the top level classes metadata of resources located in a specific package.
 - `getTopLevelClassesRecursively(String packagePrefix)` return all the top level classes metadata by recursively searching
	into subpackages of `packagePrefix`.


 - `getTypes()` load and return all the scanned classes.
 - `getTypes(packageName)` load and return all the classes located in a specific package.
 - `getTypesRecursively(packagePrefix)` load and return all the classes by recursively searching into subpackages of `packagePrefix`.
 
 
 - `getAnnotatedTypes(annotation)` load and return all the classes annotated with the specified annotation.
 - `getAnnotatedTypes(annotation, packageName)` load and return all the classes annotated with the specified annotation and
	located in a specific package.
 - `getAnnotatedTypesRecursively(annotation, packagePrefix)` load and return all the classes annotated with the specified
	annotation by recursively searching into subpackages of `packagePrefix`.

## Filters

In order to refine the reflection process, you can add custom `Filter` objects while building your `ClassPathScanner` 
instance.

Filters are simple classes implementing the `Filter` functional interface and its `accept(classLoader, resourceName)`
method:

```java
// This is a silly example. Please don't do this.
public class AngryFilter implements Filter {
	
	@Override
	public boolean accept(final ClassLoader classLoader, final String resourceName) {
		return false;
	}
}
```

Your filter can then be used during the `ClassPathScanner` configuration phase:
```java
ClassPathScanner
	.of(classLoader)
	.filter(AngryFilter::new);
```

Simple filters can also be implemented using a lambda:
```java
ClassPathScanner
	.of(classLoader)
	.filter((resourceClassLoader, resourceName) -> resourceName.contains("Filter"));
```

3 default filters are shipped with the `reflection` library:

 - `ManifestFilter` which excludes the `META-INF/MANIFEST.MF` file.
 - `PackageFilter` which allows you to filter the resource's package *during* the reflection process.
 - `AnnotationFilter` which will only accept the classes annotated with a specific set of annotations.

## Installation

Gradle:
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
	implementation 'com.github.MrKloan:reflection:master'
}
```

Maven:
```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>

<dependencies>
	<dependency>
		<groupId>com.github.MrKloan</groupId>
		<artifactId>reflection</artifactId>
		<version>master</version>
	</dependency>
</dependencies>
```