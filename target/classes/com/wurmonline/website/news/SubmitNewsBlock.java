package com.wurmonline.website.news;

import com.wurmonline.website.Block;
import com.wurmonline.website.LoginInfo;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;

public class SubmitNewsBlock
  extends Block
{
  public void write(HttpServletRequest req, PrintWriter out, LoginInfo loginInfo)
  {
    out.print("<b>Post news</b><br>");
    out.print("<br>");
    out.print("<form method=\"POST\" action=\"" + req.getRequestURL().toString() + "\">");
    out.print("<input type=\"hidden\" name=\"section\" value=\"news\">");
    out.print("<input type=\"hidden\" name=\"action\" value=\"submitnews\">");
    out.print("Title:<br><input type=\"text\" name=\"title\" size=\"60\" maxlength=\"128\"><br>");
    out.print("Text:<br><textarea name=\"text\" cols=\"60\" rows=\"20\"></textarea><br>");
    out.print("<input type=\"submit\" value=\"Post\">");
    out.print("</form>");
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\news\SubmitNewsBlock.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */