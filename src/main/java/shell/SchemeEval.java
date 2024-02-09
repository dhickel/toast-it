package shell;

import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.kawautils.wrappers.KawaResult;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.util.List;
import java.util.Set;


public class SchemeEval extends ShellEvaluator<SchemeEval> {
    public final KawaInstance scheme;

    public SchemeEval(KawaInstance scheme) {
        this.scheme = scheme;
    }

    @Override
    public void init(Terminal terminal, LineReader lineReader) {
        super.init(terminal, lineReader);

        List<ShellCommand<SchemeEval>> cmdList = List.of(
                ShellCommand.of("--user-definitions", (self, input) -> String.join("\n", scheme.userDefinitions())),
                ShellCommand.ofMatchAny(SchemeEval::eval)
        );
        commands.addAll(cmdList);

    }

    @Override

    public String eval(String input) {
        KawaResult<?> result = scheme.safeEval(input);
        return result.exception().isPresent()
                ? result.exception().get().getMessage()
                : (result.validAndPresent() ? result.get().toString() : "#t");
    }
}
