package org.seamless.swing.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class LogCategory
{
  private String name;
  private List<Group> groups = new ArrayList();
  
  public LogCategory(String name)
  {
    this.name = name;
  }
  
  public LogCategory(String name, Group[] groups)
  {
    this.name = name;
    this.groups = Arrays.asList(groups);
  }
  
  public String getName()
  {
    return this.name;
  }
  
  public List<Group> getGroups()
  {
    return this.groups;
  }
  
  public void addGroup(String name, LoggerLevel[] loggerLevels)
  {
    this.groups.add(new Group(name, loggerLevels));
  }
  
  public static class Group
  {
    private String name;
    private List<LogCategory.LoggerLevel> loggerLevels = new ArrayList();
    private List<LogCategory.LoggerLevel> previousLevels = new ArrayList();
    private boolean enabled;
    
    public Group(String name)
    {
      this.name = name;
    }
    
    public Group(String name, LogCategory.LoggerLevel[] loggerLevels)
    {
      this.name = name;
      this.loggerLevels = Arrays.asList(loggerLevels);
    }
    
    public String getName()
    {
      return this.name;
    }
    
    public List<LogCategory.LoggerLevel> getLoggerLevels()
    {
      return this.loggerLevels;
    }
    
    public boolean isEnabled()
    {
      return this.enabled;
    }
    
    public void setEnabled(boolean enabled)
    {
      this.enabled = enabled;
    }
    
    public List<LogCategory.LoggerLevel> getPreviousLevels()
    {
      return this.previousLevels;
    }
    
    public void setPreviousLevels(List<LogCategory.LoggerLevel> previousLevels)
    {
      this.previousLevels = previousLevels;
    }
  }
  
  public static class LoggerLevel
  {
    private String logger;
    private Level level;
    
    public LoggerLevel(String logger, Level level)
    {
      this.logger = logger;
      this.level = level;
    }
    
    public String getLogger()
    {
      return this.logger;
    }
    
    public Level getLevel()
    {
      return this.level;
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\seamless\swing\logging\LogCategory.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */