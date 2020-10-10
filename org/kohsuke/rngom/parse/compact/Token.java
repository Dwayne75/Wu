package org.kohsuke.rngom.parse.compact;

public class Token
{
  public int kind;
  public int beginLine;
  public int beginColumn;
  public int endLine;
  public int endColumn;
  public String image;
  public Token next;
  public Token specialToken;
  
  public String toString()
  {
    return this.image;
  }
  
  public static final Token newToken(int ofKind)
  {
    switch (ofKind)
    {
    }
    return new Token();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\kohsuke\rngom\parse\compact\Token.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */