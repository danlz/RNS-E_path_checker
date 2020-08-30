package pl.danlz.rns_e.path_checker;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class Main {

    private final static String SONG_EXTENSION = "mp3";
    private final static String PLAYLIST_EXTENSION = "m3u";
    private final static int FILENAME_LENGTH_LIMIT = 64;
    private final static Pattern ALLOWED_CHARACTERS_PATTERN = Pattern.compile("^[0-9a-zA-Z _\\-.,&'()!\\[\\]+#]+$");

    /**
     * Mix album has the {@code [MIX]} suffix.
     */
    private final static Pattern MIX_ALBUM_NAME_PATTERN = Pattern.compile("^[0-9a-zA-Z _\\-.,&'()!\\[\\]+]+\\[MIX\\]$");

    /**
     * {@code Artist - Title}.
     */
    private final static Pattern ALBUM_NAME_PATTERN = Pattern.compile("(?U)^[0-9a-zA-Z\\p{L}\\p{M} .]+ - [0-9a-zA-Z\\p{L}\\p{M} .,&'()\\-\\[\\]]+$");

    /**
     * {@code XX Song name.mp3}, where XX is a two digit number.
     */
    private final static Pattern SONG_NAME_PATTERN = Pattern.compile("^[0-9]{2} [0-9a-zA-Z\\p{L}\\p{M} _\\-.,&'()!\\[\\]+#]+$");

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Specify directory");
            System.exit(1);
        }

        File path = new File(args[0]);
        if (!path.isDirectory()) {
            System.out.println("The path must point to a directory");
            System.exit(1);
        }

        System.out.println("Checking name length...");
        executeCheck(path, Main::checkNameLength);
        System.out.println();

        System.out.println("Checking characters...");
        executeCheck(path, Main::checkCharacters);
        System.out.println();

        System.out.println("Checking album names...");
        executeCheck(path, Main::checkAlbumName);
        System.out.println();

        System.out.println("Checking song names in albums...");
        executeCheck(path, Main::checkNameInAlbum);
        System.out.println();

        System.out.println("Checking playlists...");
        checkPlaylists(path);
        System.out.println();
    }

    private static void executeCheck(File directory, EntryCheck check) {
        List<File> toVisit = new ArrayList<>();
        toVisit.add(directory);
        processDirectory(toVisit, check);
    }

    private static void processDirectory(List<File> toVisit, EntryCheck check) {
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
                check.execute(entry);
            });
        }
        processDirectory(toVisit, check);
    }

    private static void checkNameLength(File entry) {
        if (entry.getName().length() > FILENAME_LENGTH_LIMIT) {
            System.out.println(entry.getAbsolutePath() + "   " + entry.getName().length() + " characters");
        }
    }

    private static void checkCharacters(File entry) {
        if (!ALLOWED_CHARACTERS_PATTERN.matcher(entry.getName()).matches()) {
            System.out.println(entry.getAbsolutePath());
        }
    }

    private static void checkAlbumName(File entry) {
        if (entry.isDirectory()
                && !ALBUM_NAME_PATTERN.matcher(entry.getName()).matches()
                && !MIX_ALBUM_NAME_PATTERN.matcher(entry.getName()).matches()
        ) {
            System.out.println(entry.getAbsolutePath());
        }
    }

    private static void checkNameInAlbum(File entry) {
        if (entry.isFile()) {
            if (isSongFile(entry)) {
                if (isAlbumName(entry.getParentFile()) && !SONG_NAME_PATTERN.matcher(entry.getName()).matches()) {
                    System.out.println(entry.getAbsolutePath());
                }
            } else if (!isPlaylistFile(entry)) {
                System.out.println(entry.getAbsolutePath());
            }
        }
    }

    private static boolean isAlbumName(File entry) {
        return !MIX_ALBUM_NAME_PATTERN.matcher(entry.getName()).matches() && ALBUM_NAME_PATTERN.matcher(entry.getName()).matches();
    }

    private static boolean isSongFile(File entry) {
        return entry.getName().toLowerCase().endsWith("." + SONG_EXTENSION);
    }

    private static boolean isPlaylistFile(File entry) {
        return entry.getName().toLowerCase().endsWith("." + PLAYLIST_EXTENSION);
    }

    private static void checkPlaylists(File directory) {
        List<File> toVisit = new ArrayList<>();
        toVisit.add(directory);
        List<File> playlists = findPlaylists(toVisit);

        File rootPath = directory.toPath().getRoot().toFile();

        playlists.forEach(playlist -> {
            File basePath = playlist.getParentFile();
            try {
                System.out.println("Checking playlist: " + playlist);
                Files.readAllLines(playlist.toPath(), StandardCharsets.UTF_8).stream()
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(path -> {
                            File playlistItem;
                            if (path.startsWith("/")) {
                                playlistItem = new File(rootPath, path);
                            } else {
                                playlistItem = new File(basePath, path);
                            }
                            // TODO if path in playlist contains a special character like ł it won't be displayed
                            if (!playlistItem.isFile() || !playlistItem.exists()) {
                                System.out.println("Invalid path: " + playlistItem);
                            }
                        });
            } catch (IOException e) {
                System.err.println("Could not load playlist: " + playlist);
            }
        });
    }

    private static List<File> findPlaylists(List<File> toVisit) {
        List<File> playlists = new ArrayList<>();
        if (toVisit.isEmpty()) {
            return playlists;
        }

        File directory = toVisit.remove(0);
        File[] contents = directory.listFiles();
        if (contents == null) {
            System.err.println("Could not list directory: " + directory);
            System.err.flush();
        } else {
            Arrays.stream(contents).forEach(entry -> {
                if (entry.isDirectory()) {
                    toVisit.add(entry);
                }
                if (entry.isFile() && isPlaylistFile(entry)) {
                    playlists.add(entry);
                }
            });
        }
        playlists.addAll(findPlaylists(toVisit));
        return playlists;
    }

}
