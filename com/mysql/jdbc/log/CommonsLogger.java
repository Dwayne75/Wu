package com.mysql.jdbc.log;

import org.apache.commons.logging.LogFactory;

public class CommonsLogger
  implements Log
{
  private org.apache.commons.logging.Log logger;
  
  public CommonsLogger(String instanceName)
  {
    this.logger = LogFactory.getLog(instanceName);
  }
  
  public boolean isDebugEnabled()
  {
    return this.logger.isInfoEnabled();
  }
  
  public boolean isErrorEnabled()
  {
    return this.logger.isErrorEnabled();
  }
  
  public boolean isFatalEnabled()
  {
    return this.logger.isFatalEnabled();
  }
  
  public boolean isInfoEnabled()
  {
    return this.logger.isInfoEnabled();
  }
  
  public boolean isTraceEnabled()
  {
    return this.logger.isTraceEnabled();
  }
  
  public boolean isWarnEnabled()
  {
    return this.logger.isWarnEnabled();
  }
  
  public void logDebug(Object msg)
  {
    this.logger.debug(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logDebug(Object msg, Throwable thrown)
  {
    this.logger.debug(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
  
  public void logError(Object msg)
  {
    this.logger.error(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logError(Object msg, Throwable thrown)
  {
    this.logger.fatal(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
  
  public void logFatal(Object msg)
  {
    this.logger.fatal(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logFatal(Object msg, Throwable thrown)
  {
    this.logger.fatal(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
  
  public void logInfo(Object msg)
  {
    this.logger.info(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logInfo(Object msg, Throwable thrown)
  {
    this.logger.info(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
  
  public void logTrace(Object msg)
  {
    this.logger.trace(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logTrace(Object msg, Throwable thrown)
  {
    this.logger.trace(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
  
  public void logWarn(Object msg)
  {
    this.logger.warn(LogUtils.expandProfilerEventIfNecessary(msg));
  }
  
  public void logWarn(Object msg, Throwable thrown)
  {
    this.logger.warn(LogUtils.expandProfilerEventIfNecessary(msg), thrown);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\mysql\jdbc\log\CommonsLogger.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */