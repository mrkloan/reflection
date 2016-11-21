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

The `bogger` project is composed of various artifacts interacting with each other. Or not.
Doing things, all that kind of stuff.

I try to keep them focused on a single task in order to make them as reusable as possible.

### spark-runner

#### A little bit of history
To be honest, I discovered **Spring Boot** not so long ago and I loved the simplicity of it.
The project setup was done in a matter of seconds, and I was already addicted to the *annotation driven* development
style induced by JEE.

But hell, a simple (yet polite) REST API greeting me with a kind `"Hello, World!"` on each of its endpoints
weighted like **A FREAKING MONSTER**.

So I dove deep into the Java micro-framework world, and the second (if not first) link I found was one leading to the
[Spark Java](http://sparkjava.com/) website.

Needless to say that I immediately felt I love with its simple and efficient implementation.
And the size of the generated artifact was under 3MB! We're still talking Java here, right?

But I still felt something was wrong. Like something was missing. I read a lot of blog posts and tutorials about
"*How to avoid boilerplate code with Spark Java*", but was never satisfied.

Slowly sinking into distress, I finally realised that if a specific mechanism was not available I just had to create it.
And so did I.

#### API

`spark-runner` is an *annotation driven* API allowing you to delegate much of the already minimal Spark configuration
and verbosity.

I tried to stay *agnostic* here about the design, hence **you won't find any reference** to some *Service* or *Repository*
because, hey, do whatever you want with your code.

##### Usage

*TODO*

##### Configuration

*TODO*

#### Dependency
The `spark-runner` artifact in itself can be used as a dependency for your own projects in a simple way:

Maven:
```xml
<repositories>
    <repository>
        <id>fries-io</id>
        <name>fries.io Maven Repository</name>
        <url>http://maven.fries.io/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>io.fries</groupId>
        <artifactId>spark-runner</artifactId>
        <version>0.1-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Gradle:
```groovy
repositories {
    maven {
        url 'http://maven.fries.io/'
    }
}

dependencies {
    compile 'io.fries:spark-runner:0.1-SNAPSHOT'
}
```

### bogger-ws

In its current state, the `bogger-ws` artifact is only a playground to test the `spark-runner` API.

## Contributors

- [Valentin Fries](https://www.fries.io/)