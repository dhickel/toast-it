import calendar.Calendar;
import calendar.CalendarCell;
import gnu.expr.CompiledProc;
import gnu.mapping.Procedure;
import gnu.mapping.Procedure1;
import io.mindspice.kawautils.wrappers.KawaInstance;
import io.mindspice.kawautils.wrappers.functional.consumers.KawaConsumer;
import io.mindspice.kawautils.wrappers.functional.consumers.KawaConsumer2;
import io.mindspice.kawautils.wrappers.functional.functions.KawaFunction;
import io.mindspice.kawautils.wrappers.functional.predicates.KawaPredicate;
import shell.SchemeShell;
import shell.ShellMode;

import java.io.File;
import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.function.Consumer;


public class Main {

    public static void main(String[] args) throws IOException {

        KawaInstance kawa = new KawaInstance();
        var s = kawa.loadSchemeFile(new File("DATA/SCHEME/init.scm"));
        SchemeShell shell = new SchemeShell(2233, null, kawa);
    }


}
