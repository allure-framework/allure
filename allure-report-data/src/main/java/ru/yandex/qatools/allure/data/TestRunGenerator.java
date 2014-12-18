package ru.yandex.qatools.allure.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;

import static ru.yandex.qatools.allure.commons.AllureFileUtils.listTestSuiteFiles;
import static ru.yandex.qatools.allure.config.AllureModelUtils.getAllureSchemaValidator;
import static ru.yandex.qatools.allure.data.utils.AllureReportUtils.deleteFile;
import static ru.yandex.qatools.allure.data.utils.XslTransformationUtils.applyTransformations;


/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 02.12.13
 */

public class TestRunGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SUITES_TO_TEST_RUN_XSL_1 = "xsl/suites-to-testrun-1.xsl";

    public static final String SUITES_TO_TEST_RUN_XSL_2 = "xsl/suites-to-testrun-2.xsl";

    public static final String SUITES_TO_TEST_RUN_XSL_3 = "xsl/suites-to-testrun-3.xsl";

    private final ListFiles listFiles;

    private final boolean validateXML;

    public TestRunGenerator(boolean validateXML, File... dirs) {
        Collection<File> testSuitesFiles = listTestSuiteFiles(dirs);

        this.listFiles = createListFiles(testSuitesFiles);
        this.validateXML = validateXML;

    }

    private ListFiles createListFiles(Collection<File> files) {
        ListFiles lf = new ListFiles();
        for (File file : files) {
            try {
                if (validateXML) {
                    Validator validator = getAllureSchemaValidator();
                    validator.validate(new StreamSource(file));
                }
                lf.getFiles().add(file.toURI().toString());
            } catch (Exception e) {
                logger.error("File " + file + " skipped.", e);
            }
        }
        return lf;
    }

    public File generate() {
        File xml = null;

        try {
            xml = createListFiles();

            return applyTransformations(
                    xml,
                    SUITES_TO_TEST_RUN_XSL_1,
                    SUITES_TO_TEST_RUN_XSL_2,
                    SUITES_TO_TEST_RUN_XSL_3
            );
        } catch (Exception e) {
            throw new ReportGenerationException(e);
        } finally {
            deleteFile(xml);
        }
    }

    public File createListFiles() throws IOException {
        File xml = Files.createTempFile("list-files", ".xml").toFile();
        JAXBElement<ListFiles> actions = new ObjectFactory().createListFiles(listFiles);
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ListFiles.class.getPackage().getName());
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshaller.marshal(actions, xml);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
        return xml;
    }


    @SuppressWarnings("unused")
    public ListFiles getListFiles() {
        return listFiles;
    }
}