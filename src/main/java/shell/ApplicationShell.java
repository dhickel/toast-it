package shell;

import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.mindlib.functional.consumers.BiPredicatedBiConsumer;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.ShellFactory;
import org.jline.builtins.ssh.ShellFactoryImpl;
import org.jline.builtins.ssh.Ssh;
import org.jline.keymap.KeyMap;
import org.jline.reader.*;
import org.jline.reader.impl.DefaultHighlighter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp;
import util.Settings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;


public class ApplicationShell {
    private final SshServer sshd;
    private KawaInstance kawa;
    private SchemeShellCompleter completer = new SchemeShellCompleter();
    private CustomWidgets customWidgets = null;
    private OutputStream output;
    private File scratchPad = new File("src/main/resources/scheme/scratch-pad.scm");
    DirectoryManager dirManager;
    Terminal terminal;




    public ApplicationShell(KawaInstance kawa) throws IOException {
        this.kawa = kawa;
        sshd = SshServer.setUpDefaultServer();
        sshd.setHost(Settings.SHELL_BIND_ADDRESS);
        sshd.setPort(Settings.SHELL_BIND_PORT);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get(Settings.SHELL_KEY_PAIR)));
        sshd.setPasswordAuthenticator(new AuthInstance());
        sshd.setShellFactory(createShellFactory());
        sshd.start();

        kawa.defineObject("print-consumer", printToTerminal);
        kawa.safeEval("(define (print obj) print-consumer:accept obj)");
    }

    public Consumer<Object> printToTerminal = (Object object) -> {
        try {
            output.write(object.toString().replace("\n", "+\n ").getBytes());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    };


    private static class AuthInstance implements PasswordAuthenticator {
        @Override
        public boolean authenticate(String username, String password, ServerSession session) {
            return Settings.SHELL_USER.equals(username) && Settings.SHELL_PASSWORD.equals(password);
        }
    }

    public void refreshSchemeCompletions() {
        //completer.loadSchemeCompletions(kawa);
    }

//    public String modePrompt(LineReader lineReader, SchemeShellCompleter completer, ShellModeOld mode) {
//        completer.setMode(mode);
//        switch (mode) {
//            case COMMAND -> {
//                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, Settings.SHELL_COMMAND_PROMPT_ALT);
//                return Settings.SHELL_COMMAND_PROMPT;
//            }
//            case SCHEME -> {
//                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, Settings.SHELL_SCHEME_PROMPT_ALT);
//                return Settings.SHELL_SCHEME_PROMPT;
//            }
//            case SYSTEM -> {
//                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, Settings.SHELL_SYSTEM_PROMPT_ALT);
//                return Settings.SHELL_SYSTEM_PROMPT;
//            }
//
//            default -> { return Settings.SHELL_DEFAULT_PROMPT; }
//        }
//    }

    public List<BiPredicatedBiConsumer<ApplicationShell, String>> procs = new ArrayList<>();

    public void addProc(BiPredicate<ApplicationShell, String> pred, BiConsumer<ApplicationShell, String> cons) {
        procs.add(BiPredicatedBiConsumer.of(pred, cons));
    }

    private String[] splitInput(String line) {
        return line.toLowerCase().trim().split(" ");
    }

    public ShellFactory createShellFactory() {
        Consumer<Ssh.ShellParams> shellLogic = shellParams -> {

            terminal = shellParams.getTerminal();
            output = terminal.output();
           // completer.loadSchemeCompletions(kawa);
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();

            dirManager = new DirectoryManager();

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .history(new DefaultHistory())
                    .highlighter(new DefaultHighlighter())
                    .completer(completer)
                    .option(LineReader.Option.AUTO_FRESH_LINE, true)
                    .option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
                    .option(LineReader.Option.AUTO_MENU, true)
                    .option(LineReader.Option.MENU_COMPLETE, true)
                    .option(LineReader.Option.LIST_PACKED, true)
                    .build();

            CustomWidgets customWidgets = new CustomWidgets(reader);
            customWidgets.bindWidget("saveToScratch", KeyMap.alt("s"), () -> {
                try {
                    String bufferTest = reader.getBuffer().toString();
                    int cursorPosition = reader.getBuffer().cursor();
                    Files.write(Paths.get(scratchPad.toURI()),
                            Arrays.asList(System.lineSeparator(), bufferTest),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);

                    reader.getBuffer().clear();
                    reader.callWidget(LineReader.REDRAW_LINE);
                    reader.callWidget(LineReader.REDISPLAY);
                    terminal.writer().println("Appended line(s) to scratch pad");
                    terminal.flush();
                    reader.getBuffer().cursor(cursorPosition);
                    reader.getBuffer().write(bufferTest);
                    reader.callWidget(LineReader.REDRAW_LINE);
                    reader.callWidget(LineReader.REDISPLAY);
                } catch (IOException e) {
                    printToTerminal.accept(e.getMessage());
                }
                return true;
            });

            dirManager.init(terminal, reader, completer);

            while (true) {
            }

        };
        return new ShellFactoryImpl(shellLogic);
    }

//    private String schemeCommand(String line) {
//        if (line.startsWith("scheme")) {
//            line = line.replace("scheme", "");
//        }
//
//        if (line.isEmpty()) { return ""; }
//        String[] input = line.trim().split(" ");
//
//        switch (input[0]) {
//            case String s when "--user-definitions".equalsIgnoreCase(s) -> {
//                return String.join("\n", kawa.userDefinitions());
//            }
//            case String s when "--reload-auto-complete".equalsIgnoreCase(s) -> {
//                completer.loadSchemeCompletions(kawa);
//                return "Reloaded scheme auto-completion";
//            }
//            case String s when "--scratch-pad".equalsIgnoreCase(s) -> {
//                dirManager.launchNano(scratchPad, terminal);
//                return "";
//            }
//            case String s when "--scratch-load".equalsIgnoreCase(s) -> {
//                var result = kawa.loadSchemeFile(scratchPad);
//                if (result.exception().isPresent()) {
//                    return result.exception().get().toString();
//                } else {
//                    return result.result().isPresent()
//                            ? result.result().get().toString()
//                            : "No Error encounter, No Result Returned";
//                }
//            }
//            case String s when "--scratch-clear".equalsIgnoreCase(s) -> {
//                try {
//                    Files.write(Paths.get(scratchPad.toURI()), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
//                } catch (IOException e) {
//                    return "Error: Error encountered clearing scratch pad";
//                }
//                return "Scratch pad cleared";
//            }
//            case String s when s.toLowerCase().trim().startsWith("--scratch-append-to") -> {
//                if (input.length < 2) { return "Error: Must specify file to append to"; }
//                try {
//                    File copyTo = new File(input[1]);
//                    Files.write(Paths.get(copyTo.toURI()), Collections.singleton(System.lineSeparator()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//                    byte[] content = Files.readAllBytes(Paths.get(scratchPad.toURI()));
//                    Files.write(Paths.get(copyTo.toURI()), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//                } catch (IOException e) {
//                    return "Error: Error Copying scratch pad to specified location";
//                }
//                return "Append scratch contents to: " + input[1];
//            }
//            case String s when "--load-procedure".equalsIgnoreCase(s) -> {
//                if (input.length != 3) { return "Error: Invalid number of arguments, expected 2"; }
//                return loadSchemeProcedure(input[1], input[2]);
//            }
//
//            default -> {
//                var result = kawa.safeEval(line);
//                if (result.exception().isPresent()) {
//                    return result.exception().get().toString();
//                } else {
//                    return result.result().isPresent()
//                            ? result.result().get().toString()
//                            : "No Error encounter, No Result Returned";
//                }
//            }
//        }
//    }
//
//    private String loadSchemeProcedure(String funcType, String funcName) {
//        FuncType funcEnum = FuncType.fromString(funcType);
//        if (funcEnum == null) {
//            return ("Error: Failed enum value lookup for: " + funcType);
//        }
//        out.println("func-enum:" + funcEnum);
//
//        var result = kawa.safeEval(funcName);
//        if (result.exception().isPresent() || result.result().isEmpty()) {
//            return result.exception().get().getMessage();
//        }
//
//        return ("Stored scheme procedure: " + funcName);
//    }
//
//    private String parseCommand(String input) {
//        out.println(input);
//        return input;
//    }
//
//    public String helpString = """
//
//            #####################
//            ## Global Commands ##
//            #####################
//
//            "command"
//              -> Switch to command console.
//
//            "scheme"
//              -> Switch to scheme repl
//              -> Commands:
//                -> --user-definitions : List user defined procedures
//                -> --reload auto-complete : Reload auto complete definitions
//                -> --scratch-pad : open up the scheme scratch pad
//                -> --scratch-load : load definitions from scratch pad into environment
//                -> --scratch-append-to <file> : append definitions in scratch file to specified file
//
//             "browse"
//              -> Switch to file browser and editor
//              -> Sub commands: ls, cd, mv, rm, mkdir, cp, touch, nano
//
//            "load-scheme-file <file>"
//                -> Loads the contents of a scheme file into environment
//
//            "exit"
//              -> Exit and close connection
//
//            "clear"
//              -> Clear screen
//
//            ################
//            ## Key Macros ##
//            ################
//
//            "alt-s"
//             -> saves current terminal input to the scheme scratch pad
//            """;
//

}
