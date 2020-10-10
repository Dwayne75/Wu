package com.sun.tools.xjc.reader.xmlschema.parser;

import com.sun.org.apache.xerces.internal.impl.Version;
import com.sun.org.apache.xerces.internal.impl.XMLEntityManager;
import com.sun.org.apache.xerces.internal.parsers.XMLGrammarPreparser;
import com.sun.org.apache.xerces.internal.util.ErrorHandlerWrapper;
import com.sun.org.apache.xerces.internal.util.XMLGrammarPoolImpl;
import com.sun.org.apache.xerces.internal.xni.XNIException;
import com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import com.sun.tools.xjc.ConsoleErrorReporter;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import com.sun.xml.bind.util.Which;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.StringTokenizer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class SchemaConstraintChecker
{
  private static final String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
  
  public static boolean check(InputSource[] schemas, ErrorReceiver errorHandler, EntityResolver entityResolver)
    throws IOException
  {
    checkXercesVersion(errorHandler);
    
    XMLGrammarPreparser preparser = new XMLGrammarPreparser();
    preparser.registerPreparser("http://www.w3.org/2001/XMLSchema", null);
    
    preparser.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);
    
    ErrorReceiverFilter filter = new ErrorReceiverFilter(errorHandler);
    preparser.setErrorHandler(new ErrorHandlerWrapper(filter));
    if (entityResolver != null) {
      preparser.setEntityResolver(new XMLFallthroughEntityResolver(new XMLEntityResolverImpl(entityResolver), new XMLEntityManager()));
    }
    preparser.setGrammarPool(new XMLGrammarPoolImpl());
    try
    {
      for (int i = 0; i < schemas.length; i++)
      {
        preparser.preparseGrammar("http://www.w3.org/2001/XMLSchema", createXMLInputSource(schemas[i]));
        rewind(schemas[i]);
      }
    }
    catch (XNIException e) {}
    return !filter.hadError();
  }
  
  private static void checkXercesVersion(ErrorReceiver errorHandler)
  {
    String version = null;
    try
    {
      version = (String)Version.class.getField("fVersion").get(null);
    }
    catch (Throwable t)
    {
      try
      {
        version = Version.getVersion();
      }
      catch (Throwable tt) {}
    }
    if (version != null)
    {
      StringTokenizer tokens = new StringTokenizer(version);
      while (tokens.hasMoreTokens())
      {
        VersionNumber v;
        try
        {
          v = new VersionNumber(tokens.nextToken());
        }
        catch (IllegalArgumentException e) {}
        continue;
        VersionNumber v;
        if (v.isOlderThan(new VersionNumber("2.2"))) {
          errorHandler.warning(null, Messages.format("SchemaConstraintChecker.XercesTooOld", Which.which(Version.class), version));
        }
        return;
      }
    }
    errorHandler.warning(null, Messages.format("SchemaConstraintChecker.UnableToCheckXercesVersion", Which.which(Version.class), version));
  }
  
  private static XMLInputSource createXMLInputSource(InputSource is)
    throws IOException
  {
    XMLInputSource xis = new XMLInputSource(is.getPublicId(), is.getSystemId(), null);
    
    xis.setByteStream(is.getByteStream());
    xis.setCharacterStream(is.getCharacterStream());
    xis.setEncoding(is.getEncoding());
    return xis;
  }
  
  private static void rewind(InputSource is)
    throws IOException
  {
    if (is.getByteStream() != null) {
      is.getByteStream().reset();
    }
    if (is.getCharacterStream() != null) {
      is.getCharacterStream().reset();
    }
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


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\tools\xjc\reader\xmlschema\parser\SchemaConstraintChecker.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */