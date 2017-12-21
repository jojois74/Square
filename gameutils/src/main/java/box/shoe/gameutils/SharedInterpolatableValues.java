package box.shoe.gameutils;

import org.w3c.dom.Node;

/**
 * Created by Joseph on 12/22/2017.
 */

public class SharedInterpolatableValues //aka what a big mess! (W.A.B.M.)
{
    /*package*/ Node activeNode;
    private int size;

    public SharedInterpolatableValues()
    {
        activeNode = null;
        size = 0;
    }

    public int size()
    {
        return size;
    }

    public void push(Object source, Number value)
    {
        Node node = new Node(source, value);

        if (activeNode != null)
        {
            node.next = activeNode;
        }

        activeNode = node;

        size++;
    }

    public Number pop(Object source)
    {
        if (activeNode == null)
        {
            throw new IllegalStateException("No more elements to pop!");
        }
        else
        {
            Node temp = activeNode;
            if (temp.source != source)
            {
                throw new IllegalAccessError("Cannot pop another object's value!");
            }
            activeNode = activeNode.next;
            return temp.value;
        }
    }

    /*package*/ SharedInterpolatableValues interpolateTo(SharedInterpolatableValues other, double interpolationRatio)
    {
        if (size() != other.size())
        {
            throw new IllegalStateException("Interpolation values count mismatch. You must interpolate a constant number of values from each Entity!");
        }

        SharedInterpolatableValues interpolatableValues = new SharedInterpolatableValues();

        Node currentNode = activeNode;
        Node currentNodeOther = other.activeNode;
        do
        {
            double a = activeNode.value.doubleValue();
            double b = currentNodeOther.value.doubleValue();
            interpolatableValues.push(activeNode.source ,a * (interpolationRatio + 1) - (interpolationRatio * b));
        }
        while ((currentNode = currentNode.next) != null);

        return interpolatableValues;
    }

    private class Node
    {
        private Node next;

        private final Number value;

        private final Object source;

        private Node(Object source, Number value)
        {
            next = null;
            this.source = source;
            this.value = value;
        }
    }
}
