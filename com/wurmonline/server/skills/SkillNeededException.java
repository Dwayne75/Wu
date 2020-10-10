package com.wurmonline.server.skills;

import com.wurmonline.shared.exceptions.WurmServerException;

public class SkillNeededException
  extends WurmServerException
{
  private static final long serialVersionUID = 928122916198689152L;
  
  public SkillNeededException(String message)
  {
    super(message);
  }
  
  public SkillNeededException(Throwable cause)
  {
    super(cause);
  }
  
  public SkillNeededException(String message, Throwable cause)
  {
    super(message, cause);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\server\skills\SkillNeededException.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */