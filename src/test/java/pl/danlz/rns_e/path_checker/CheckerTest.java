package pl.danlz.rns_e.path_checker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class CheckerTest {
    private final URL baseUrl = this.getClass().getClassLoader().getResource("some-test.txt");
    private final File basePath = new File(baseUrl.getPath()).getParentFile();

    @Test
    void buildEntryList() {
        Checker checker = new Checker(basePath);
        checker.buildEntryList();

        Assertions.assertEquals(14, checker.entries.size());
    }
}
