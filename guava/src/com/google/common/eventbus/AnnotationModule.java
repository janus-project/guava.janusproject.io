package com.google.common.eventbus;

import java.lang.annotation.Annotation;

import com.google.inject.AbstractModule;

public class AnnotationModule extends AbstractModule {
	
	private final Class<? extends Annotation> annotationClass;
	
	public AnnotationModule() {
		this.annotationClass = Subscribe.class;
	}
	
	
	public AnnotationModule(Class<? extends Annotation> annotation) {
		this.annotationClass = annotation;
	}
	
	@Override
	protected void configure() {		
		this.bind(SubscriberFindingStrategy.class).toInstance(new AnnotatedSubscriberFinder(this.annotationClass));			
	}

}
