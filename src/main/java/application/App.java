package application;

import io.mindspice.kawautils.wrappers.KawaInstance;
import shell.ApplicationShell;
import util.Settings;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;


public class App {

    private static final App INSTANCE;
    private KawaInstance scheme = new KawaInstance();
    private ApplicationShell shell;

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
        var loadResult = scheme.loadSchemeFile(new File("scheme_files/init.scm"));
        if (!loadResult.valid()) {
            System.err.println("Error loading init.scm: " + loadResult.exception().orElseThrow().getMessage());
        }
        shell = new ApplicationShell(scheme);
        System.out.println(Settings.SHELL_BIND_PORT);
    }

    public void init() {
        scheme.defineObject("ShellInstance", shell);
        scheme.defineObject("AppInstance", this);
    }


}
