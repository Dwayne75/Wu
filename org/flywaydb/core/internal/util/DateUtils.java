package org.flywaydb.core.internal.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils
{
  public static String formatDateAsIsoString(Date date)
  {
    if (date == null) {
      return "";
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\util\DateUtils.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */