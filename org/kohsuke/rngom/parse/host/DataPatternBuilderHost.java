package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.builder.Annotations;
import org.kohsuke.rngom.ast.builder.BuildException;
import org.kohsuke.rngom.ast.builder.DataPatternBuilder;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;
import org.kohsuke.rngom.ast.om.ParsedPattern;
import org.kohsuke.rngom.parse.Context;

final class DataPatternBuilderHost
  extends Base
  implements DataPatternBuilder
{
  final DataPatternBuilder lhs;
  final DataPatternBuilder rhs;
  
  DataPatternBuilderHost(DataPatternBuilder lhs, DataPatternBuilder rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
  
  public void addParam(String name, String value, Context context, String ns, Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    this.lhs.addParam(name, value, context, ns, loc.lhs, anno.lhs);
    this.rhs.addParam(name, value, context, ns, loc.rhs, anno.rhs);
  }
  
  public void annotation(ParsedElementAnnotation _ea)
  {
    ParsedElementAnnotationHost ea = (ParsedElementAnnotationHost)_ea;
    
    this.lhs.annotation(ea.lhs);
    this.rhs.annotation(ea.rhs);
  }
  
  public ParsedPattern makePattern(Location _loc, Annotations _anno)
    throws BuildException
  {
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makePattern(loc.lhs, anno.lhs), this.rhs.makePattern(loc.rhs, anno.rhs));
  }
  
  public ParsedPattern makePattern(ParsedPattern _except, Location _loc, Annotations _anno)
    throws BuildException
  {
    ParsedPatternHost except = (ParsedPatternHost)_except;
    LocationHost loc = cast(_loc);
    AnnotationsHost anno = cast(_anno);
    
    return new ParsedPatternHost(this.lhs.makePattern(except.lhs, loc.lhs, anno.lhs), this.rhs.makePattern(except.rhs, loc.rhs, anno.rhs));
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\DataPatternBuilderHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */