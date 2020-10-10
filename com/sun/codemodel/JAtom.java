package com.sun.codemodel;

final class JAtom
  extends JExpressionImpl
{
  private final String what;
  
  JAtom(String what)
  {
    this.what = what;
  }
  
  public void generate(JFormatter f)
  {
    f.p(this.what);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JAtom.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */