package org.testshift.testcube.branches.rendering;

import net.sourceforge.plantuml.NewpagedDiagram;
import net.sourceforge.plantuml.TitledDiagram;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.cucadiagram.DisplayPositionned;
import net.sourceforge.plantuml.error.PSystemError;
import net.sourceforge.plantuml.sequencediagram.Event;
import net.sourceforge.plantuml.sequencediagram.Newpage;
import net.sourceforge.plantuml.sequencediagram.SequenceDiagram;

import java.util.ArrayList;
import java.util.List;

public class Titles {
    private final List<String> titles;

    public Titles(net.sourceforge.plantuml.core.Diagram diagram) {
        this.titles = initTitles(diagram);
    }

    static List<String> initTitles(net.sourceforge.plantuml.core.Diagram diagram) {
        List<String> titles = new ArrayList<>();
        if (diagram instanceof SequenceDiagram) {
            SequenceDiagram sequenceDiagram = (SequenceDiagram) diagram;
            MyBlock.addTitle(titles, sequenceDiagram.getTitle().getDisplay());
            List<Event> events = sequenceDiagram.events();
            for (Event event : events) {
                if (event instanceof Newpage) {
                    Display title = ((Newpage) event).getTitle();
                    MyBlock.addTitle(titles, title);
                }
            }
        } else if (diagram instanceof NewpagedDiagram) {
            NewpagedDiagram newpagedDiagram = (NewpagedDiagram) diagram;
            List<net.sourceforge.plantuml.core.Diagram> diagrams = newpagedDiagram.getDiagrams();
            for (net.sourceforge.plantuml.core.Diagram diagram1 : diagrams) {
                if (diagram1 instanceof UmlDiagram) {
                    DisplayPositionned title = ((UmlDiagram) diagram1).getTitle();
                    MyBlock.addTitle(titles, title.getDisplay());
                }
            }
        } else if (diagram instanceof UmlDiagram) {
            DisplayPositionned title = ((UmlDiagram) diagram).getTitle();
            MyBlock.addTitle(titles, title.getDisplay());
        } else if (diagram instanceof PSystemError) {
            DisplayPositionned title = ((PSystemError) diagram).getTitle();
            if (title == null) {
                titles.add(null);
            } else {
                MyBlock.addTitle(titles, title.getDisplay());
            }
        } else if (diagram instanceof TitledDiagram) {
            MyBlock.addTitle(titles, ((TitledDiagram) diagram).getTitle().getDisplay());
        } else {
            titles.add(null);
        }
        return titles;
    }

    public String getTitle(int page) {
        if (titles.size() > page) {
            return titles.get(page);
        } else {
//            logger.error("page is too big = " + page + " " + this);
            return null;
        }
    }
}
