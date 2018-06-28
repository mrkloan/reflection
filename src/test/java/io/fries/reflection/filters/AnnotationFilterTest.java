package io.fries.reflection.filters;

import org.junit.Test;
import org.junit.runner.RunWith;

import static java.lang.Thread.currentThread;
import static org.assertj.core.api.Assertions.*;

public class AnnotationFilterTest {
	
	@Test
	public void accept() {
		final Filter filter = AnnotationFilter.all(RunWith.class);
		
		final boolean accepted = filter.accept(currentThread().getContextClassLoader(), "io/fries/reflection/ReflectionTest.class");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void deny() {
		final Filter filter = AnnotationFilter.all(RunWith.class);
		
		final boolean accepted = filter.accept(currentThread().getContextClassLoader(), "io/fries/reflection/filters/AnnotationFilterTest.class");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void classDoesNotExists() {
		final Filter filter = AnnotationFilter.all(RunWith.class);
		
		final boolean accepted = filter.accept(currentThread().getContextClassLoader(), "some/random/resource/Class.class");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void notAClass() {
		final Filter filter = AnnotationFilter.all(RunWith.class);
		
		final boolean accepted = filter.accept(currentThread().getContextClassLoader(), "some/random/resource.properties");
		
		assertThat(accepted).isFalse();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exception() {
		AnnotationFilter.any();
	}
}