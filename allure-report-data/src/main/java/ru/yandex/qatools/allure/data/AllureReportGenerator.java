package ru.yandex.qatools.allure.data;

import ru.yandex.qatools.allure.data.providers.DataProvider;
import ru.yandex.qatools.allure.data.utils.Async;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static ru.yandex.qatools.allure.data.utils.AllureReportUtils.*;
import static ru.yandex.qatools.allure.data.utils.ServiceLoaderUtils.load;


/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.10.13
 */
public class AllureReportGenerator {

    private static final String DATA_SUFFIX = "data";

    private ClassLoader classLoader;

    protected final File[] inputDirectories;

    protected TestRunGenerator testRunGenerator;

    private boolean validateXML = true;

    public AllureReportGenerator(final File... inputDirectories) {
        this.testRunGenerator = new TestRunGenerator(validateXML, inputDirectories);
        this.classLoader = getClass().getClassLoader();
        this.inputDirectories = inputDirectories;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @SuppressWarnings("unused")
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void generate(File reportDirectory) {
        long start = System.currentTimeMillis();

        final File reportDataDirectory = createDirectory(reportDirectory, DATA_SUFFIX);
        final AtomicLong reportSize = new AtomicLong(0);

        final File testRun = testRunGenerator.generate();

        List<DataProvider> dataProviders = load(getClassLoader(), DataProvider.class);
        Collections.sort(dataProviders, new DataProviderComparator());

        new Async<DataProvider>() {
            @Override
            public void async(DataProvider provider) {
                reportSize.addAndGet(provider.provide(
                        testRun,
                        inputDirectories,
                        reportDataDirectory
                ));
            }
        }.execute(dataProviders);

        deleteFile(testRun);

        long stop = System.currentTimeMillis();

        AllureReportInfo reportInfo = new AllureReportInfo();
        reportInfo.setSize(reportSize.get());
        reportInfo.setTime(stop - start);

        writeAllureReportInfo(reportInfo, reportDataDirectory);
    }

    @SuppressWarnings("unused")
    public void setValidateXML(boolean validateXML) {
        this.validateXML = validateXML;
    }

    @SuppressWarnings("unused")
    public File[] getInputDirectories() {
        return inputDirectories;
    }

    protected static class DataProviderComparator implements Comparator<DataProvider> {

        @Override
        public int compare(DataProvider dp1, DataProvider dp2) {
            return dp1.getPhase().compareTo(dp2.getPhase());
        }

    }

}
