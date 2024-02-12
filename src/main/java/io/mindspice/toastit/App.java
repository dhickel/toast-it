package io.mindspice.toastit;

import io.mindspice.toastit.sqlite.DBConnection;
import io.mindspice.toastit.entries.event.EventManager;
import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.toastit.shell.ApplicationShell;
import io.mindspice.toastit.util.Settings;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class App {

    private static final App INSTANCE;
    private KawaInstance scheme;
    private ApplicationShell shell;
    private DBConnection dbConnection;
    private ScheduledExecutorService exec;

    //Managers
    private EventManager eventManager;

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
            System.err.println("Error loading init.scm: " + loadResult.exception().orElseThrow());
            System.out.println(Arrays.toString(loadResult.exception().get().getStackTrace()));
        }

        dbConnection = new DBConnection();


    }

    public App init() throws IOException {
        scheme.defineObject("ShellInstance", shell);
        scheme.defineObject("AppInstance", this);
        exec = Executors.newScheduledThreadPool(Settings.EXEC_THREADS);
        eventManager = new EventManager();
        eventManager.init();

        var loadResult = scheme.loadSchemeFile(new File("scheme_files/post-init.scm"));
        if (!loadResult.valid()) {
            System.err.println("Error loading post-init.scm: " + loadResult.exception().orElseThrow());
            System.out.println(Arrays.toString(loadResult.exception().get().getStackTrace()));
        }
        shell = new ApplicationShell(scheme);

        return INSTANCE;
    }

    public DBConnection getDatabase() {
        return dbConnection;
    }

    public ScheduledExecutorService getExec() {
        return exec;
    }

    public EventManager getEventManager() {
        return eventManager;
    }


}