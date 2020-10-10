package com.wurmonline.website;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

public abstract class Block
{
  public abstract void write(HttpServletRequest paramHttpServletRequest, PrintWriter paramPrintWriter, LoginInfo paramLoginInfo);
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\Block.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */