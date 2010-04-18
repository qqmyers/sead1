package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.LabeledListBox;

public class BatchOperationListBoxView extends Composite implements Display {
    LabeledListBox       menu;
    Map<String, Command> commands = new HashMap<String, Command>();

    public BatchOperationListBoxView() {
        menu = new LabeledListBox("0 datasets selected");
        menu.getLabel().addStyleName("batchOperationCount");
        menu.addItem("Actions", "Actions");
        menu.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                String commandName = event.getValue();
                if (commandName != null && !commandName.equals("Actions")) {
                    Command command = commands.get(commandName);
                    if (command != null) {
                        command.execute();
                    }
                    menu.setSelected("Actions");
                }
            }
        });
        initWidget(menu);
    }

    @Override
    public void addMenuAction(String name, Command command) {
        menu.addItem(name, name);
        commands.put(name, command);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setNumSelected(int num) {
        menu.setText(num + " dataset" + (num == 1 ? "" : "s") + " selected");
    }

}
