package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedPattern;

public class ParsedPatternHost
  implements ParsedPattern
{
  public final ParsedPattern lhs;
  public final ParsedPattern rhs;
  
  ParsedPatternHost(ParsedPattern lhs, ParsedPattern rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\ParsedPatternHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */