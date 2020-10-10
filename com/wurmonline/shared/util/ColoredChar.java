package com.wurmonline.shared.util;

public class ColoredChar
{
  public char chr;
  public float color;
  
  public ColoredChar(char chr, byte color)
  {
    this.chr = chr;
    this.color = color;
  }
  
  public char getChr()
  {
    return this.chr;
  }
  
  public void setChr(char chr)
  {
    this.chr = chr;
  }
  
  public float getColor()
  {
    return this.color;
  }
  
  public void setColor(float color)
  {
    this.color = color;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\ColoredChar.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */