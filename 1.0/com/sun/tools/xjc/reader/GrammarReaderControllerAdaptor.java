package com.sun.tools.xjc.reader;

import com.sun.msv.reader.GrammarReaderController;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import java.io.IOException;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class GrammarReaderControllerAdaptor
  extends ErrorReceiverFilter
  implements GrammarReaderController
{
  private final EntityResolver entityResolver;
  
  public GrammarReaderControllerAdaptor(ErrorReceiver core, EntityResolver _entityResolver)
  {
    super(core);
    this.entityResolver = _entityResolver;
  }
  
  public void warning(Locator[] locs, String msg)
  {
    boolean firstTime = true;
    if (locs != null) {
      for (int i = 0; i < locs.length; i++) {
        if (locs[i] != null)
        {
          if (firstTime) {
            warning(locs[i], msg);
          } else {
            warning(locs[i], Messages.format("GrammarReaderControllerAdaptor.RelevantLocation"));
          }
          firstTime = false;
        }
      }
    }
    if (firstTime) {
      warning((Locator)null, msg);
    }
  }
  
  public void error(Locator[] locs, String msg, Exception e)
  {
    boolean firstTime = true;
    if (locs != null) {
      for (int i = 0; i < locs.length; i++) {
        if (locs[i] != null)
        {
          if (firstTime) {
            error(locs[i], msg);
          } else {
            error(locs[i], Messages.format("GrammarReaderControllerAdaptor.RelevantLocation"));
          }
          firstTime = false;
        }
      }
    }
    if (firstTime) {
      error((Locator)null, msg);
    }
  }
  
  public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException
  {
    if (this.entityResolver == null) {
      return null;
    }
    return this.entityResolver.resolveEntity(publicId, systemId);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\GrammarReaderControllerAdaptor.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */