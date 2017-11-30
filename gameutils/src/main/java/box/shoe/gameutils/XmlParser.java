package box.shoe.gameutils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Joseph on 12/1/2017.
 * Because languages that are not JavaScript and k'yotze bahen do not understand how to do Xml
 */

public class XmlParser
{
    public static ParsedXml parse(String input)
    {
        return null;
    }

    /**
     * Based on a tree structure
     * has methods for getting elements by tag name, easily getting attributes
     */
    private class ParsedXml
    {
        private ParsedXml()
        {

        }
    }

    private class Element
    {
        private String name;
        private Map<String, String> attributes;

        private Element(String name)
        {
            this.name = name;
            attributes = new HashMap<>();
        }
    }
}
