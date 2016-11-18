package spark.runner.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SparkRoute {
	enum HttpMethod { GET, POST, PUT, PATCH, DELETE, HEAD, TRACE, CONNECT, OPTIONS }

	String path();
	String accept() default "";
	String contentType() default "application/json";
	HttpMethod method() default HttpMethod.GET;
}
