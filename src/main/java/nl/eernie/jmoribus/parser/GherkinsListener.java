package nl.eernie.jmoribus.parser;

import nl.eernie.jmoribus.GherkinsBaseListener;
import nl.eernie.jmoribus.GherkinsParser;
import nl.eernie.jmoribus.model.Feature;
import nl.eernie.jmoribus.model.Line;
import nl.eernie.jmoribus.model.Prologue;
import nl.eernie.jmoribus.model.Scenario;
import nl.eernie.jmoribus.model.Step;
import nl.eernie.jmoribus.model.StepContainer;
import nl.eernie.jmoribus.model.StepType;
import nl.eernie.jmoribus.model.Story;
import nl.eernie.jmoribus.model.Table;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GherkinsListener extends GherkinsBaseListener {

    private Map<String, Scenario> scenarios = new HashMap<>();
    private Set<ReferringScenario> referringScenarios = new HashSet<>();

    private Story story;
    private Feature feature;
    private StepContainer prologueOrScenario;

    private Step step;
    private StepType stepType;
    private Table table;
    private List<String> row = new ArrayList<>();

    @Override
    public void enterStory(@NotNull GherkinsParser.StoryContext ctx) {
        story = new Story();
    }

    @Override
    public void enterFeature(@NotNull GherkinsParser.FeatureContext ctx) {
        feature = new Feature();
        feature.setStory(story);
    }

    @Override
    public void exitFeature_title(@NotNull GherkinsParser.Feature_titleContext ctx) {
        story.setTitle(ctx.getText().trim());
    }

    @Override
    public void exitFeature_content(@NotNull GherkinsParser.Feature_contentContext ctx) {
        feature.setContent(ctx.getText().trim());
    }

    @Override
    public void exitFeature(@NotNull GherkinsParser.FeatureContext ctx) {
        story.setFeature(feature);
        feature = null;
    }

    @Override
    public void enterScenario(@NotNull GherkinsParser.ScenarioContext ctx) {
        prologueOrScenario = new Scenario();
    }

    @Override
    public void exitScenario_title(@NotNull GherkinsParser.Scenario_titleContext ctx) {
        ((Scenario) prologueOrScenario).setTitle(ctx.getText().trim());
    }

    @Override
    public void exitScenario(@NotNull GherkinsParser.ScenarioContext ctx) {
        Scenario scenario = (Scenario) prologueOrScenario;
        story.getScenarios().add(scenario);
        prologueOrScenario.setStory(story);
        scenarios.put(scenario.getTitle(), scenario);
        prologueOrScenario = null;
    }

    @Override
    public void enterPrologue(@NotNull GherkinsParser.PrologueContext ctx) {
        prologueOrScenario = new Prologue();
    }

    @Override
    public void exitPrologue(@NotNull GherkinsParser.PrologueContext ctx) {
        story.setPrologue((Prologue) prologueOrScenario);
        prologueOrScenario.setStory(story);
        prologueOrScenario = null;
    }

    @Override
    public void exitStep_keyword(@NotNull GherkinsParser.Step_keywordContext ctx) {

        switch (ctx.getText()) {
            case "Given":
                stepType = StepType.GIVEN;
                break;
            case "When":
                stepType = StepType.WHEN;
                break;
            case "Then":
                stepType = StepType.THEN;
                break;
            case "And":
                // Steptype stays the same as previous steptype
                break;
            case "Referring":
                stepType = StepType.REFERRING;
                break;
            default:
                throw new IllegalArgumentException("Unknown step type " + ctx.getText());
        }

        step = new Step(stepType);
    }

    @Override
    public void exitStep(@NotNull GherkinsParser.StepContext ctx) {
        if (stepType == StepType.REFERRING) {
            ReferringScenario scenario = new ReferringScenario(step.getCombinedStepLines(), prologueOrScenario);
            referringScenarios.add(scenario);
            prologueOrScenario.getSteps().add(scenario);
        } else {
            step.setStepContainer(prologueOrScenario);
            prologueOrScenario.getSteps().add(step);
        }
        step = null;
    }

    @Override
    public void enterTable(@NotNull GherkinsParser.TableContext ctx) {
        table = new Table();
    }

    @Override
    public void exitCell(@NotNull GherkinsParser.CellContext ctx) {
        String cell = ctx.getText();
        cell = cell.replace("|", "").trim();
        row.add(cell);
    }

    @Override
    public void exitTable_row(@NotNull GherkinsParser.Table_rowContext ctx) {
        if (table.getHeader() == null) {
            table.setHeader(row);
        } else {
            table.getRows().add(row);
        }
        row = new ArrayList<>();
    }

    @Override
    public void exitExamples_table(@NotNull GherkinsParser.Examples_tableContext ctx) {
        Scenario scn = (Scenario) prologueOrScenario;
        scn.setExamplesTable(table);
        table = null;
    }

    @Override
    public void exitStep_table_line(@NotNull GherkinsParser.Step_table_lineContext ctx) {
        step.getStepLines().add(table);
        table = null;
    }

    @Override
    public void exitStep_text_line(@NotNull GherkinsParser.Step_text_lineContext ctx) {
        step.getStepLines().add(new Line(ctx.getText().trim()));
    }

    public Story getStory() {
        return story;
    }

    public void setScenarios(Map<String, Scenario> scenarios) {
        this.scenarios = scenarios;
    }

    public void setReferringScenarios(Set<ReferringScenario> referringScenarios) {
        this.referringScenarios = referringScenarios;
    }
}
