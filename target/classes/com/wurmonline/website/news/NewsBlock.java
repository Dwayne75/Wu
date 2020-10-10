package com.wurmonline.website.news;

import com.wurmonline.website.Block;
import com.wurmonline.website.LoginInfo;
import java.io.PrintWriter;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

public class NewsBlock
  extends Block
{
  private News news;
  
  public NewsBlock(News aNews)
  {
    this.news = aNews;
  }
  
  public void write(HttpServletRequest req, PrintWriter out, LoginInfo loginInfo)
  {
    out.print("<b>" + this.news.getTitle() + "</b> posted " + new Date(this.news.getTimestamp()) + " by " + this.news.getPostedBy() + "<br>");
    
    out.print("<br>");
    out.print(this.news.getText());
    if ((loginInfo != null) && (loginInfo.isAdmin())) {
      out.print("<hr>ADMIN [ <a href=\"news.jsp?id=" + this.news.getId() + "&action=edit\">Edit</a> | <a href=\"news.jsp?id=" + this.news.getId() + "&action=delete\">Delete</a> ]");
    }
  }
  
  protected News getNews()
  {
    return this.news;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\news\NewsBlock.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */