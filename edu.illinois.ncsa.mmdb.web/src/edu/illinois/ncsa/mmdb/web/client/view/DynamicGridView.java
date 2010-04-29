package edu.illinois.ncsa.mmdb.web.client.view;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.presenter.DynamicGridPresenter.Display;
import edu.illinois.ncsa.mmdb.web.client.ui.PreviewWidget;

/**
 * 
 * @author Luigi Marini
 * 
 */
public class DynamicGridView extends FlexTable implements Display {

    private final HashMap<CheckBox, String> checkBoxes;
    private final static DateTimeFormat     DATE_TIME_FORMAT  = DateTimeFormat.getShortDateTimeFormat();
    public static final int                 DEFAULT_PAGE_SIZE = 24;
    private final int                       ROW_WIDTH         = 6;
    private int                             numItems          = 0;

    public DynamicGridView() {
        super();
        addStyleName("dynamicGrid");
        checkBoxes = new HashMap<CheckBox, String>();
    }

    @Override
    public int insertItem(final String id, String name, String type, Date date, String preview, String size, String authorId) {

        VerticalPanel layoutPanel = new VerticalPanel();
        layoutPanel.addStyleName("dynamicGridElement");
        layoutPanel.setHeight("130px");
        layoutPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        HorizontalPanel titlePanel = new HorizontalPanel();
        titlePanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // preview
        PreviewWidget pre = new PreviewWidget(id, GetPreviews.SMALL, "dataset?id=" + id);
        pre.setWidth("120px");
        pre.setMaxWidth(100);
        layoutPanel.add(pre);

        // selection checkbox
        CheckBox checkBox = new CheckBox();
        titlePanel.add(checkBox);

        // title
        Hyperlink hyperlink = new Hyperlink(shortenTitle(name), "dataset?id=" + id);
        hyperlink.addStyleName("smallText");
        hyperlink.addStyleName("inline");
        hyperlink.setWidth("100px");
        titlePanel.add(hyperlink);

        layoutPanel.add(titlePanel);
        layoutPanel.setCellHeight(titlePanel, "20px");

        final int row = this.getRowCount();

        setWidget(numItems / ROW_WIDTH, numItems % ROW_WIDTH, layoutPanel);

        numItems++;

        return row;
    }

    private String shortenTitle(String title) {
        if (title.length() > 15) {
            return title.substring(0, 10) + "...";
        } else {
            return title;
        }
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
