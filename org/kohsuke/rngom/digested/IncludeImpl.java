package org.kohsuke.rngom.digested;

import java.util.HashSet;
import java.util.Set;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.GrammarSection.Combine;
import org.kohsuke.rngom.ast.builder.Include;
import org.kohsuke.rngom.ast.builder.IncludedGrammar;
import org.kohsuke.rngom.ast.builder.Scope;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.IllegalSchemaException;
import org.kohsuke.rngom.parse.Parseable;

final class IncludeImpl
  extends GrammarBuilderImpl
  implements Include
{
  private Set overridenPatterns = new HashSet();
  private boolean startOverriden = false;
  
  public IncludeImpl(DGrammarPattern p, Scope parent, DSchemaBuilderImpl sb)
  {
    super(p, parent, sb);
  }
  
  public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
    throws BuildException
  {
    super.define(name, combine, pattern, loc, anno);
    if (name == "\000#start\000") {
      this.startOverriden = true;
    } else {
      this.overridenPatterns.add(name);
    }
  }
  
  public void endInclude(Parseable current, String uri, String ns, Location loc, Annotations anno)
    throws BuildException, IllegalSchemaException
  {
    current.parseInclude(uri, this.sb, new IncludedGrammarImpl(this.grammar, this.parent, this.sb), ns);
  }
  
  private class IncludedGrammarImpl
    extends GrammarBuilderImpl
    implements IncludedGrammar
  {
    public IncludedGrammarImpl(DGrammarPattern p, Scope parent, DSchemaBuilderImpl sb)
    {
      super(parent, sb);
    }
    
    public void define(String name, GrammarSection.Combine combine, ParsedPattern pattern, Location loc, Annotations anno)
      throws BuildException
    {
      if (name == "\000#start\000")
      {
        if (!IncludeImpl.this.startOverriden) {}
      }
      else if (IncludeImpl.this.overridenPatterns.contains(name)) {
        return;
      }
      super.define(name, combine, pattern, loc, anno);
    }
    
    public ParsedPattern endIncludedGrammar(Location loc, Annotations anno)
      throws BuildException
    {
      return null;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\IncludeImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */