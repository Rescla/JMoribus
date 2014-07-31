package nl.eernie.jmoribus.matcher;


import nl.eernie.jmoribus.matcher.RegexStepMatcher;
import nl.eernie.jmoribus.model.Feature;
import nl.eernie.jmoribus.model.StepType;

import java.lang.reflect.Method;

public class PossibleStep {
    private String step;
    private Method method;
    private Object methodObject;
    private StepType stepType;
    private RegexStepMatcher regexStepMatcher;
    private String[] categories;

    public PossibleStep(String step, Method method, StepType stepType, Object object, String[] categories) {
        this.step = step;
        this.method = method;
        this.stepType = stepType;
        this.methodObject = object;
        this.categories = categories;
    }

    public String getStep() {
        return step;
    }

    public Method getMethod() {
        return method;
    }

    public Object getMethodObject() {
        return methodObject;
    }

    public StepType getStepType() {
        return stepType;
    }

    public RegexStepMatcher getRegexStepMatcher() {
        return regexStepMatcher;
    }

    public void setRegexStepMatcher(RegexStepMatcher regexStepMatcher) {
        this.regexStepMatcher = regexStepMatcher;
    }

    public String[] getCategories() {
        return categories;
    }
}
