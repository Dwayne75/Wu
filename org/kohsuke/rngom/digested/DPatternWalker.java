package org.kohsuke.rngom.digested;

public class DPatternWalker
  implements DPatternVisitor<Void>
{
  public Void onAttribute(DAttributePattern p)
  {
    return onXmlToken(p);
  }
  
  protected Void onXmlToken(DXmlTokenPattern p)
  {
    return onUnary(p);
  }
  
  public Void onChoice(DChoicePattern p)
  {
    return onContainer(p);
  }
  
  protected Void onContainer(DContainerPattern p)
  {
    for (DPattern c = p.firstChild(); c != null; c = c.next) {
      c.accept(this);
    }
    return null;
  }
  
  public Void onData(DDataPattern p)
  {
    return null;
  }
  
  public Void onElement(DElementPattern p)
  {
    return onXmlToken(p);
  }
  
  public Void onEmpty(DEmptyPattern p)
  {
    return null;
  }
  
  public Void onGrammar(DGrammarPattern p)
  {
    return (Void)p.getStart().accept(this);
  }
  
  public Void onGroup(DGroupPattern p)
  {
    return onContainer(p);
  }
  
  public Void onInterleave(DInterleavePattern p)
  {
    return onContainer(p);
  }
  
  public Void onList(DListPattern p)
  {
    return onUnary(p);
  }
  
  public Void onMixed(DMixedPattern p)
  {
    return onUnary(p);
  }
  
  public Void onNotAllowed(DNotAllowedPattern p)
  {
    return null;
  }
  
  public Void onOneOrMore(DOneOrMorePattern p)
  {
    return onUnary(p);
  }
  
  public Void onOptional(DOptionalPattern p)
  {
    return onUnary(p);
  }
  
  public Void onRef(DRefPattern p)
  {
    return (Void)p.getTarget().getPattern().accept(this);
  }
  
  public Void onText(DTextPattern p)
  {
    return null;
  }
  
  public Void onValue(DValuePattern p)
  {
    return null;
  }
  
  public Void onZeroOrMore(DZeroOrMorePattern p)
  {
    return onUnary(p);
  }
  
  protected Void onUnary(DUnaryPattern p)
  {
    return (Void)p.getChild().accept(this);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\digested\DPatternWalker.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */