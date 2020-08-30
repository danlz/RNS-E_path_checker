package pl.danlz.rns_e.path_checker;

import java.io.File;

@FunctionalInterface
public interface EntryCheck {

    void execute(File entry);
}
