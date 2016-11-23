package io.fries.reflection.filters;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.*;

public class AnnotationFilterTest {
	
	@Test
	public void accept() throws Exception {
		Filter filter = new AnnotationFilter(RunWith.class);
		
		boolean accepted = filter.accept(Thread.currentThread().getContextClassLoader(), "io/fries/reflection/ReflectionTest.class");
		
		assertThat(accepted).isTrue();
	}
	
	@Test
	public void deny() throws Exception {
		Filter filter = new AnnotationFilter(RunWith.class);
		
		boolean accepted = filter.accept(Thread.currentThread().getContextClassLoader(), "io/fries/reflection/filters/AnnotationFilterTest.class");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void classDoesNotExists() throws Exception {
		Filter filter = new AnnotationFilter(RunWith.class);
		
		boolean accepted = filter.accept(Thread.currentThread().getContextClassLoader(), "some/random/resource/Class.class");
		
		assertThat(accepted).isFalse();
	}
	
	@Test
	public void notAClass() throws Exception {
		Filter filter = new AnnotationFilter(RunWith.class);
		
		boolean accepted = filter.accept(Thread.currentThread().getContextClassLoader(), "some/random/resource.properties");
		
		assertThat(accepted).isFalse();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void exception() throws Exception {
		new AnnotationFilter();
	}
}