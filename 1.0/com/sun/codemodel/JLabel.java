package com.sun.codemodel;

public class JLabel
  implements JStatement
{
  final String label;
  
  JLabel(String _label)
  {
    this.label = _label;
  }
  
  public void state(JFormatter f)
  {
    f.p(this.label + ':').nl();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JLabel.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */