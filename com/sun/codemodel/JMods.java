package com.sun.codemodel;

import java.io.PrintWriter;
import java.io.StringWriter;

public class JMods
  implements JGenerable
{
  private static int VAR = 8;
  private static int FIELD = 799;
  private static int METHOD = 255;
  private static int CLASS = 63;
  private static int INTERFACE = 1;
  private int mods;
  
  private JMods(int mods)
  {
    this.mods = mods;
  }
  
  public int getValue()
  {
    return this.mods;
  }
  
  private static void check(int mods, int legal, String what)
  {
    if ((mods & (legal ^ 0xFFFFFFFF)) != 0) {
      throw new IllegalArgumentException("Illegal modifiers for " + what + ": " + new JMods(mods).toString());
    }
  }
  
  static JMods forVar(int mods)
  {
    check(mods, VAR, "variable");
    return new JMods(mods);
  }
  
  static JMods forField(int mods)
  {
    check(mods, FIELD, "field");
    return new JMods(mods);
  }
  
  static JMods forMethod(int mods)
  {
    check(mods, METHOD, "method");
    return new JMods(mods);
  }
  
  static JMods forClass(int mods)
  {
    check(mods, CLASS, "class");
    return new JMods(mods);
  }
  
  static JMods forInterface(int mods)
  {
    check(mods, INTERFACE, "class");
    return new JMods(mods);
  }
  
  public boolean isAbstract()
  {
    return (this.mods & 0x20) != 0;
  }
  
  public boolean isNative()
  {
    return (this.mods & 0x40) != 0;
  }
  
  public boolean isSynchronized()
  {
    return (this.mods & 0x80) != 0;
  }
  
  public void setSynchronized(boolean newValue)
  {
    setFlag(128, newValue);
  }
  
  private void setFlag(int bit, boolean newValue)
  {
    this.mods = (this.mods & (bit ^ 0xFFFFFFFF) | (newValue ? bit : 0));
  }
  
  public void generate(JFormatter f)
  {
    if ((this.mods & 0x1) != 0) {
      f.p("public");
    }
    if ((this.mods & 0x2) != 0) {
      f.p("protected");
    }
    if ((this.mods & 0x4) != 0) {
      f.p("private");
    }
    if ((this.mods & 0x8) != 0) {
      f.p("final");
    }
    if ((this.mods & 0x10) != 0) {
      f.p("static");
    }
    if ((this.mods & 0x20) != 0) {
      f.p("abstract");
    }
    if ((this.mods & 0x40) != 0) {
      f.p("native");
    }
    if ((this.mods & 0x80) != 0) {
      f.p("synchronized");
    }
    if ((this.mods & 0x100) != 0) {
      f.p("transient");
    }
    if ((this.mods & 0x200) != 0) {
      f.p("volatile");
    }
  }
  
  public String toString()
  {
    StringWriter s = new StringWriter();
    JFormatter f = new JFormatter(new PrintWriter(s));
    generate(f);
    return s.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\codemodel\JMods.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */