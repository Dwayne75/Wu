package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.util.CheckingSchemaBuilder;
import org.kohsuke.rngom.parse.Parseable;
import org.kohsuke.rngom.parse.compact.CompactParseable;
import org.kohsuke.rngom.parse.xml.SAXParseable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class Main
{
  public static void main(String[] args)
    throws Exception
  {
    ErrorHandler eh = new DefaultHandler()
    {
      public void error(SAXParseException e)
        throws SAXException
      {
        throw e;
      }
    };
    Parseable p;
    Parseable p;
    if (args[0].endsWith(".rng")) {
      p = new SAXParseable(new InputSource(args[0]), eh);
    } else {
      p = new CompactParseable(new InputSource(args[0]), eh);
    }
    SchemaBuilder sb = new CheckingSchemaBuilder(new DSchemaBuilderImpl(), eh);
    try
    {
      p.parse(sb);
    }
    catch (BuildException e)
    {
      if ((e.getCause() instanceof SAXException))
      {
        SAXException se = (SAXException)e.getCause();
        if (se.getException() != null) {
          se.getException().printStackTrace();
        }
      }
      throw e;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\Main.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */