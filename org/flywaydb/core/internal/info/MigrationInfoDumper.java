package org.flywaydb.core.internal.info;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.util.DateUtils;
import org.flywaydb.core.internal.util.StringUtils;

public class MigrationInfoDumper
{
  private static final String VERSION_TITLE = "Version";
  private static final String DESCRIPTION_TITLE = "Description";
  
  public static String dumpToAsciiTable(MigrationInfo[] migrationInfos)
  {
    int versionWidth = "Version".length();
    int descriptionWidth = "Description".length();
    MigrationInfo migrationInfo;
    for (migrationInfo : migrationInfos)
    {
      versionWidth = Math.max(versionWidth, migrationInfo.getVersion() == null ? 0 : migrationInfo.getVersion().toString().length());
      descriptionWidth = Math.max(descriptionWidth, migrationInfo.getDescription().length());
    }
    String ruler = "+-" + StringUtils.trimOrPad("", versionWidth, '-') + "-+-" + StringUtils.trimOrPad("", descriptionWidth, '-') + "-+---------------------+---------+\n";
    
    StringBuilder table = new StringBuilder();
    table.append(ruler);
    table.append("| ").append(StringUtils.trimOrPad("Version", versionWidth, ' '))
      .append(" | ").append(StringUtils.trimOrPad("Description", descriptionWidth))
      .append(" | Installed on        | State   |\n");
    table.append(ruler);
    if (migrationInfos.length == 0)
    {
      table.append(StringUtils.trimOrPad("| No migrations found", ruler.length() - 2, ' ')).append("|\n");
    }
    else
    {
      MigrationInfo[] arrayOfMigrationInfo2 = migrationInfos;migrationInfo = arrayOfMigrationInfo2.length;
      for (MigrationInfo localMigrationInfo1 = 0; localMigrationInfo1 < migrationInfo; localMigrationInfo1++)
      {
        MigrationInfo migrationInfo = arrayOfMigrationInfo2[localMigrationInfo1];
        String versionStr = migrationInfo.getVersion() == null ? "" : migrationInfo.getVersion().toString();
        table.append("| ").append(StringUtils.trimOrPad(versionStr, versionWidth));
        table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getDescription(), descriptionWidth));
        table.append(" | ").append(StringUtils.trimOrPad(DateUtils.formatDateAsIsoString(migrationInfo.getInstalledOn()), 19));
        table.append(" | ").append(StringUtils.trimOrPad(migrationInfo.getState().getDisplayName(), 7));
        table.append(" |\n");
      }
    }
    table.append(ruler);
    return table.toString();
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\info\MigrationInfoDumper.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */