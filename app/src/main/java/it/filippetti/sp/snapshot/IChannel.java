package it.filippetti.sp.snapshot;

import java.net.URI;

public interface IChannel extends IMeasurable, IFlowState {

    String getLabel();

    URI getSource();

    URI getTarget();

    void setSource(URI source);

    void setTarget(URI source);

}
