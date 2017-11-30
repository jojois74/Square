package box.shoe.gameutils;

/**
 * Created by Joseph on 10/24/2017.
 */

/**
 * A field which can store a supposed value which does not overwrite the previous value until it is confirmed
 * This means that it can store two values at once
 */
public class ConfirmableValue<T>
{
    private T impressedValue;
    private T confirmedValue;

    public ConfirmableValue(T initialValue)
    {
        impressedValue = initialValue;
        confirmedValue = initialValue;
    }

    public T getImpressedValue()
    {
        return impressedValue;
    }

    public void setImpressedValue(T newValue)
    {
        impressedValue = newValue;
    }

    public T getConfirmedValue()
    {
        return confirmedValue;
    }

    public void confirmValue()
    {
        confirmedValue = impressedValue;
    }
}
