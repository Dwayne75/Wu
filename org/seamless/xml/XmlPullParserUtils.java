package org.seamless.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

public class XmlPullParserUtils
{
  private static final Logger log = Logger.getLogger(XmlPullParserUtils.class.getName());
  static XmlPullParserFactory xmlPullParserFactory;
  
  static
  {
    try
    {
      xmlPullParserFactory = XmlPullParserFactory.newInstance();
      xmlPullParserFactory.setNamespaceAware(true);
    }
    catch (XmlPullParserException e)
    {
      log.severe("cannot create XmlPullParserFactory instance: " + e);
    }
  }
  
  public static XmlPullParser createParser(String xml)
    throws XmlPullParserException
  {
    XmlPullParser xpp = createParser();
    InputStream is;
    try
    {
      is = new ByteArrayInputStream(xml.getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      throw new XmlPullParserException("UTF-8: unsupported encoding");
    }
    xpp.setInput(is, "UTF-8");
    return xpp;
  }
  
  public static XmlPullParser createParser()
    throws XmlPullParserException
  {
    if (xmlPullParserFactory == null) {
      throw new XmlPullParserException("no XML Pull parser factory");
    }
    return xmlPullParserFactory.newPullParser();
  }
  
  public static XmlSerializer createSerializer()
    throws XmlPullParserException
  {
    if (xmlPullParserFactory == null) {
      throw new XmlPullParserException("no XML Pull parser factory");
    }
    return xmlPullParserFactory.newSerializer();
  }
  
  public static void setSerializerIndentation(XmlSerializer serializer, int indent)
  {
    if (indent > 0) {
      try
      {
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
      }
      catch (Exception e)
      {
        log.warning("error setting feature of XmlSerializer: " + e);
      }
    }
  }
  
  public static void skipTag(XmlPullParser xpp, String tag)
    throws IOException, XmlPullParserException
  {
    int event;
    do
    {
      event = xpp.next();
    } while ((event != 1) && ((event != 3) || (!xpp.getName().equals(tag))));
  }
  
  public static void searchTag(XmlPullParser xpp, String tag)
    throws IOException, XmlPullParserException
  {
    int event;
    while ((event = xpp.next()) != 1) {
      if ((event == 2) && (xpp.getName().equals(tag))) {
        return;
      }
    }
    throw new IOException(String.format("tag '%s' not found", new Object[] { tag }));
  }
  
  public static void serializeIfNotNullOrEmpty(XmlSerializer serializer, String ns, String tag, String value)
    throws Exception
  {
    if (isNullOrEmpty(value)) {
      return;
    }
    serializer.startTag(ns, tag);
    serializer.text(value);
    serializer.endTag(ns, tag);
  }
  
  public static boolean isNullOrEmpty(String s)
  {
    return (s == null) || (s.length() == 0);
  }
  
  public static void serializeIfNotEqual(XmlSerializer serializer, String ns, String tag, Object value, Object forbiddenValue)
    throws Exception
  {
    if ((value == null) || (value.equals(forbiddenValue))) {
      return;
    }
    serializer.startTag(ns, tag);
    serializer.text(value.toString());
    serializer.endTag(ns, tag);
  }
  
  public static String fixXMLEntities(String xml)
  {
    StringBuilder fixedXml = new StringBuilder(xml.length());
    
    boolean isFixed = false;
    for (int i = 0; i < xml.length(); i++)
    {
      char c = xml.charAt(i);
      if (c == '&')
      {
        String sub = xml.substring(i, Math.min(i + 10, xml.length()));
        if ((!sub.startsWith("&#")) && (!sub.startsWith("&lt;")) && (!sub.startsWith("&gt;")) && (!sub.startsWith("&amp;")) && (!sub.startsWith("&apos;")) && (!sub.startsWith("&quot;")))
        {
          isFixed = true;
          fixedXml.append("&amp;");
        }
        else
        {
          fixedXml.append(c);
        }
      }
      else
      {
        fixedXml.append(c);
      }
    }
    if (isFixed) {
      log.warning("fixed badly encoded entities in XML");
    }
    return fixedXml.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\xml\XmlPullParserUtils.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */