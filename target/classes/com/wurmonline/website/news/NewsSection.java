package com.wurmonline.website.news;

import com.wurmonline.website.Block;
import com.wurmonline.website.LoginInfo;
import com.wurmonline.website.Section;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

public class NewsSection
  extends Section
{
  private SubmitNewsBlock submitBlock = new SubmitNewsBlock();
  private List<NewsBlock> news = new ArrayList();
  
  public String getName()
  {
    return "News";
  }
  
  public String getId()
  {
    return "news";
  }
  
  public List<Block> getBlocks(HttpServletRequest req, LoginInfo loginInfo)
  {
    List<Block> list = new ArrayList();
    if ("delete".equals(req.getParameter("action"))) {
      if ((loginInfo != null) && (loginInfo.isAdmin())) {
        delete(req.getParameter("id"));
      }
    }
    list.addAll(this.news);
    if ((loginInfo != null) && (loginInfo.isAdmin())) {
      list.add(this.submitBlock);
    }
    return list;
  }
  
  private void delete(String id) {}
  
  public void handlePost(HttpServletRequest req, LoginInfo loginInfo)
  {
    if ((loginInfo != null) && (loginInfo.isAdmin()))
    {
      String title = req.getParameter("title");
      String text = req.getParameter("text");
      text = text.replaceAll("\r\n", "<br>");
      text = text.replaceAll("\r", "<br>");
      text = text.replaceAll("\n", "<br>");
      
      this.news.add(new NewsBlock(new News(title, text, loginInfo.getName())));
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\news\NewsSection.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */