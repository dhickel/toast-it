package io.mindspice.toastit.shell;

import io.mindspice.toastit.shell.evaluators.DirectoryEval;
import io.mindspice.kawautils.wrappers.KawaInstance;
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
import io.mindspice.toastit.util.Settings;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Consumer;


public class ApplicationShell {
    private final SshServer sshd;
    private KawaInstance kawa;
    private ShellCompleter completer = new ShellCompleter();
    private CustomWidgets customWidgets = null;
    private OutputStream output;
    private File scratchPad = new File("src/main/resources/scheme/scratch-pad.scm");
    DirectoryEval dirManager;
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
            System.err.println(e.getMessage());
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

    public ShellFactory createShellFactory() {
        Consumer<Ssh.ShellParams> shellLogic = shellParams -> {
            terminal = shellParams.getTerminal();
            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.flush();
            output = terminal.output();

            LineReader reader = initLineReader();
            initWidgets(reader);

            completer.loadSchemeCompletions(kawa);

            List<ShellMode<?>> modes = Settings.SHELL_MODES;
            modes.forEach(mode -> mode.modeInstance().init(terminal, reader));
            ShellMode<?> currMode = modes.getFirst();

            while (true) {
                try {
                    String inputLine = reader.readLine(currMode.promptSymbol().trim());
                    if (inputLine.isEmpty()) { continue; }
                    switch (inputLine) {
                        case String s when s.startsWith("exit") -> onExit(reader, shellParams);
                        case String s when s.startsWith("clear") -> onClear(terminal);
                        case String s when s.startsWith("help") -> onHelp(output);
                        default -> {
                            for (var mode : modes) {
                                if (mode.test(inputLine)) {
                                    if (currMode != mode) {
                                        reader.setVariable(LineReader.SECONDARY_PROMPT_PATTERN, mode.altPromptSymbol());
                                        currMode = mode;
                                        completer.setMode(mode.mode());
                                        terminal.puts(InfoCmp.Capability.clear_screen);
                                        terminal.flush();
                                        output.write(mode.modeInstance().modeDisplay.get().getBytes());
                                    }
                                }
                            }

                            for (var alias : currMode.aliases()) {
                                if (inputLine.startsWith(alias)) {
                                    inputLine = inputLine.replaceFirst(alias, "").trim();
                                    break;
                                }
                            }

                            if (inputLine.isEmpty()) { continue; }
                            String returnOutput = currMode.modeInstance().eval(inputLine);
                            if (returnOutput != null) {
                                output.write(returnOutput.trim().getBytes());
                            }
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        };
        return new ShellFactoryImpl(shellLogic);
    }

    public void onExit(LineReader reader, Ssh.ShellParams shellParams) throws IOException {
        if (reader.readLine("Confirm Exit (y|n): ").contains("y")) {
            shellParams.getSession().close();
        }
    }

    public void onClear(Terminal terminal) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
    }

    public LineReader initLineReader() {
        return LineReaderBuilder.builder()
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
    }

    public CustomWidgets initWidgets(LineReader reader) {
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
        return customWidgets;
    }

    public void onHelp(OutputStream output) {

    }

}
