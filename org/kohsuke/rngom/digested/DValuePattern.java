package org.kohsuke.rngom.digested;

import org.kohsuke.rngom.parse.Context;

public class DValuePattern
  extends DPattern
{
  private String datatypeLibrary;
  private String type;
  private String value;
  private Context context;
  private String ns;
  
  public DValuePattern(String datatypeLibrary, String type, String value, Context context, String ns)
  {
    this.datatypeLibrary = datatypeLibrary;
    this.type = type;
    this.value = value;
    this.context = context;
    this.ns = ns;
  }
  
  public String getDatatypeLibrary()
  {
    return this.datatypeLibrary;
  }
  
  public String getType()
  {
    return this.type;
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
  
  public boolean isNullable()
  {
    return false;
  }
  
  public Object accept(DPatternVisitor visitor)
  {
    return visitor.onValue(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DValuePattern.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */