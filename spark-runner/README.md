# spark-runner

## A little bit of history
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

## Usage

`spark-runner` is an *annotation driven* library allowing you to delegate much of the already minimal Spark
configuration and verbosity.

I tried to stay *agnostic* here about the design, hence **you won't find any reference** to some *Service* or *Repository*
because, hey, do whatever you want with your code.

### Annotations

*TODO*

### Configuration

*TODO*

## Installation
The `spark-runner` artifact in itself can be used as a dependency for your own projects in a simple way:

Maven:
```xml
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
dependencies {
    compile 'io.fries:spark-runner:0.1-SNAPSHOT'
}
```