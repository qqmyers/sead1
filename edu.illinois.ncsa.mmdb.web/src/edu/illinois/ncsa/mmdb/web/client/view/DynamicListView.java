package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicListPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicListView extends FlexTable implements Display {

    private final HashMap<CheckBox, String> checkBoxes;
    private final static DateTimeFormat     DATE_TIME_FORMAT = DateTimeFormat.getShortDateTimeFormat();

    public DynamicListView() {
        super();
        addStyleName("dynamicTableList");
        checkBoxes = new HashMap<CheckBox, String>();
    }

    @Override
    public int insertItem(final String id, String name, String type, Date date, String preview, String size, String authorId) {

        final int row = this.getRowCount();

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        setWidget(row, 0, checkBox);

        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
        pre.setMaxWidth(100);
        setWidget(row, 1, pre);

        VerticalPanel verticalPanel = new VerticalPanel();

        verticalPanel.setSpacing(5);

        setWidget(row, 2, verticalPanel);

        // title
        Hyperlink hyperlink = new Hyperlink(name, "dataset?id=" + id);
        verticalPanel.add(hyperlink);

        // date
        verticalPanel.add(new Label(DATE_TIME_FORMAT.format(date)));

        // size
        verticalPanel.add(new Label(size));

        // author
        verticalPanel.add(new Label(authorId));

        // type
        verticalPanel.add(new Label(type));

        getCellFormatter().addStyleName(row, 0, "cell");
        getCellFormatter().addStyleName(row, 1, "cell");
        getCellFormatter().addStyleName(row, 2, "cell");
        getCellFormatter().setVerticalAlignment(row, 1, HasVerticalAlignment.ALIGN_TOP); // FIXME move to CSS

        return row;
    }

    public String getCheckboxId(CheckBox checkbox) {
        return "";
    }

    @Override
    public Set<HasValue<Boolean>> getCheckBoxes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HasValue<Boolean> getSelected(int location) {
        return (CheckBox) getWidget(location, 0);
    }

}
