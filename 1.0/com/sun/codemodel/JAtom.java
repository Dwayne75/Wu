package com.sun.codemodel;

class JAtom
  extends JExpressionImpl
{
  String what;
  
  JAtom(String what)
  {
    this.what = what;
  }
  
  public void generate(JFormatter f)
  {
    f.p(this.what);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JAtom.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */