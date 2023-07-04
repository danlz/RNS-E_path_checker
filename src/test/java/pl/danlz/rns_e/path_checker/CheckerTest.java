package pl.danlz.rns_e.path_checker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.danlz.rns_e.path_checker.model.Entry;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

public class CheckerTest {
    private final URL baseUrl = this.getClass().getClassLoader().getResource("some-test.txt");
    private final File basePath = new File(baseUrl.getPath()).getParentFile();

    @Test
    void buildEntryList() {
        Checker checker = new Checker(basePath);
        checker.buildEntryList();

        Assertions.assertEquals(17, checker.entries.size());
        Entry entry1 = checker.entries.get(0);
        Assertions.assertEquals("123456789012345678901234567890123456789012345678901234567890Too_long", entry1.getName());
        Entry entry2 = checker.entries.get(1);
        Assertions.assertEquals("album", entry2.getName());
        Entry entry3 = checker.entries.get(2);
        Assertions.assertEquals("folder", entry3.getName());
    }

    @Test
    void checkNameLength_Valid() {
        Entry entry = createEntry("album/other file.txt");
        String result = Checker.Checks.nameLength(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkNameLength_Invalid() {
        Entry entry = createEntry("123456789012345678901234567890123456789012345678901234567890Too_long");
        String result = Checker.Checks.nameLength(entry);

        Assertions.assertEquals("Entry name has 68 characters", result);
    }

    @Test
    void checkCharacters_Valid() {
        Entry entry = createEntry("album/other file.txt");
        String result = Checker.Checks.characters(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkCharacters_Invalid() {
        Entry entry = createEntry("album/invalid characters ół.txt");
        String result = Checker.Checks.characters(entry);

        Assertions.assertEquals("Invalid characters in name", result);
    }

    @Test
    void checkAlbumName_Valid() {
        Entry entry = createEntry("Artist - Title");
        String result = Checker.Checks.albumName(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkAlbumName_ValidMix() {
        Entry entry = createEntry("directory[MIX]");
        String result = Checker.Checks.albumName(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkAlbumName_Invalid() {
        Entry entry = createEntry("album");
        String result = Checker.Checks.albumName(entry);

        Assertions.assertEquals("Invalid album name", result);
    }

    @Test
    void checkNameInAlbum_ValidSong() {
        Entry entry = createEntry("Artist - Title/01 Song name.mp3");
        String result = Checker.Checks.nameInAlbum(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkNameInAlbum_ValidPlaylist() {
        Entry entry = createEntry("Artist - Title/playlist.m3u");
        String result = Checker.Checks.nameInAlbum(entry);

        Assertions.assertNull(result);
    }

    @Test
    void checkNameInAlbum_InvalidSongName() {
        Entry entry = createEntry("Artist - Title/invalid song name.mp3");
        String result = Checker.Checks.nameInAlbum(entry);

        Assertions.assertEquals("Invalid song name in album", result);
    }

    @Test
    void checkNameInAlbum_InvalidFile() {
        Entry entry = createEntry("Artist - Title/other.txt");
        String result = Checker.Checks.nameInAlbum(entry);

        Assertions.assertEquals("Invalid file in album", result);
    }

    @Test
    void checkPlaylists() {
        Checker checker = new Checker(basePath);
        checker.buildEntryList();
        checker.checkPlaylists();

        List<Entry> playlistEntries = checker.entries.stream()
                .filter(entry -> entry.getPath().equals(new File(basePath, "playlist.m3u")))
                .collect(Collectors.toList());

        Assertions.assertEquals(1, playlistEntries.size());
        Entry playlistEntry = playlistEntries.get(0);
        List<String> violations = playlistEntry.getViolations();
        Assertions.assertEquals(2, violations.size());
        Assertions.assertEquals("Invalid path in playlist: some-song.mp3", violations.get(0));
        Assertions.assertEquals("Invalid path in playlist: /song.mp3", violations.get(1));
    }


    private Entry createEntry(String resourcePath) {
        File file = new File(basePath, resourcePath);
        return new Entry(file);
    }
}
