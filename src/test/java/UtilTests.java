import io.mindspice.mindlib.util.JsonUtils;
import io.mindspice.toastit.App;
import io.mindspice.toastit.sqlite.DBConnection;
import io.mindspice.toastit.util.DateTimeUtil;
import io.mindspice.toastit.util.Settings;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.time.*;

import static org.junit.Assert.*;


public class UtilTests {
    private static App app;

    @BeforeClass
    public static void init() {
        try {
            app = App.instance().init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void fuzzyEnumMatch() {
        var match1 = Util.enumMatch(Month.values(), "jul");
        var match2 = Util.enumMatch(Month.values(), "mar");
        var match3 = Util.enumMatch(Month.values(), "october");
        var match4 = Util.enumMatch(Month.values(), "may");
        var match5 = Util.enumMatch(DayOfWeek.values(), "tu");
        var match6 = Util.enumMatch(DayOfWeek.values(), "sat");
        var match7 = Util.enumMatch(DayOfWeek.values(), "WedNEsDAY");
        var match8 = Util.enumMatch(DayOfWeek.values(), "october");
        assertEquals(match1, Month.JULY);
        assertEquals(match2, Month.MARCH);
        assertEquals(match3, Month.OCTOBER);
        assertEquals(match4, Month.MAY);
        assertEquals(match5, DayOfWeek.TUESDAY);
        assertEquals(match6, DayOfWeek.SATURDAY);
        assertEquals(match7, DayOfWeek.WEDNESDAY);
        assertEquals(match8, null);

    }

    @Test
    public void dateParsing() throws IOException {
        assertEquals(LocalDate.of(2023, Month.OCTOBER, 24), DateTimeUtil.parseDateInput("10/24/23"));
        assertEquals(LocalTime.of(20, 23), DateTimeUtil.parseTimeInput("20:23"));
        assertEquals(LocalTime.of(20, 23), DateTimeUtil.parseTimeInput("8:23pm"));
        assertEquals(LocalTime.of(20, 23), DateTimeUtil.parseTimeInput("8:23PM"));
        assertEquals(LocalTime.of(20, 23), DateTimeUtil.parseTimeInput("8:23 pm"));
        assertEquals(LocalTime.of(20, 23), DateTimeUtil.parseTimeInput("8:23 PM"));


    }
}
