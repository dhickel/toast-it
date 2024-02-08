package shell;

import org.jline.reader.LineReader;
import org.jline.reader.Reference;
import org.jline.reader.Widget;
import org.jline.widget.Widgets;


public  class CustomWidgets extends Widgets {
    public CustomWidgets(LineReader reader) {
        super(reader);
    }

    public void bindWidget(String refName, CharSequence seq, Widget boolFunc) {
        addWidget(refName, boolFunc);
        getKeyMap().bind(new Reference(refName), seq);
    }
}