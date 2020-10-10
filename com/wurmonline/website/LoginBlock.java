package com.wurmonline.website;

import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

public class LoginBlock
  extends Block
{
  private void writeLoginForm(HttpServletRequest req, PrintWriter out) {}
  
  private void writeLogoutForm(HttpServletRequest req, PrintWriter out) {}
  
  public void write(HttpServletRequest req, PrintWriter out, LoginInfo loginInfo) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\website\LoginBlock.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */