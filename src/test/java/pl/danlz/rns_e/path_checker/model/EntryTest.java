package pl.danlz.rns_e.path_checker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class EntryTest {

    private final URL baseUrl = this.getClass().getClassLoader().getResource("some-test.txt");
    private final File basePath = new File(baseUrl.getPath()).getParentFile();


    @Test
    void isPlaylist() {
        File playlistFile = new File(basePath, "playlist.m3u");
        Entry entry = new Entry(playlistFile);

        Assertions.assertTrue(entry.isPlaylistFile());
    }

    @Test
    void isFile() {
        File playlistFile = new File(basePath, "some-test.txt");
        Entry entry = new Entry(playlistFile);

        Assertions.assertTrue(entry.isFile());
    }
}
