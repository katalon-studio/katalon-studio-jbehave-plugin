package com.kms.katalon.core

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStories;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.steps.AbstractStepsFactory
import org.jbehave.core.steps.AbstractStepsFactory.StepsInstanceNotFound
import org.junit.After;
import org.junit.Before;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

public class ScanningStepsFactory extends AbstractStepsFactory {

	private final Map<Class<?>, Object> instances = new HashMap<>();

	private final Set<Class<?>> types = new HashSet<>();
	private String matchingRegex = ".*";
	private String notMatchingRegex = "";

	public ScanningStepsFactory(Configuration configuration, Class<?> root) {
		this(configuration, root.getPackage().getName());
	}

	public ScanningStepsFactory(Configuration configuration,
	String... packageNames) {
		super(configuration);
		for (String packageName : packageNames) {
			types.addAll(scanTypes(packageName));
		}
	}

	public ScanningStepsFactory matchingNames(String matchingRegex) {
		this.matchingRegex = matchingRegex;
		return this;
	}

	public ScanningStepsFactory notMatchingNames(String notMatchingRegex) {
		this.notMatchingRegex = notMatchingRegex;
		return this;
	}

	private Set<Class<?>> scanTypes(String packageName) {
		Reflections reflections = new Reflections(packageName,
				new MethodAnnotationsScanner());
		Set<Class<?>> types = new HashSet<>();
		types.addAll(typesAnnotatedWith(reflections, Given.class));
		types.addAll(typesAnnotatedWith(reflections, When.class));
		types.addAll(typesAnnotatedWith(reflections, Then.class));
		types.addAll(typesAnnotatedWith(reflections, Before.class));
		types.addAll(typesAnnotatedWith(reflections, After.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeScenario.class));
		types.addAll(typesAnnotatedWith(reflections, AfterScenario.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeStory.class));
		types.addAll(typesAnnotatedWith(reflections, AfterStory.class));
		types.addAll(typesAnnotatedWith(reflections, BeforeStories.class));
		types.addAll(typesAnnotatedWith(reflections, AfterStories.class));
		return types;
	}

	private Set<Class<?>> typesAnnotatedWith(Reflections reflections,
			Class<? extends Annotation> annotation) {
		Set<Class<?>> types = new HashSet<>();
		Set<Method> methodsAnnotatedWith = reflections
				.getMethodsAnnotatedWith(annotation);
		for (Method method : methodsAnnotatedWith) {
			types.add(method.getDeclaringClass());
		}
		return types;
	}

	@Override
	protected List<Class<?>> stepsTypes() {
		List<Class<?>> matchingTypes = new ArrayList<>();
		for (Class<?> type : types) {
			String name = type.getName();
			if (name.matches(matchingRegex) && !name.matches(notMatchingRegex)) {
				matchingTypes.add(type);
			}
		}
		return matchingTypes;
	}

	@Override
	public Object createInstanceOfType(Class<?> type) {
		Object instance;
		try {
			instance = instances.get(type);
			if (instance == null) {
				instance = type.newInstance();
				instances.put(type, instance);
			}
		} catch (Exception e) {
			throw new StepsInstanceNotFound(type, this)
		}
		return instance;
	}
}
