package ru.yandex.qatools.allure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.properties.PropertyLoader;
import ru.yandex.qatools.properties.annotations.Property;
import ru.yandex.qatools.properties.annotations.Resource;
import ru.yandex.qatools.properties.annotations.Use;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Artem Eroshenko eroshenkoam@yandex-team.ru
 *         Date: 12/13/13
 */
@SuppressWarnings("unused")
@Resource.Classpath("allure.properties")
public class AllureConfig {

    public static final Logger LOGGER = LoggerFactory.getLogger(AllureConfig.class);

    private static final File DEFAULT_RESULTS_DIRECTORY = new File("target/allure-results");

    @Property("allure.model.schema.file.name")
    private String schemaFileName = "allure.xsd";

    @Property("allure.report.remove.attachments")
    private String removeAttachments = "a^";

    @Property("allure.results.testsuite.file.regex")
    private String testSuiteFileRegex = ".*-testsuite\\.xml";

    @Property("allure.results.testsuite.file.suffix")
    private String testSuiteFileSuffix = "testsuite";

    @Property("allure.results.testsuite.file.extension")
    private String testSuiteFileExtension = "xml";

    @Property("allure.results.attachment.file.regex")
    private String attachmentFileRegex = ".+-attachment(\\..+)?";

    @Property("allure.results.attachment.file.suffix")
    private String attachmentFileSuffix = "-attachment";

    @Property("allure.results.environment.xml.file.name")
    private String environmentXmlFileRegex = ".*environment\\.xml";

    @Property("allure.results.environment.properties.file.name")
    private String environmentPropertiesFileRegex = ".*environment\\.properties";

    @Property("allure.results.directory")
    private File resultsDirectory = DEFAULT_RESULTS_DIRECTORY;

    @Property("allure.attachments.encoding")
    private String attachmentsEncoding = "UTF-8";

    @Use(AllureStatusFilterConverter.class)
    @Property("allure.results.testcases.status.filter")
    private Collection<Status> resultsTestCasesStatusFilter = new HashSet<>();

    private String version = getClass().getPackage().getImplementationVersion();

    public AllureConfig() {
        PropertyLoader.populate(this);
    }

    public String getSchemaFileName() {
        return schemaFileName;
    }

    public String getRemoveAttachments() {
        return removeAttachments;
    }

    public String getTestSuiteFileRegex() {
        return testSuiteFileRegex;
    }

    public String getTestSuiteFileSuffix() {
        return testSuiteFileSuffix;
    }

    public String getTestSuiteFileExtension() {
        return testSuiteFileExtension;
    }

    public String getAttachmentFileRegex() {
        return attachmentFileRegex;
    }

    public String getAttachmentFileSuffix() {
        return attachmentFileSuffix;
    }

    public String getEnvironmentXmlFileRegex() {
        return environmentXmlFileRegex;
    }

    public String getEnvironmentPropertiesFileRegex() {
        return environmentPropertiesFileRegex;
    }

    public File getResultsDirectory() {
        return resultsDirectory;
    }

    public Charset getAttachmentsEncoding() {
        try {
            return Charset.forName(attachmentsEncoding);
        } catch (Exception e) {
            LOGGER.trace("Can't find attachments encoding \"" + attachmentsEncoding, "\" use default", e);
            return Charset.defaultCharset();
        }
    }

    public static File getDefaultResultsDirectory() {
        return DEFAULT_RESULTS_DIRECTORY;
    }

    public String getVersion() {
        return version;
    }

    public Collection<Status> getResultsTestCasesStatusFilter() {
        if (resultsTestCasesStatusFilter.contains(Status.FAILED) ||
                resultsTestCasesStatusFilter.contains(Status.BROKEN)) {
            LOGGER.trace("Property \"allure.results.testcases.status.filter\" should not " +
                    "contain \"failed\" or \"broken\" status");
            resultsTestCasesStatusFilter.removeAll(Arrays.asList(Status.FAILED, Status.BROKEN));
        }
        return resultsTestCasesStatusFilter;
    }

    public static AllureConfig newInstance() {
        return new AllureConfig();
    }

}
