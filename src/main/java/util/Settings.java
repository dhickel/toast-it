package util;

import java.io.File;


public class Settings {

    private static Settings INSTANCE = new Settings();
    public String ROOT_PATH = "DATA";
    public String TODO_PATH = ROOT_PATH + File.separator + "TODO";

    public static Settings GET() {
        return INSTANCE;
    }
}
