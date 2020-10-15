package it.filippetti.sp.snapshot;

import java.net.URI;
import java.util.Calendar;

public interface IFlowState {
    String getID();

    String getKey();

    String getLabel();

    URI getMetadata();

    URI getSource();

    URI getTarget();

    Long getTimestamp();

    String getUnit();

    Number getValue();

    public IFlowState putCalendar(Calendar timestamp);

    public IFlowState putExtension(String extension);


}
