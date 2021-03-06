package nl.eernie.jmoribus.matcher;

import nl.eernie.jmoribus.annotation.AfterScenario;
import nl.eernie.jmoribus.annotation.AfterStory;
import nl.eernie.jmoribus.annotation.BeforeScenario;
import nl.eernie.jmoribus.annotation.BeforeStory;
import nl.eernie.jmoribus.annotation.Category;
import nl.eernie.jmoribus.annotation.Given;
import nl.eernie.jmoribus.annotation.OutputVariables;
import nl.eernie.jmoribus.annotation.RequiredVariables;
import nl.eernie.jmoribus.annotation.Then;
import nl.eernie.jmoribus.annotation.When;
import nl.eernie.jmoribus.exception.NoParameterConverterFoundException;
import nl.eernie.jmoribus.model.Step;
import nl.eernie.jmoribus.model.StepType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodMatcher {

    private final List<Object> objects;

    private List<PossibleStep> possibleSteps = new ArrayList<>();

    private List<ParameterConverter> parameterConverters = new ArrayList<>();

    private Map<BeforeAfterType, List<BeforeAfterMethod>> beforeAfterMethods = new HashMap<>();

    private StepParser parser = new StepParser();

    public MethodMatcher(List<Object> objects) {
        this.objects = objects;
        findUsableMethods();
        setRegexMatchers();

    }

    private void findUsableMethods() {
        for (Object object : objects) {
            for (Method method : object.getClass().getMethods()) {
                createPossibleStep(method, object);
                createParameterConverter(method, object);
                createBeforeAfterMethods(method, object);
            }
        }
    }

    private void createBeforeAfterMethods(Method method, Object object) {
        if (method.isAnnotationPresent(BeforeStory.class)) {
            addBeforeAfterMethod(new BeforeAfterMethod(method, object, BeforeAfterType.BEFORE_STORY));
        }
        if (method.isAnnotationPresent(BeforeScenario.class)) {
            addBeforeAfterMethod(new BeforeAfterMethod(method, object, BeforeAfterType.BEFORE_SCENARIO));
        }
        if (method.isAnnotationPresent(AfterScenario.class)) {
            addBeforeAfterMethod(new BeforeAfterMethod(method, object, BeforeAfterType.AFTER_SCENARIO));
        }
        if (method.isAnnotationPresent(AfterStory.class)) {
            addBeforeAfterMethod(new BeforeAfterMethod(method, object, BeforeAfterType.AFTER_STORY));
        }
    }

    private void addBeforeAfterMethod(BeforeAfterMethod beforeAfterMethod) {
        BeforeAfterType type = beforeAfterMethod.getBeforeAfterType();
        if (!beforeAfterMethods.containsKey(type)) {
            beforeAfterMethods.put(type, new ArrayList<BeforeAfterMethod>());
        }
        beforeAfterMethods.get(type).add(beforeAfterMethod);
    }

    private void setRegexMatchers() {
        for (PossibleStep possibleStep : possibleSteps) {
            RegexStepMatcher regexStepMatcher = parser.parseStep(possibleStep.getStep());
            possibleStep.setRegexStepMatcher(regexStepMatcher);
        }
    }

    private void createParameterConverter(Method method, Object object) {
        if (method.isAnnotationPresent(nl.eernie.jmoribus.annotation.ParameterConverter.class)) {
            Class<?> returnType = method.getReturnType();
            parameterConverters.add(new ParameterConverter(method, object, returnType));

        }
    }

    private void createPossibleStep(Method method, Object object) {
        String[] categories = null;
        if (method.isAnnotationPresent(Category.class)) {
            Category category = method.getAnnotation(Category.class);
            categories = category.value();
        }

        String[] requiredVariables = null;
        if (method.isAnnotationPresent(RequiredVariables.class)) {
            RequiredVariables requiredVariablesAnnotation = method.getAnnotation(RequiredVariables.class);
            requiredVariables = requiredVariablesAnnotation.value();
        }

        String[] outputVariables = null;
        if (method.isAnnotationPresent(OutputVariables.class)) {
            OutputVariables outputVariablesAnnotation = method.getAnnotation(OutputVariables.class);
            outputVariables = outputVariablesAnnotation.value();
        }

        if (method.isAnnotationPresent(Given.class)) {
            Given annotation = method.getAnnotation(Given.class);
            possibleSteps.addAll(createPossibleSteps(annotation.value(), method, StepType.GIVEN, object, categories, requiredVariables, outputVariables));
        }
        if (method.isAnnotationPresent(When.class)) {
            When annotation = method.getAnnotation(When.class);
            possibleSteps.addAll(createPossibleSteps(annotation.value(), method, StepType.WHEN, object, categories, requiredVariables, outputVariables));
        }
        if (method.isAnnotationPresent(Then.class)) {
            Then annotation = method.getAnnotation(Then.class);
            possibleSteps.addAll(createPossibleSteps(annotation.value(), method, StepType.THEN, object, categories, requiredVariables, outputVariables));
        }
    }

    private List<PossibleStep> createPossibleSteps(String[] values, Method method, StepType stepType, Object object, String[] categories, String[] requiredVariables, String[] outputVariables) {
        ArrayList<PossibleStep> possibleSteps = new ArrayList<>();
        for (String value : values) {
            possibleSteps.add(new PossibleStep(value, method, stepType, object, categories, requiredVariables, outputVariables));
        }
        return possibleSteps;
    }

    public PossibleStep findMatchedStep(Step step) {
        for (PossibleStep possibleStep : possibleSteps) {
            if (possibleStep.getStepType().equals(step.getStepType())) {
                if (possibleStep.getRegexStepMatcher().matches(step.getCombinedStepLines())) {
                    return possibleStep;
                }
            }
        }
        return null;
    }

    public ParameterConverter findConverterFor(Class<?> parameterType) {
        for (ParameterConverter parameterConverter : parameterConverters) {
            if (parameterConverter.getReturnType().equals(parameterType)) {
                return parameterConverter;
            }
        }
        throw new NoParameterConverterFoundException();
    }

    public List<BeforeAfterMethod> findBeforeAfters(BeforeAfterType beforeAfterType) {
        return beforeAfterMethods.get(beforeAfterType);
    }

    public List<PossibleStep> getPossibleSteps() {
        return this.possibleSteps;
    }
}
