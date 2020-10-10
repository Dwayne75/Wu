package org.kohsuke.rngom.digested;

public class DDefine
{
  private final String name;
  private DPattern pattern;
  private Boolean nullable;
  DAnnotation annotation;
  
  public DDefine(String name)
  {
    this.name = name;
  }
  
  public DPattern getPattern()
  {
    return this.pattern;
  }
  
  public DAnnotation getAnnotation()
  {
    if (this.annotation == null) {
      return DAnnotation.EMPTY;
    }
    return this.annotation;
  }
  
  public void setPattern(DPattern pattern)
  {
    this.pattern = pattern;
    this.nullable = null;
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public boolean isNullable()
  {
    if (this.nullable == null) {
      this.nullable = (this.pattern.isNullable() ? Boolean.TRUE : Boolean.FALSE);
    }
    return this.nullable.booleanValue();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DDefine.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */