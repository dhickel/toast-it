package io.mindspice.toastit.shell.evaluators;

import com.github.freva.asciitable.ColumnData;
import io.mindspice.mindlib.data.tuples.Pair;
import io.mindspice.toastit.App;
import io.mindspice.toastit.entries.TodoManager;
import io.mindspice.toastit.shell.InputPrompt;
import io.mindspice.toastit.shell.ShellCommand;
import io.mindspice.toastit.util.TableConfig;
import io.mindspice.toastit.util.TableUtil;
import io.mindspice.toastit.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.Set;


public class TodoEval extends ShellEvaluator<TodoEval> {
    public final TodoManager todoManager = App.instance().getTodoManager();
    public final InputPrompt<String> prompt = new InputPrompt<>(todoManager.getAllItems());

    @Override
    public String modeDisplay() {
        clearScreen();
        return TableUtil.generateTable(prompt.getIndexedItems(), TableConfig.TODO_VIEW_TABLE) + "\n";
    }

    public TodoEval() {
        commands.addAll(List.of(
                ShellCommand.of("add", TodoEval::onAddItem),
                ShellCommand.of(Set.of("remove", "delete"), TodoEval::onRemoveItem))

        );
    }

    public String onAddItem(String input) {
        String item = Util.removeFirstWord(input);
        if (item.isEmpty() || !confirmPrompt(String.format("Add Item: %s ?", item))) {
            item = promptInput("Enter New Item: ");
        }
        todoManager.addItem(item);
        prompt.addItem(item);
        return modeDisplay();
    }

    public String onRemoveItem(String input) {
        String item = Util.removeFirstWord(input);
        String resp = (Util.isInt(item)
                       ? prompt.create()
                               .validateAndGetIndex(item)
                               .confirm(this::confirmPrompt, i -> String.format("Remove Item: %s ?", i))
                               .itemConsumer(todoManager::removeItem)
                               .listRemove()
                               .display(i -> "Removed Item:" + i)
                       : prompt.create()
                               .forceSelect(Util.stringMatch(prompt.getItems(), item))
                               .confirm(this::confirmPrompt, i -> String.format("Remove Item: %s ?", i))
                               .itemConsumer(todoManager::removeItem)
                               .listRemove()
                               .display(i -> "Removed Item:" + i));
        return modeDisplay() + resp;
    }
}
