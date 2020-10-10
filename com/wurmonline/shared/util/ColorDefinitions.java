package com.wurmonline.shared.util;

public final class ColorDefinitions
{
  public static final float[] COLOR_SYSTEM = { 0.5F, 1.0F, 0.5F };
  public static final float[] COLOR_ERROR = { 1.0F, 0.3F, 0.3F };
  public static final float[] COLOR_WHITE = { 1.0F, 1.0F, 1.0F };
  public static final float[] COLOR_BLACK = { 0.0F, 0.0F, 0.0F };
  public static final float[] COLOR_NAVY_BLUE = { 0.23F, 0.39F, 1.0F };
  public static final float[] COLOR_GREEN = { 0.08F, 1.0F, 0.08F };
  public static final float[] COLOR_RED = { 1.0F, 0.0F, 0.0F };
  public static final float[] COLOR_MAROON = { 0.5F, 0.0F, 0.0F };
  public static final float[] COLOR_PURPLE = { 0.5F, 0.0F, 0.5F };
  public static final float[] COLOR_ORANGE = { 1.0F, 0.85F, 0.24F };
  public static final float[] COLOR_YELLOW = { 1.0F, 1.0F, 0.0F };
  public static final float[] COLOR_LIME = { 0.0F, 1.0F, 0.0F };
  public static final float[] COLOR_TEAL = { 0.0F, 0.5F, 0.5F };
  public static final float[] COLOR_CYAN = { 0.0F, 1.0F, 1.0F };
  public static final float[] COLOR_ROYAL_BLUE = { 0.23F, 0.39F, 1.0F };
  public static final float[] COLOR_FUCHSIA = { 1.0F, 0.0F, 1.0F };
  public static final float[] COLOR_GREY = { 0.5F, 0.5F, 0.5F };
  public static final float[] COLOR_SILVER = { 0.75F, 0.75F, 0.75F };
  
  public static float[] getColor(byte colorCode)
  {
    switch (colorCode)
    {
    case 0: 
      return COLOR_WHITE;
    case 1: 
      return COLOR_BLACK;
    case 2: 
      return COLOR_NAVY_BLUE;
    case 3: 
      return COLOR_GREEN;
    case 4: 
      return COLOR_RED;
    case 5: 
      return COLOR_MAROON;
    case 6: 
      return COLOR_PURPLE;
    case 7: 
      return COLOR_ORANGE;
    case 8: 
      return COLOR_YELLOW;
    case 9: 
      return COLOR_LIME;
    case 10: 
      return COLOR_TEAL;
    case 11: 
      return COLOR_CYAN;
    case 12: 
      return COLOR_ROYAL_BLUE;
    case 13: 
      return COLOR_FUCHSIA;
    case 14: 
      return COLOR_GREY;
    case 15: 
      return COLOR_SILVER;
    case 100: 
      return COLOR_SYSTEM;
    case 101: 
      return COLOR_ERROR;
    }
    return COLOR_BLACK;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\shared\util\ColorDefinitions.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */