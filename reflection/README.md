# reflection

`reflection` is a lightweight and easy to use library allowing you to scan the content of a `ClassLoader`'s
classpath at runtime.

```java
// Print all the resources of the specified classLoader to stdout
Reflection.of(classLoader).scan()
          .getResources().forEach(System.out::println);
```

## Usage 

A single entry point is exposed to use the `reflection` library: the `Reflection` class.

Use the `Reflection.of(classLoader)` method to create a new builder object that is used for the configuration of the
reflection process.

Then each call to the `filter` method will add a custom `Filter` (see the **Filters** section below) to the
configuration of our object.

When you're all set, you just have to call the `scan` method. It will run the reflection process and return a fully
initialized `Reflection` object.

```java
Reflection r = Reflection.of(classLoader)
                         .filter(new PackageFilter("com.example")
                             .allowSubPackages()
                         ).filter(new AnnotationFilter(MyAnnotation.class))
                         .scan();
```

The `Reflection` exposes a set of simple methods:

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

In order to refine the reflection process, you can add custom `Filter` objects while building your `Reflection` instance.

Filters are simple classes implementing the `Filter` functional interface and its `accept(classLoader, resourceName)`
method:

```java
// This is a silly example. Please don't do this.
public class AngryFilter implements Filter {
	
	@Override
	public boolean accept(ClassLoader classLoader, String resourceName) {
		return false; // Because I'm ANGRY
	}
}
```

Your filter can then be used by the `Reflection.Builder`:
```java
Reflection.of(classLoader).filter(new AngryFilter()).scan();
```

3 default filters are shipped with the `reflection` library:

 - `ManifestFilter` which exclude the `META-INF/MANIFEST.MF` file.
 - `PackageFilter` which allow you to filter the resource's package *during* the reflection process.
 - `AnnotationFilter` which will only accept the classes annotated with a specific set of annotations.

## Installation

And that's it! Now you just have to add `reflection` to your project dependencies to get ready:

Maven:
```xml
<dependencies>
    <dependency>
        <groupId>io.fries</groupId>
        <artifactId>reflection</artifactId>
        <version>1.0-RC2</version>
    </dependency>
</dependencies>
```

Gradle:
```groovy
dependencies {
    compile 'io.fries:reflection:1.0-RC1'
}
```

## License 

This project is inspired by Google's [Guava ClassPath](https://github.com/google/guava/blob/master/guava/src/com/google/common/reflect/ClassPath.java)
implementation, with some personal features and improvements.

Please see [LICENSE.md](LICENSE.md) for further details.