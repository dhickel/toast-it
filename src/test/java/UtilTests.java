import org.junit.Test;
import util.Util;

import java.time.DayOfWeek;
import java.time.Month;


public class UtilTests {


    @Test
    public void fuzzyEnumMatch(){
        var match1 = Util.enumMatch(Month.values(),"jul");
        var match2 = Util.enumMatch(Month.values(),"mar");
        var match3 = Util.enumMatch(Month.values(), "october");
        var match4 = Util.enumMatch(Month.values(),"may");
        var match5 = Util.enumMatch(DayOfWeek.values(), "tu");
        var match6 = Util.enumMatch(DayOfWeek.values(), "sat");
        var match7 = Util.enumMatch(DayOfWeek.values(), "WedNEsDAY");
        var match8 = Util.enumMatch(DayOfWeek.values(), "october");
        assert match1 == Month.JULY;
        assert match2 == Month.MARCH;
        assert match3 == Month.OCTOBER;
        assert match4 == Month.MAY;
        System.out.println(match5);
        assert match5 == DayOfWeek.TUESDAY;
        assert match6 == DayOfWeek.SATURDAY;
        assert match7 == DayOfWeek.WEDNESDAY;
        System.out.println(match8);
        System.out.println(Util.fuzzyMatchLength("october".toUpperCase(), DayOfWeek.MONDAY.name()));
        assert match8 == null;

    }
}
