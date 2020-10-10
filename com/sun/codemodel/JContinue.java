package com.sun.codemodel;

class JContinue
  implements JStatement
{
  private final JLabel label;
  
  JContinue(JLabel _label)
  {
    this.label = _label;
  }
  
  public void state(JFormatter f)
  {
    if (this.label == null) {
      f.p("continue;").nl();
    } else {
      f.p("continue").p(this.label.label).p(';').nl();
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JContinue.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */