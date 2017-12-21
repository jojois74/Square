package box.shoe.gameutils;

import java.util.List;

/**
 * Created by Joseph on 12/21/2017.
 */

public interface Interpolatable //TODO: part of paintable, it should be
{
    List<Double> getInterpolatables();
    void giveInterpolatables(List<Double> interpolatedValues);

    void wasInterpolated();
    void wasNotInterpolated();
}
