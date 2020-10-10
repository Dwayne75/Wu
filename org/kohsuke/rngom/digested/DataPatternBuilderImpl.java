package org.kohsuke.rngom.digested;

import java.util.List;
import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;
import org.xml.sax.Locator;

final class DataPatternBuilderImpl
  implements DataPatternBuilder
{
  private final DDataPattern p;
  
  public DataPatternBuilderImpl(String datatypeLibrary, String type, Location loc)
  {
    this.p = new DDataPattern();
    this.p.location = ((Locator)loc);
    this.p.datatypeLibrary = datatypeLibrary;
    this.p.type = type;
  }
  
  public void addParam(String name, String value, Context context, String ns, Location loc, Annotations anno)
    throws BuildException
  {
    DDataPattern tmp15_12 = this.p;tmp15_12.getClass();this.p.params.add(new DDataPattern.Param(tmp15_12, name, value, context.copy(), ns, loc, (Annotation)anno));
  }
  
  public void annotation(ParsedElementAnnotation ea) {}
  
  public ParsedPattern makePattern(Location loc, Annotations anno)
    throws BuildException
  {
    return makePattern(null, loc, anno);
  }
  
  public ParsedPattern makePattern(ParsedPattern except, Location loc, Annotations anno)
    throws BuildException
  {
    this.p.except = ((DPattern)except);
    if (anno != null) {
      this.p.annotation = ((Annotation)anno).getResult();
    }
    return this.p;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DataPatternBuilderImpl.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */