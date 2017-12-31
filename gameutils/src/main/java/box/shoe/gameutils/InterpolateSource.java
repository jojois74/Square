package box.shoe.gameutils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Created by Joseph on 12/23/2017.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface InterpolateSource
{
    String id();
}

