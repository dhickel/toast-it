import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.mindlib.util.JsonUtils;
import shell.SchemeShell;

import java.io.IOException;
import java.util.HashSet;


public class Application {

    private static final Application INSTANCE;
    private KawaInstance scheme = new KawaInstance();
    private final SchemeShell shell;
    private final HashSet<String> tags = new HashSet<>();


    static {
        try {
            INSTANCE = new Application();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Application instance(){
        return INSTANCE;
    }

    private Application() throws IOException {
        JsonUtils.getMapper().registerModule(new JavaTimeModule());
        shell = new SchemeShell(3233, null, scheme);
    }
}
