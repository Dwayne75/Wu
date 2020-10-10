package com.wurmonline.website;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

public abstract class Section
{
  public abstract List<Block> getBlocks(HttpServletRequest paramHttpServletRequest, LoginInfo paramLoginInfo);
  
  public abstract String getId();
  
  public abstract String getName();
  
  public void handlePost(HttpServletRequest req, LoginInfo loginInfo) {}
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\website\Section.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */