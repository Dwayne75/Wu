package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedNameClass;

final class ParsedNameClassHost
  implements ParsedNameClass
{
  final ParsedNameClass lhs;
  final ParsedNameClass rhs;
  
  ParsedNameClassHost(ParsedNameClass lhs, ParsedNameClass rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\ParsedNameClassHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */