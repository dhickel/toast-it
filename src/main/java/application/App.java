package application;

import application.sqlite.DBConnection;
import io.mindspice.kawautils.wrappers.KawaInstance;
import shell.ApplicationShell;
import util.Settings;
import util.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;


public class App {

    private static final App INSTANCE;
    private KawaInstance scheme;
    private ApplicationShell shell;
    private DBConnection dbConnection;

    private final HashSet<String> tags = new HashSet<>();


    static {
        try {
            INSTANCE = new App();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static App instance() {
        return INSTANCE;
    }

    private App() throws IOException {
        scheme = new KawaInstance();
        scheme.defineObject("SchemeInstance", scheme);
        var loadResult = scheme.loadSchemeFile(new File("scheme_files/init.scm"));
        if (!loadResult.valid()) {
            System.err.println("Error loading init.scm: " + loadResult.exception().orElseThrow().getMessage());
        }
        shell = new ApplicationShell(scheme);
        dbConnection = new DBConnection();

    }

    public App init() throws IOException {

        scheme.defineObject("ShellInstance", shell);
        scheme.defineObject("AppInstance", this);
        return instance();
    }

    public DBConnection getDatabase() {
        return dbConnection;
    }


}
