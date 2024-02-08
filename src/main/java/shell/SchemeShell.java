package shell;

import calendar.Calendar;
import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.kawautils.wrappers.functional.FuncRef;
import io.mindspice.kawautils.wrappers.functional.FuncType;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.mindlib.data.tuples.Triple;
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
import org.jline.widget.Widgets;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.System.out;


public class SchemeShell {
    private final SshServer sshd;
    private final Map<String, FuncRef> procedures = new HashMap<>();
    private KawaInstance kawa;
    private ShellMode mode = ShellMode.COMMAND;
    private SchemeShellCompleter completer = new SchemeShellCompleter();
    private CommandInterface commandInterface;
    private Consumer<String> externalInputConsumer;
    private CustomWidgets customWidgets = null;
    private OutputStream output;
    private Consumer<Boolean> eventToggle;
    private File scratchPad = new File("src/main/resources/scheme/scratch-pad.scm");
    DirectoryManager dirManager;
    Terminal terminal;



    public SchemeShell(int port, CommandInterface commandInterface, KawaInstance kawa) throws IOException {
        this.kawa = kawa;
        this.commandInterface = commandInterface;

        sshd = SshServer.setUpDefaultServer();
        sshd.setPort(port);
        sshd.setHost("127.0.0.1");
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(Paths.get("hostkey.ser")));
        sshd.setPasswordAuthenticator(new AuthInstance());
        sshd.setShellFactory(createShellFactory());
        sshd.start();

        externalInputConsumer = (string) -> {
            if (output == null) { return; }
            try {
                output.write("\n".getBytes());
                output.write(("▹ " + string.replace("\n", "+\n▹ ")).getBytes());
                output.write("\n".getBytes());
            } catch (IOException e) {
                out.println(e.getMessage());
            }
        };

        kawa.defineObject("print-consumer", printToTerminal);
        kawa.safeEval("(define (print obj) print-consumer:accept obj)");

    }

    public Consumer<Object> printToTerminal = (Object object) -> {
        try {
            output.write(("▹ " + object.toString().replace("\n", "+\n▹ ")).getBytes());
        } catch (IOException e) {
            out.println(e.getMessage());
        }
    };

    public void addEventToggle(Consumer<Boolean> eventToggle) {
        this.eventToggle = eventToggle;
    }

    public void refreshCompletions() {
        completer.loadSchemeCompletions(kawa);
    }

    public Consumer<String> getExternalInputConsumer() {
        return externalInputConsumer;
    }

    private static class AuthInstance implements PasswordAuthenticator {
        @Override
        public boolean authenticate(String username, String password, ServerSession session) {
            return "user".equals(username) && "password".equals(password);
        }
    }

    public String modePrompt(LineReader lineReader, SchemeShellCompleter completer, ShellMode mode) {
        completer.setMode(mode);
        switch (mode) {
            case COMMAND -> {
                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, "#▹▹ ");
                return "#|▹ ";
            }
            case SCHEME -> {
                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, "λ▹▹ ");
                return "λ|▹ ";
            }
            case SYSTEM -> {
                lineReader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, "⛘▹▹ ");
                return "⛘|▹ ";
            }

            default -> { return "#|> "; }
        }
    }

    public String modePromptMulti(ShellMode mode) {
        completer.setMode(mode);
        switch (mode) {
            case COMMAND -> { return "#▹▹ "; }
            case SCHEME -> { return "λ▹▹ "; }
            case SYSTEM -> { return "⛘▹▹ "; }
            default -> { return "#▹▹ "; }
        }
    }

    public static class CustomWidgets extends Widgets {

        public CustomWidgets(LineReader reader) {
            super(reader);
        }

        public void bindWidget(String refName, CharSequence seq, Widget boolFunc) {
            addWidget(refName, boolFunc);
            getKeyMap().bind(new Reference(refName), seq);
        }

        public void bindWidget(Triple<String, CharSequence, Widget> binding) {
            addWidget(binding.first(), binding.third());
            getKeyMap().bind(new Reference(binding.first()), binding.second());
        }
    }

    public ShellFactory createShellFactory() {
        Consumer<Ssh.ShellParams> shellLogic = shellParams -> {
            try {
                terminal = shellParams.getTerminal();
                OutputStream out = terminal.output();
                output = out;
                dirManager = new DirectoryManager(terminal);
                completer.loadSchemeCompletions(kawa);

                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .history(new DefaultHistory())
                        .highlighter(new DefaultHighlighter())
                        .completer(completer)
                        .option(LineReader.Option.AUTO_FRESH_LINE, true)
                        .option(LineReader.Option.HISTORY_IGNORE_DUPS, true)
                        .option(LineReader.Option.AUTO_MENU, true)
                        .option(LineReader.Option.MENU_COMPLETE, true)
                        // .option(LineReader.Option.LIST_AMBIGUOUS, true)
                        .option(LineReader.Option.LIST_PACKED, true)
                        .build();

                customWidgets = new CustomWidgets(reader);
                customWidgets.bindWidget("eventMon", KeyMap.alt('m'), () -> {
                    eventToggle.accept(true);
                    return true;
                });
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
                    }
                    return true;
                });

                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();

                while (true) {
                    final String line = reader.readLine(modePrompt(reader, completer, mode)).trim();
                    String output = "";
                    switch (line) {
                        case String s when "exit".equalsIgnoreCase(s.trim()) -> shellParams.getSession().close();
                        case String s when "help".equalsIgnoreCase(s.trim()) -> output = helpString;
                        case String s when s.toLowerCase().trim().startsWith("scheme") -> {
                            mode = ShellMode.SCHEME;
                            output = schemeCommand(line);
                        }
                        case String s when s.toLowerCase().trim().startsWith("command") -> {
                            mode = ShellMode.COMMAND;
                            if (commandInterface == null) {
                                output = "Error: No command interface assigned";
                                break;
                            }
                            output = parseCommand(line);
                        }
                        case String s when s.toLowerCase().trim().startsWith("system") -> {
                            mode = ShellMode.SYSTEM;
                            output = dirManager.processCommand(line);
                        }
                        case String s when "clear".equalsIgnoreCase(s.trim()) -> {
                            terminal.puts(InfoCmp.Capability.clear_screen);
                            terminal.flush();
                        }
                        case String s when s.toLowerCase().contains("load-scheme-file") -> {
                            try {
                                var file = dirManager.getCurrPath().resolve(s.trim().split(" ")[1]).toFile();
                                var r = kawa.loadSchemeFile(file);
                                if (r.exception().isPresent()) {
                                    output = "Error: Could not load file: " + r.exception().get().getMessage();
                                } else {
                                    output = "Loaded scheme file: " + file;
                                    completer.loadSchemeCompletions(kawa);
                                }
                            } catch (Exception e) {
                                output = "Error: Could not load file: " + e.getMessage();
                            }
                        }
                        default -> {
                            switch (mode) {
                                case COMMAND -> {
                                    if (commandInterface == null) {
                                        output = "Error: No command interface assigned";
                                        break;
                                    }
                                    output = parseCommand(line);
                                }
                                case SYSTEM -> output = dirManager.processCommand(line);
                                case SCHEME -> output = schemeCommand(line);
                            }
                        }
                    }
                    if (output.isEmpty()) { continue; }
                    out.write(("▹ " + output.replace("\n", "\n▹ ")).getBytes());

                    out.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Throwable e) {
                try {
                    out.write(("Error: " + e.getMessage()).getBytes());
                } catch (IOException ex) { /*Ignored*/}
            }
        };

        return new ShellFactoryImpl(shellLogic);
    }

    private String schemeCommand(String line) {
        if (line.startsWith("scheme")) {
            line = line.replace("scheme", "");
        }

        if (line.isEmpty()) { return ""; }
        String[] input = line.trim().split(" ");

        switch (input[0]) {
            case String s when "--user-definitions".equalsIgnoreCase(s) -> {
                return String.join("\n", kawa.userDefinitions());
            }
            case String s when "--reload-auto-complete".equalsIgnoreCase(s) -> {
                completer.loadSchemeCompletions(kawa);
                return "Reloaded scheme auto-completion";
            }
            case String s when "--scratch-pad".equalsIgnoreCase(s) -> {
                dirManager.launchNano(scratchPad, terminal);
                return "";
            }
            case String s when "--scratch-load".equalsIgnoreCase(s) -> {
                var result = kawa.loadSchemeFile(scratchPad);
                if (result.exception().isPresent()) {
                    return result.exception().get().toString();
                } else {
                    return result.result().isPresent()
                            ? result.result().get().toString()
                            : "No Error encounter, No Result Returned";
                }
            }
            case String s when "--scratch-clear".equalsIgnoreCase(s) -> {
                try {
                    Files.write(Paths.get(scratchPad.toURI()), new byte[0], StandardOpenOption.TRUNCATE_EXISTING);
                } catch (IOException e) {
                    return "Error: Error encountered clearing scratch pad";
                }
                return "Scratch pad cleared";
            }
            case String s when s.toLowerCase().trim().startsWith("--scratch-append-to") -> {
                if (input.length < 2) { return "Error: Must specify file to append to"; }
                try {
                    File copyTo = new File(input[1]);
                    Files.write(Paths.get(copyTo.toURI()), Collections.singleton(System.lineSeparator()), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    byte[] content = Files.readAllBytes(Paths.get(scratchPad.toURI()));
                    Files.write(Paths.get(copyTo.toURI()), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (IOException e) {
                    return "Error: Error Copying scratch pad to specified location";
                }
                return "Append scratch contents to: " + input[1];
            }
            case String s when "--load-procedure".equalsIgnoreCase(s) -> {
                if (input.length != 3) { return "Error: Invalid number of arguments, expected 2"; }
                return loadSchemeProcedure(input[1], input[2]);
            }

            default -> {
                var result = kawa.safeEval(line);
                if (result.exception().isPresent()) {
                    return result.exception().get().toString();
                } else {
                    return result.result().isPresent()
                            ? result.result().get().toString()
                            : "No Error encounter, No Result Returned";
                }
            }
        }
    }

    private String loadSchemeProcedure(String funcType, String funcName) {
        FuncType funcEnum = FuncType.fromString(funcType);
        if (funcEnum == null) {
            return ("Error: Failed enum value lookup for: " + funcType);
        }
        out.println("func-enum:" + funcEnum);

        var result = kawa.safeEval(funcName);
        if (result.exception().isPresent() || result.result().isEmpty()) {
            return result.exception().get().getMessage();
        }

        procedures.put(funcName, FuncRef.of(funcEnum, result.result().get()));
        return ("Stored scheme procedure: " + funcName);
    }

    private String parseCommand(String input) {
        out.println(input);
        return input;
    }

    public String helpString = """
                        
            #####################
            ## Global Commands ##
            #####################
                
            "command"
              -> Switch to command console.
              
            "scheme"
              -> Switch to scheme repl
              -> Commands:
                -> --user-definitions : List user defined procedures
                -> --reload auto-complete : Reload auto complete definitions
                -> --scratch-pad : open up the scheme scratch pad
                -> --scratch-load : load definitions from scratch pad into environment
                -> --scratch-append-to <file> : append definitions in scratch file to specified file
                
             "browse"
              -> Switch to file browser and editor
              -> Sub commands: ls, cd, mv, rm, mkdir, cp, touch, nano
              
            "load-scheme-file <file>"
                -> Loads the contents of a scheme file into environment
              
            "exit"
              -> Exit and close connection
              
            "clear"
              -> Clear screen
              
            ################
            ## Key Macros ##
            ################
            
            "alt-s"
             -> saves current terminal input to the scheme scratch pad
            """;


}
