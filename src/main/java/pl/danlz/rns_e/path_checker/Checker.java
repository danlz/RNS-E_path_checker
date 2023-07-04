package pl.danlz.rns_e.path_checker;

import pl.danlz.rns_e.path_checker.model.Entry;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class Checker {

    private final File rootPath;

    final List<Entry> entries = new ArrayList<>();

    public Checker(File rootPath) {
        this.rootPath = rootPath;
    }


    /**
     * Builds entry list by scanning the specified root directory.
     */
    public void buildEntryList() {
        System.out.println("Scanning directory: " + rootPath.getPath());
        List<File> toVisit = new ArrayList<>();
        toVisit.add(rootPath);
        visitDirectory(toVisit);
        Collections.sort(entries);
    }

    private void visitDirectory(List<File> toVisit) {
        if (toVisit.isEmpty()) {
            return;
        }
        File directory = toVisit.remove(0);
        File[] contents = directory.listFiles();
        if (contents == null) {
            System.err.println("Could not list directory: " + directory);
        } else {
            Arrays.stream(contents).forEach(entry -> {
                if (entry.isDirectory()) {
                    toVisit.add(entry);
                }
                entries.add(new Entry(entry));
            });
        }
        visitDirectory(toVisit);
    }

    public void checkNameLength() {
        executeCheck(Checks::nameLength);
    }

    public void checkCharacters() {
        executeCheck(Checks::characters);
    }

    public void checkAlbumName() {
        executeCheck(Checks::albumName);
    }

    public void checkNamesInAlbum() {
        executeCheck(Checks::nameInAlbum);
    }

    /**
     * Checks if playlists contain references to valid songs.
     */
    public void checkPlaylists() {
        File rootBasePath = rootPath.toPath().getRoot().toFile();
        for (Entry entry: entries) {
            if (entry.isFile() && entry.isPlaylistFile()) {
                File relativeBasePath = entry.getPath().getParentFile();
                List<String> playListItems = entry.getPlaylistItems();
                for (String playlistItem : playListItems) {
                    File playlistItemFile;
                    if (playlistItem.startsWith("/")) {
                        playlistItemFile = new File(rootBasePath, playlistItem);
                    } else {
                        playlistItemFile = new File(relativeBasePath, playlistItem);
                    }

                    // TODO if path in playlist contains a special character like ł it won't be displayed
                    // TODO playlist item should be mp3 file
                    if (!playlistItemFile.isFile() || !playlistItemFile.exists()) {
                        entry.addViolation("Invalid path in playlist: " + playlistItem);
                    }
                }
            }
        }
    }

    private void executeCheck(Check check) {
        for (Entry entry: entries) {
            String result = check.execute(entry);
            if (result != null) {
                entry.addViolation(result);
            }
        }
    }

    /**
     * Prints violations for each entry.
     */
    public void printViolations() {
        System.out.println("Violations found:");
        for (Entry entry: entries) {
            if (!entry.getViolations().isEmpty()) {
                System.out.println(entry);
                for (String violation: entry.getViolations()) {
                    System.out.println("  " + violation);
                }
            }
        }
    }

    void printEntries() {
        System.out.println("--- ENTRIES ---");
        for (Entry entry: entries) {
            System.out.println(entry);
        }
    }

    /**
     * A check for single entry.
     */
    @FunctionalInterface
    public interface Check {

        /**
         * Executes check on the given entry.
         *
         * @param entry entry
         * @return {@code null} if there is no violation; otherwise a string describing the violation
         */
        String execute(Entry entry);
    }

    static class Checks {

        private final static int FILENAME_LENGTH_LIMIT = 64;
        private final static Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("^[0-9a-zA-Z _\\-.,&'()!\\[\\]+#]+$");


        static String nameLength(Entry entry) {
            if (entry.getNameLength() > FILENAME_LENGTH_LIMIT) {
                return "Entry name has " + entry.getNameLength() + " characters";
            }
            return null;
        }

        static String characters(Entry entry) {
            if (!ALLOWED_CHARACTERS_PATTERN.matcher(entry.getName()).matches()) {
                // TODO should we check path elements?
                return "Invalid characters in name";
            }
            return null;
        }

        /**
         * Album name can have two formats:
         * <ol>
         *     <li>{@code Artists - Title}</li>
         *     <li>{@code some name[MIX]}</li>
         * </ol>
         *
         */
        static String albumName(Entry entry) {
            if (entry.isDirectory() && !entry.isAlbumName() && !entry.isMixAlbumName()) {
                return "Invalid album name";
            }
            return null;
        }

        /**
         * An album may contain only songs or playlists.
         */
        static String nameInAlbum(Entry entry) {
            if (entry.isFile()) {
                if (entry.isSongFile()) {
                    if (entry.isInAlbum() && !entry.isSongName()) {
                        return "Invalid song name in album";
                    }
                } else if (!entry.isPlaylistFile()) {
                    return "Invalid file in album";
                }
            }
            return null;
        }

    }

}
