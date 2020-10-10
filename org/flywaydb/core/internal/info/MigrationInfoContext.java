package org.flywaydb.core.internal.info;

import java.util.HashMap;
import java.util.Map;
import org.flywaydb.core.api.MigrationVersion;

public class MigrationInfoContext
{
  public boolean outOfOrder;
  public boolean pending;
  public boolean future;
  public MigrationVersion target;
  public MigrationVersion schema;
  public MigrationVersion baseline;
  public MigrationVersion lastResolved = MigrationVersion.EMPTY;
  public MigrationVersion lastApplied = MigrationVersion.EMPTY;
  public Map<String, Integer> latestRepeatableRuns = new HashMap();
  
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    MigrationInfoContext that = (MigrationInfoContext)o;
    if (this.outOfOrder != that.outOfOrder) {
      return false;
    }
    if (this.pending != that.pending) {
      return false;
    }
    if (this.future != that.future) {
      return false;
    }
    if (this.target != null ? !this.target.equals(that.target) : that.target != null) {
      return false;
    }
    if (this.schema != null ? !this.schema.equals(that.schema) : that.schema != null) {
      return false;
    }
    if (this.baseline != null ? !this.baseline.equals(that.baseline) : that.baseline != null) {
      return false;
    }
    if (this.lastResolved != null ? !this.lastResolved.equals(that.lastResolved) : that.lastResolved != null) {
      return false;
    }
    if (this.lastApplied != null ? !this.lastApplied.equals(that.lastApplied) : that.lastApplied != null) {
      return false;
    }
    return this.latestRepeatableRuns.equals(that.latestRepeatableRuns);
  }
  
  public int hashCode()
  {
    int result = this.outOfOrder ? 1 : 0;
    result = 31 * result + (this.pending ? 1 : 0);
    result = 31 * result + (this.future ? 1 : 0);
    result = 31 * result + (this.target != null ? this.target.hashCode() : 0);
    result = 31 * result + (this.schema != null ? this.schema.hashCode() : 0);
    result = 31 * result + (this.baseline != null ? this.baseline.hashCode() : 0);
    result = 31 * result + (this.lastResolved != null ? this.lastResolved.hashCode() : 0);
    result = 31 * result + (this.lastApplied != null ? this.lastApplied.hashCode() : 0);
    result = 31 * result + this.latestRepeatableRuns.hashCode();
    return result;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\org\flywaydb\core\internal\info\MigrationInfoContext.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */