package com.wurmonline.shared.util;

public class MulticolorLineSegment
{
  private byte color;
  private String text;
  
  public MulticolorLineSegment(String text, byte color)
  {
    this.text = text.replaceAll("\\p{C}", "?");
    this.color = color;
  }
  
  public ColoredChar[] convertToCharArray()
  {
    ColoredChar[] arr = new ColoredChar[getText().length()];
    for (int i = 0; i < getText().length(); i++) {
      arr[i] = new ColoredChar(getText().charAt(i), getColor());
    }
    return arr;
  }
  
  public void setText(String text)
  {
    this.text = text;
  }
  
  public String getText()
  {
    return this.text;
  }
  
  public void setColor(byte color)
  {
    this.color = color;
  }
  
  public byte getColor()
  {
    return this.color;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\MulticolorLineSegment.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */