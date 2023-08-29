package pl.danlz.rns_e.path_checker.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.util.List;

public class EntryTest {

    private final URL baseUrl = this.getClass().getClassLoader().getResource("some-test.txt");
    private final File basePath = new File(baseUrl.getPath()).getParentFile();


    @Test
    void isDirectory() {
        Entry entry = createEntry("Album/Folder");

        Assertions.assertTrue(entry.isDirectory());
    }

    @Test
    void isFile() {
        Entry entry = createEntry("some-test.txt");

        Assertions.assertTrue(entry.isFile());
    }

    @Test
    void isSongFile() {
        Entry entry = createEntry("Artist - Title/invalid song name.mp3");

        Assertions.assertTrue(entry.isSongFile());
    }

    @Test
    void isPlaylist() {
        Entry entry = createEntry("playlist.m3u");

        Assertions.assertTrue(entry.isPlaylistFile());
    }

    @Test
    void isAlbumName() {
        Entry entry = createEntry("Artist - Title");

        Assertions.assertTrue(entry.isAlbumName());
    }

    @Test
    void isMixAlbumName() {
        Entry entry = createEntry("directory[MIX]");

        Assertions.assertTrue(entry.isMixAlbumName());
    }

    @Test
    void isSongName() {
        Entry entry = createEntry("Artist - Title/01 Song name.mp3");

        Assertions.assertTrue(entry.isSongFile());
    }

    @Test
    void isInAlbum() {
        Entry entry = createEntry("Artist - Title/other.txt");

        Assertions.assertTrue(entry.isInAlbum());
    }

    @Test
    void getNameLength() {
        Entry entry = createEntry("Album/other file.txt");

        Assertions.assertEquals(14, entry.getNameLength());
    }

    @Test
    void getName() {
        Entry entry = createEntry("Album/other file.txt");

        Assertions.assertEquals("other file.txt", entry.getName());
    }

    @Test
    void getPath() {
        File file = new File(basePath, "Album/other file.txt");
        Entry entry = new Entry(file);

        Assertions.assertEquals(file, entry.getPath());
    }

    @Test
    void getPlaylistItems() {
        Entry entry = createEntry("playlist.m3u");
        List<String> items = entry.getPlaylistItems();

        Assertions.assertEquals(3, items.size());
        Assertions.assertArrayEquals(
                new Object[] { "some-song.mp3", "Artist - Title/01 Song name.mp3", "/song.mp3"},
                items.toArray());
    }

    @Test
    void getPlaylistItemsForNotPlaylistFile() {
        Entry entry = createEntry("some-test.txt");

        Assertions.assertThrows(IllegalStateException.class, entry::getPlaylistItems);
    }

    private Entry createEntry(String resourcePath) {
        File file = new File(basePath, resourcePath);
        return new Entry(file);
    }
}
