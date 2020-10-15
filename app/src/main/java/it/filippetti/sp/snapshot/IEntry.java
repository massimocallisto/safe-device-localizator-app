package it.filippetti.sp.snapshot;

import java.io.Serializable;
import java.net.URI;

public interface IEntry extends Serializable {
    String getKey();

    URI getSource();

    Number getValue();

    Number setValue(Number value);
}
