package com.sun.mail.handlers;

import java.awt.Image;
import javax.activation.ActivationDataFlavor;

public class image_jpeg
  extends image_gif
{
  private static ActivationDataFlavor myDF = new ActivationDataFlavor(Image.class, "image/jpeg", "JPEG Image");
  
  protected ActivationDataFlavor getDF()
  {
    return myDF;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\handlers\image_jpeg.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */