package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.SchemaFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SchemaConstraintChecker
{
  public static boolean check(InputSource[] schemas, ErrorReceiver errorHandler, EntityResolver entityResolver)
  {
    ErrorReceiverFilter errorFilter = new ErrorReceiverFilter(errorHandler);
    boolean hadErrors = false;
    
    SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
    sf.setErrorHandler(errorFilter);
    if (entityResolver != null) {
      sf.setResourceResolver(new LSResourceResolver()
      {
        public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
        {
          try
          {
            InputSource is = this.val$entityResolver.resolveEntity(namespaceURI, systemId);
            if (is == null) {
              return null;
            }
            return new LSInputSAXWrapper(is);
          }
          catch (SAXException e)
          {
            return null;
          }
          catch (IOException e) {}
          return null;
        }
      });
    }
    try
    {
      sf.newSchema(getSchemaSource(schemas));
    }
    catch (SAXException e)
    {
      hadErrors = true;
    }
    catch (OutOfMemoryError e)
    {
      errorHandler.warning(null, Messages.format("SchemaConstraintChecker.UnableToCheckCorrectness", new Object[0]));
    }
    return (!hadErrors) && (!errorFilter.hadError());
  }
  
  private static Source[] getSchemaSource(InputSource[] schemas)
  {
    SAXSource[] sources = new SAXSource[schemas.length];
    for (int i = 0; i < schemas.length; i++) {
      sources[i] = new SAXSource(schemas[i]);
    }
    return sources;
  }
  
  public static void main(String[] args)
    throws IOException
  {
    InputSource[] sources = new InputSource[args.length];
    for (int i = 0; i < args.length; i++) {
      sources[i] = new InputSource(new File(args[i]).toURL().toExternalForm());
    }
    check(sources, new ConsoleErrorReporter(), null);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\xjc\reader\xmlschema\parser\SchemaConstraintChecker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */