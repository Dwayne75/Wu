package org.fourthline.cling.support.model.dlna.types;

public class ScmsFlagType
{
  private boolean copyright;
  private boolean original;
  
  public ScmsFlagType()
  {
    this.copyright = true;
    this.original = true;
  }
  
  public ScmsFlagType(boolean copyright, boolean original)
  {
    this.copyright = copyright;
    this.original = original;
  }
  
  public boolean isCopyright()
  {
    return this.copyright;
  }
  
  public boolean isOriginal()
  {
    return this.original;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\fourthline\cling\support\model\dlna\types\ScmsFlagType.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       0.7.1
 */