package com.sun.codemodel;

final class JBreak
  implements JStatement
{
  private final JLabel label;
  
  JBreak(JLabel _label)
  {
    this.label = _label;
  }
  
  public void state(JFormatter f)
  {
    if (this.label == null) {
      f.p("break;").nl();
    } else {
      f.p("break").p(this.label.label).p(';').nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JBreak.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */