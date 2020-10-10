package com.sun.mail.handlers;

import javax.activation.ActivationDataFlavor;

public class text_html
  extends text_plain
{
  private static ActivationDataFlavor myDF = new ActivationDataFlavor(String.class, "text/html", "HTML String");
  
  protected ActivationDataFlavor getDF()
  {
    return myDF;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\mail\handlers\text_html.class
 * Java compiler version: 4 (48.0)
 * JD-Core Version:       0.7.1
 */