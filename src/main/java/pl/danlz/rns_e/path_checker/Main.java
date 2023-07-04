package pl.danlz.rns_e.path_checker;

import java.io.File;

public class Main {

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

        Checker checker = new Checker(path);
        checker.buildEntryList();

        checker.checkNameLength();
        checker.checkCharacters();
        checker.checkAlbumName();
        checker.checkNamesInAlbum();
        checker.checkPlaylists();

        //checker.printEntries();
        checker.printViolations();
    }

}
