package com.sun.xml.xsom.parser;

import java.io.IOException;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract interface XMLParser
{
  public abstract void parse(InputSource paramInputSource, ContentHandler paramContentHandler, ErrorHandler paramErrorHandler, EntityResolver paramEntityResolver)
    throws SAXException, IOException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\xsom\parser\XMLParser.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */