package org.kohsuke.rngom.digested;

import java.util.ArrayList;
import java.util.List;
import org.kohsuke.rngom.ast.om.Location;
import org.kohsuke.rngom.parse.Context;

public class DDataPattern
  extends DPattern
{
  DPattern except;
  String datatypeLibrary;
  String type;
  final List<Param> params;
  
  public DDataPattern()
  {
    this.params = new ArrayList();
  }
  
  public final class Param
  {
    String name;
    String value;
    Context context;
    String ns;
    Location loc;
    Annotation anno;
    
    public Param(String name, String value, Context context, String ns, Location loc, Annotation anno)
    {
      this.name = name;
      this.value = value;
      this.context = context;
      this.ns = ns;
      this.loc = loc;
      this.anno = anno;
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public String getValue()
    {
      return this.value;
    }
    
    public Context getContext()
    {
      return this.context;
    }
    
    public String getNs()
    {
      return this.ns;
    }
    
    public Location getLoc()
    {
      return this.loc;
    }
    
    public Annotation getAnno()
    {
      return this.anno;
    }
  }
  
  public String getDatatypeLibrary()
  {
    return this.datatypeLibrary;
  }
  
  public String getType()
  {
    return this.type;
  }
  
  public List<Param> getParams()
  {
    return this.params;
  }
  
  public DPattern getExcept()
  {
    return this.except;
  }
  
  public boolean isNullable()
  {
    return false;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onData(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DDataPattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */