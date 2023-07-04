package pl.danlz.rns_e.path_checker.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Entry in the file system.
 */
public class Entry {

    private final static String SONG_EXTENSION = "mp3";
    private final static String PLAYLIST_EXTENSION = "m3u";

    /**
     * Mix album has the {@code [MIX]} suffix.
     */
    private final static Pattern MIX_ALBUM_NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z _\\-.,&'()!\\[\\]+]+\\[MIX\\]$");

    /**
     * {@code Artist - Title}.
     */
    private final static Pattern ALBUM_NAME_PATTERN = Pattern.compile("(?U)^[0-9a-zA-Z\\p{L}\\p{M} .]+ - [0-9a-zA-Z\\p{L}\\p{M} .,&'()\\-\\[\\]]+$");

    /**
     * {@code XX Song name.mp3}, where XX is a two-digit number.
     */
    private final static Pattern SONG_NAME_PATTERN = Pattern.compile("^[0-9]{2} [0-9a-zA-Z\\p{L}\\p{M} _\\-.,&'()!\\[\\]+#]+$");


    private final File path;

    private final List<String> violations = new ArrayList<>();

    public Entry(File path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return path.isDirectory();
    }

    public boolean isFile() {
        return path.isFile();
    }

    public boolean isSongFile() {
        return path.getName().toLowerCase().endsWith("." + SONG_EXTENSION);
    }

    public boolean isPlaylistFile() {
        return path.getName().toLowerCase().endsWith("." + PLAYLIST_EXTENSION);
    }

    public boolean isAlbumName() {
        return ALBUM_NAME_PATTERN.matcher(path.getName()).matches();
    }

    public boolean isMixAlbumName() {
        return MIX_ALBUM_NAME_PATTERN.matcher(path.getName()).matches();
    }

    public boolean isSongName() {
        return SONG_NAME_PATTERN.matcher(path.getName()).matches();
    }

    public boolean isInAlbum() {
        Entry parentEntry = new Entry(path.getParentFile());
        return parentEntry.isAlbumName();
    }

    public int getNameLength() {
        return path.getName().length();
    }

    public String getName() {
        return path.getName();
    }

    public File getPath() {
        return path;
    }

    public List<String> getPlaylistItems() {
        if (!isPlaylistFile()) {
            throw new IllegalStateException("Entry '" + this + "' is not a playlist file");
        }
        try {
            return Files.readAllLines(path.toPath(), StandardCharsets.UTF_8).stream()
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Could not load playlist: " + path);
            return Collections.emptyList();
        }
    }


    public void addViolation(String violation) {
        violations.add(violation);
    }

    public List<String> getViolations() {
        return Collections.unmodifiableList(violations);
    }

    @Override
    public String toString() {
        return path.getAbsolutePath();
    }

}
