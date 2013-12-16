package ru.yandex.qatools.allure;

import org.junit.Test;
import ru.yandex.qatools.allure.events.*;
import ru.yandex.qatools.allure.model.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 14.12.13
 */
public class AllureLifecycleTest {

    @Test
    public void allureLifecycleTest() throws Exception {
        TestSuiteResult testSuite = fireTestSuiteStart();
        TestSuiteResult anotherTestSuite = fireCustomTestSuiteEvent();
        assertEquals(testSuite, anotherTestSuite);

        TestCaseResult testCase = fireTestCaseStart();
        TestCaseResult anotherTestCase = fireCustomTestCaseEvent();
        assertEquals(testCase, anotherTestCase);

        assertThat(testSuite.getTestCases(), hasSize(1));
        assertEquals(testSuite.getTestCases().get(0), testCase);

        Step parentStep = fireStepStart();
        Attachment firstAttach = fireMakeAttach();

        assertThat(parentStep.getAttachments(), hasSize(1));
        assertEquals(parentStep.getAttachments().get(0), firstAttach);

        Step nestedStep = fireStepStart();
        Attachment secondAttach = fireMakeAttach();

        assertNotEquals(firstAttach, secondAttach);

        assertThat(nestedStep.getAttachments(), hasSize(1));
        assertEquals(nestedStep.getAttachments().get(0), secondAttach);

        fireStepFinished();

        assertThat(parentStep.getSteps(), hasSize(1));
        assertEquals(parentStep.getSteps().get(0), nestedStep);

        fireStepFinished();

        Attachment testCaseAttachment = fireMakeAttach();

        fireTestCaseFinished();

        assertThat(testCase.getSteps(), hasSize(1));
        assertEquals(testCase.getSteps().get(0), parentStep);

        assertThat(testCase.getAttachments(), hasSize(1));
        assertEquals(testCase.getAttachments().get(0), testCaseAttachment);

        fireTestSuiteFinished();

        assertThat(testSuite.getTestCases(), hasSize(1));

        TestSuiteResult nextTestSuite = fireTestSuiteStart();
        assertNotEquals(anotherTestSuite, nextTestSuite);
    }

    public TestSuiteResult fireTestSuiteStart() {
        Allure.LIFECYCLE.fire(new TestSuiteStartedEvent("some.uid", "some.suite.name"));
        TestSuiteResult testSuite = Allure.LIFECYCLE.testSuiteStorage.get("some.uid");
        assertNotNull(testSuite);
        assertThat(testSuite.getName(), is("some.suite.name"));
        assertThat(testSuite.getTestCases(), hasSize(0));
        return testSuite;
    }

    public void fireTestSuiteFinished() {
        Allure.LIFECYCLE.fire(new TestSuiteFinishedEvent("some.uid"));
    }

    public TestCaseResult fireTestCaseStart() {
        Allure.LIFECYCLE.fire(new TestCaseStartedEvent("some.uid", "some.case.name"));
        TestCaseResult testCase = Allure.LIFECYCLE.testCaseStorage.get();
        assertNotNull(testCase);
        assertThat(testCase.getName(), is("some.case.name"));
        return testCase;
    }

    public void fireTestCaseFinished() {
        Allure.LIFECYCLE.fire(new TestCaseFinishedEvent());
    }

    public Step fireStepStart() {
        Allure.LIFECYCLE.fire(new StepStartedEvent("some.step.name"));
        Step step = Allure.LIFECYCLE.stepStorage.getLast();
        assertNotNull(step);
        assertThat(step.getName(), is("some.step.name"));
        return step;
    }

    public Attachment fireMakeAttach() {
        Step lastStep = Allure.LIFECYCLE.stepStorage.getLast();
        int attachmentsCount = lastStep.getAttachments().size();

        Allure.LIFECYCLE.fire(new MakeAttachEvent("some.attach.title", AttachmentType.TXT, "attach.body"));

        assertThat(lastStep.getAttachments().size(), is(attachmentsCount + 1));
        Attachment attachment = lastStep.getAttachments().get(attachmentsCount);
        assertNotNull(attachment);

        return attachment;
    }

    public void fireStepFinished() {
        Allure.LIFECYCLE.fire(new StepFinishedEvent());
    }

    public TestSuiteResult fireCustomTestSuiteEvent() {
        Allure.LIFECYCLE.fire(new ChangeTestSuiteTitleEvent("some.uid", "new.suite.title"));
        TestSuiteResult testSuite = Allure.LIFECYCLE.testSuiteStorage.get("some.uid");
        assertNotNull(testSuite);
        assertThat(testSuite.getTitle(), is("new.suite.title"));
        return testSuite;
    }

    public TestCaseResult fireCustomTestCaseEvent() {
        Allure.LIFECYCLE.fire(new ChangeTestCaseTitleEvent("new.case.title"));
        TestCaseResult testCase = Allure.LIFECYCLE.testCaseStorage.get();
        assertNotNull(testCase);
        assertThat(testCase.getTitle(), is("new.case.title"));
        return testCase;
    }
}
