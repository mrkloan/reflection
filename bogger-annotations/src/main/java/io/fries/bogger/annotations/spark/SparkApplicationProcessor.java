package io.fries.bogger.annotations.spark;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("io.fries.bogger.annotations.spark.SparkApplication")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SparkApplicationProcessor extends AbstractProcessor {

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		System.out.println("Init SparkApplication processor");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		System.out.println("Start processing of SparkApplication");
		for(TypeElement te : annotations)  {
			for(Element annotated : roundEnv.getElementsAnnotatedWith(te)) {
				if(annotated.getKind() == ElementKind.CLASS) {
					System.out.println("Annotated: " + annotated.getSimpleName().toString());
				}
			}
		}
		System.out.println("End of SparkApplication processing");
		return true;
	}
}
