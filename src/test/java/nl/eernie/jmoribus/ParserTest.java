package nl.eernie.jmoribus;

import nl.eernie.jmoribus.exception.UnableToParseStoryException;
import nl.eernie.jmoribus.model.Scenario;
import nl.eernie.jmoribus.model.Step;
import nl.eernie.jmoribus.model.StepType;
import nl.eernie.jmoribus.model.Story;
import nl.eernie.jmoribus.parser.ParseableStory;
import nl.eernie.jmoribus.parser.StoryParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class ParserTest {

    @Test
    public void testParse() throws IOException {
        InputStream fileInputStream = getClass().getResourceAsStream("/test.story");
        ParseableStory parseableStory = new ParseableStory(fileInputStream, "test.story");

        Story story = StoryParser.parseStory(parseableStory);

        Assert.assertEquals("test.story", story.getUniqueIdentifier());
        Assert.assertEquals("Some awsome title", story.getTitle());
        Assert.assertEquals("In order to realize a named business value" + System.lineSeparator() + "  As a explicit system actor" + System.lineSeparator() + "  I want to gain some beneficial outcome which furthers the goal", story.getFeature().getContent());
        Assert.assertSame(story, story.getFeature().getStory());

        Assert.assertEquals(1, story.getScenarios().size());

        Scenario scenario = story.getScenarios().get(0);
        Assert.assertEquals("scenario description", scenario.getTitle());
        Assert.assertSame(story, scenario.getStory());

        Assert.assertEquals(4, scenario.getSteps().size());

        Step step = scenario.getSteps().remove(0);
        assertStep(StepType.GIVEN, "a system state", scenario, step);
        step = scenario.getSteps().remove(0);
        assertStep(StepType.WHEN, "I do something", scenario, step);
        step = scenario.getSteps().remove(0);
        assertStep(StepType.THEN, "system is in a different state", scenario, step);
        step = scenario.getSteps().remove(0);
        assertStep(StepType.THEN, "another assertion", scenario, step);
    }

    private void assertStep(StepType type, String stepString, Scenario scenario, Step step) {
        Assert.assertSame(scenario, step.getStepContainer());
        Assert.assertEquals(type, step.getStepType());
        Assert.assertEquals(stepString, step.getFirstStepLine().getText());
    }

    @Test
    public void testMultipleScenarioParse() throws IOException {
        InputStream fileInputStream = getClass().getResourceAsStream("/multiScenario.story");
        ParseableStory parseableStory = new ParseableStory(fileInputStream, "test.story");

        Story story = StoryParser.parseStory(parseableStory);

        Assert.assertEquals(6, story.getScenarios().size());

    }

    @Test
    public void testMultipleStories() throws IOException {
        List<ParseableStory> parseableStories = new ArrayList<>(3);
        InputStream fileInputStream = getClass().getResourceAsStream("/multiScenario.story");
        parseableStories.add(new ParseableStory(fileInputStream, "MultiScenarioTitle"));
        fileInputStream = getClass().getResourceAsStream("/test2.story");
        parseableStories.add(new ParseableStory(fileInputStream, "testTitle"));
        fileInputStream = getClass().getResourceAsStream("/referring/referring.story");
        parseableStories.add(new ParseableStory(fileInputStream, "PrologueTest"));

        List<Story> stories = StoryParser.parseStories(parseableStories);
        Assert.assertEquals(3, stories.size());
        Assert.assertEquals("MultiScenarioTitle", stories.get(0).getTitle());

    }

    @Test(expected = UnableToParseStoryException.class)
    public void testUnparsableStory() throws IOException {
        FileInputStream fileInputStream = mock(FileInputStream.class);
        when(fileInputStream.read()).thenThrow(IOException.class);
        ParseableStory parseableStory = new ParseableStory(fileInputStream, "typo.story");
        StoryParser.parseStory(parseableStory);

    }
}
