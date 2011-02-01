package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class GetSectionResult implements Result {
    List<Section> sections;

    public List<Section> getSections() {
        if (sections == null) {
            sections = new ArrayList<Section>();
        }
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public void addSection(Section s) {
        getSections().add(s);
    }
}
