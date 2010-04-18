/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.view;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import edu.illinois.ncsa.mmdb.web.client.presenter.BatchOperationPresenter;

/**
 * @author Luigi Marini
 * 
 */
public class BatchOperationView extends Composite implements BatchOperationPresenter.Display {

    private final FlowPanel mainLayout;
    private final Label     numSelectedLabel;
    private final MenuBar   actionsMenu;

    public BatchOperationView() {
        mainLayout = new FlowPanel();
        mainLayout.addStyleName("batchOperationPanel");
        initWidget(mainLayout);
        MenuBar actionsBar = new MenuBar();
        actionsBar.setWidth("120px");
        actionsBar.addStyleName("batchOperationMenu");
        actionsMenu = new MenuBar(true);
        actionsBar.addItem("Actions", actionsMenu);

        mainLayout.add(actionsBar);
        numSelectedLabel = new Label("0 selected datasets");
        numSelectedLabel.addStyleName("batchOperationCount");
        mainLayout.add(numSelectedLabel);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setNumSelected(int num) {
        numSelectedLabel.setText(num + " selected datasets");
    }

    @Override
    public void addMenuAction(String name, Command command) {
        MenuItem menuItem = new MenuItem(name, command);
        actionsMenu.addItem(menuItem);
    }
}
