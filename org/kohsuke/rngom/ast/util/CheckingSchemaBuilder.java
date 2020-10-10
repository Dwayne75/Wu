package org.kohsuke.rngom.ast.util;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.binary.SchemaBuilderImpl;
import org.kohsuke.rngom.binary.SchemaPatternBuilder;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.host.ParsedPatternHost;
import org.kohsuke.rngom.parse.host.SchemaBuilderHost;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;

public class CheckingSchemaBuilder
  extends SchemaBuilderHost
{
  public CheckingSchemaBuilder(SchemaBuilder sb, ErrorHandler eh)
  {
    super(new SchemaBuilderImpl(eh), sb);
  }
  
  public CheckingSchemaBuilder(SchemaBuilder sb, ErrorHandler eh, DatatypeLibraryFactory dlf)
  {
    super(new SchemaBuilderImpl(eh, dlf, new SchemaPatternBuilder()), sb);
  }
  
  public ParsedPattern expandPattern(ParsedPattern p)
    throws BuildException, IllegalSchemaException
  {
    ParsedPatternHost r = (ParsedPatternHost)super.expandPattern(p);
    return r.rhs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\ast\util\CheckingSchemaBuilder.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */