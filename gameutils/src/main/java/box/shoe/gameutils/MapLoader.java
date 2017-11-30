package box.shoe.gameutils;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import static android.R.attr.id;
import static android.R.attr.switchMinWidth;

/**
 * Created by Joseph on 11/17/2017.
 * Loads a tile map created in Tiled. (Uncomplete, Untested)
 *
 * XML in non JavaScript languages is horrible, hence my possible desire to create my own XML parsing library?
 */

public class MapLoader
{
    Map currentMap;
    XmlPullParser xmlParser;

    public MapLoader()
    {

    }
/*
    public Map loadMapFromXml(InputStream input) throws IOException, XmlPullParserException
    {
        currentMap = new Map();

        try
        {
            xmlParser = Xml.newPullParser();
            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            xmlParser.setInput(input, null);
            //xmlParser.require(XmlPullParser.START_TAG, null, );

            //Width/Height
            xmlParser.require(XmlPullParser.START_TAG, null, "map");
            xmlParser.next();



            //Tilesets
            //xmlParser.require(XmlPullParser.START_TAG, null, "tileset");

            int event;
            while ((event = xmlParser.next()) != XmlPullParser.END_DOCUMENT)
            {
                String name = xmlParser.getName();

                if (event == XmlPullParser.START_TAG)
                {
                    switch (name)
                    {
                        case "map": //Get map attributes
                            currentMap.mapWidth = Integer.parseInt(xmlParser.getAttributeValue(null, "width"));
                            currentMap.mapHeight = Integer.parseInt(xmlParser.getAttributeValue(null, "height"));

                            currentMap.tileWidth = Integer.parseInt(xmlParser.getAttributeValue(null, "tilewidth"));
                            currentMap.tileHeight = Integer.parseInt(xmlParser.getAttributeValue(null, "tileheight"));

                            break;

                        case "tileset":
                            Tileset tileset = new Tileset();
                            tileset.imageHeight = ;
                            tileset.imageWidth = ;
                            tileset.imagePath = ;
                            tileset.firstGid = ;
                            tileset.name = ;
                            break;
                    }
                }
                else if (event == XmlPullParser.END_TAG)
                {

                }
            }

            getTilesets();
            getLayers();

        }
        finally
        {
            input.close();
        }
        return new Map();
    }
*/
    private static class Map
    {
        private int mapWidth;
        private int mapHeight;

        private int tileWidth;
        private int tileHeight;

        private java.util.Map objects; //TODO: generic params
        private java.util.Map collisions; //TODO: generic params

        private Map()
        {

        }
    }

    private static class Tileset
    {
        private int firstGid;
        private String name;

        private int imageWidth;
        private int imageHeight;
        private String imagePath;
    }
}
