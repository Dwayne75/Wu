package org.kohsuke.rngom.parse;

import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.SchemaBuilder;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.ParsedPattern;

public abstract interface Parseable
{
  public abstract <P extends ParsedPattern> P parse(SchemaBuilder<?, P, ?, ?, ?, ?> paramSchemaBuilder)
    throws BuildException, IllegalSchemaException;
  
  public abstract <P extends ParsedPattern> P parseInclude(String paramString1, SchemaBuilder<?, P, ?, ?, ?, ?> paramSchemaBuilder, IncludedGrammar<P, ?, ?, ?, ?> paramIncludedGrammar, String paramString2)
    throws BuildException, IllegalSchemaException;
  
  public abstract <P extends ParsedPattern> P parseExternal(String paramString1, SchemaBuilder<?, P, ?, ?, ?, ?> paramSchemaBuilder, Scope paramScope, String paramString2)
    throws BuildException, IllegalSchemaException;
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\Parseable.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */