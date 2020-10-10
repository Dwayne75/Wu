package org.kohsuke.rngom.xml.sax;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public abstract interface XMLReaderCreator
{
  public abstract XMLReader createXMLReader()
    throws SAXException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\xml\sax\XMLReaderCreator.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */