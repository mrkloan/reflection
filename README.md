# bogger

`bogger` (a silly contraction for **b**ot l**ogger**) aims to become a centralized and automated log handler
(fancy, isn't it?).

Started as a pet project during the first year of my Master's degree in *Software Architecture*,
my primary goal was to experiment with various technologies such as micro-frameworks
([*Spark Java*](http://sparkjava.com/), [*Spring Boot*](https://projects.spring.io/spring-boot/), ...),
[*Docker*](https://www.docker.com/) containers, [*Slack*](https://slack.com/) bots,
microservices architecture and so on...

We'll see how it turns out in the end!

## Artifacts

The `bogger` project is composed of various - and sometime unrelated - artifacts.
I try to keep them focused on a single task in order to make them as reusable as possible.

Here are the direct links to the sub-projects `README.md` files:

 - [reflection](reflection/README.md): a personal implementation of Google's [Guava ClassPath](https://github.com/google/guava/blob/master/guava/src/com/google/common/reflect/ClassPath.java)
 - [spark-runner](spark-runner/README.md): an *annotation driven* library for [Spark Java](http://sparkjava.com/)
 - [bogger-ws](bogger-ws/README.md): currently a playground

## Repository

Some of the artifacts may be totally functional outside of the `bogger` project context.

If you wish to give them a try, feel free to add the following repository to your project
and refer to the **Installation** section of the sub-project's `README.md`:

Maven:
```xml
<repository>
    <repository>
        <id>fries-io</id>
        <name>fries.io Maven Repository</name>
        <url>http://maven.fries.io/</url>
    </repository>
</repository>
```

Gradle:
```groovy
repositories {
    maven {
        url 'http://maven.fries.io/'
    }
}
```

## Contributors

- [Valentin Fries](https://www.fries.io/)