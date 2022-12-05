package us.dot.its.jpo.ode.messagesender;

import lombok.Data;

@Data
public class TimestampedCoordinate {
    long timestamp;
    double[] coords;
}
