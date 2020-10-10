package org.kohsuke.rngom.parse.host;

import org.kohsuke.rngom.ast.om.ParsedElementAnnotation;

final class ParsedElementAnnotationHost
  implements ParsedElementAnnotation
{
  final ParsedElementAnnotation lhs;
  final ParsedElementAnnotation rhs;
  
  ParsedElementAnnotationHost(ParsedElementAnnotation lhs, ParsedElementAnnotation rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\host\ParsedElementAnnotationHost.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */