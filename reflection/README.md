# reflection

`reflection` is a simple library allowing you to scan the content of a `ClassLoader`'s classpath at runtime.

```java
// Print all the resources to stdout
Reflection.scan().getResources().forEach(System.out::println);
```

## Usage 

A single entry point is exposed to use the `reflection` library: the `Reflection` class,
with its `Reflection.scan(...)` method.

3 variants of the `scan` method can be used:

Without any parameter. in that case, `Thread.currentThread().getContextClassLoader()` is used with the `DefaultScanner`:
```java
Reflection reflection = Reflection.scan();
```

Obviously, you can specify a `ClassLoader` (the `DefaultScanner` is also used):
```java
// You can scan any ClassLoader instance
Reflection reflection = Reflection.scan(classLoader);
```

Or you can even use a custom `Scanner` implementation:
```java
// Call your Scanner with any ClassLoader instance, and send it to the method
Reflection reflection = Reflection.scan(new MyScanner(classLoader));
```

Once your `Reflection` object has been initialized, you can access the resources metadata through a set of simple methods:

 - `getResources()` return all the scanned resources
 - `getSimpleResources()` return all the non-class resources (`.properties`, `.xml`, ...)
 - `getClasses()` return all the classes gathered by the `Scanner`
 - `getTopLevelClasses()` return only the top level classes (*inner classes* are ignored)
 - `getTopLevelClasses(String packageName)` return only the top level classes located in a specific package
 - `getTopLevelClassesRecursively(String packagePrefix)` return all the top level classes by recursively searching into
    sub-packages of `packagePrefix`

## Scanners

`Scanner`s are classes that contain all the logic of the reflection process.

You can create your own reflection logic by extending the `Scanner` abstract class: 
```java
public class MyScanner extends Scanner {
    
    public MyScanner(ClassLoader classLoader) {
        super(classLoader);
    }
    
    @Override
    protected void scanDirectory(File dir, ClassLoader classLoader) {
        // Do stuff with the directory
    }
    
    @Override
    protected void scanJarFile(JarFile jarFile, ClassLoader classLoader) {
        // Do stuff with the JAR file
    }
}
```

The method `addResource(ClassLoader, String)` will then allow you store a resource into the `Scanner` context
before handing it to the `Reflection` instance.

The `reflection` default scanners are located in the `io.fries.reflection.scanners` package: 
 
 - `DefaultScanner` store all the classpath resources
 - `AnnotationScanner` store all the classes annotated with the provided annotations

Please note that the *weight* of the reflection process highly depends on the `Scanner` implementation you use.

While the `DefaultScanner` will accept any resource without loading them, it is mandatory for the `AnnotationScanner`
to load the classes in the `ClassLoader` in order to inspect their annotations.

## Installation

And that's it! Now you just have to add `reflection` to your project dependencies to get ready:

Maven:
```xml
<dependencies>
    <dependency>
        <groupId>io.fries</groupId>
        <artifactId>reflection</artifactId>
        <version>1.0-RC1</version>
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

This project is a personal implementation of Google's [Guava ClassPath](https://github.com/google/guava/blob/master/guava/src/com/google/common/reflect/ClassPath.java).

Please see [LICENSE.md](LICENSE.md) for further details.