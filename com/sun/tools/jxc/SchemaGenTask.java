package com.sun.tools.jxc;

import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.tools.jxc.apt.SchemaGenerator;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Commandline.Argument;

public class SchemaGenTask
  extends AptBasedTask
{
  private final List schemas;
  private File episode;
  
  public SchemaGenTask()
  {
    this.schemas = new ArrayList();
  }
  
  protected void setupCommandlineSwitches(Commandline cmd)
  {
    cmd.createArgument().setValue("-nocompile");
  }
  
  protected String getCompilationMessage()
  {
    return "Generating schema from ";
  }
  
  protected String getFailedMessage()
  {
    return "schema generation failed";
  }
  
  public Schema createSchema()
  {
    Schema s = new Schema();
    this.schemas.add(s);
    return s;
  }
  
  public void setEpisode(File f)
  {
    this.episode = f;
  }
  
  protected AnnotationProcessorFactory createFactory()
  {
    Map m = new HashMap();
    for (int i = 0; i < this.schemas.size(); i++)
    {
      Schema schema = (Schema)this.schemas.get(i);
      if (m.containsKey(schema.namespace)) {
        throw new BuildException("the same namespace is specified twice");
      }
      m.put(schema.namespace, schema.file);
    }
    SchemaGenerator r = new SchemaGenerator(m);
    if (this.episode != null) {
      r.setEpisodeFile(this.episode);
    }
    return r;
  }
  
  public class Schema
  {
    private String namespace;
    private File file;
    
    public Schema() {}
    
    public void setNamespace(String namespace)
    {
      this.namespace = namespace;
    }
    
    public void setFile(String fileName)
    {
      File dest = SchemaGenTask.this.getDestdir();
      if (dest == null) {
        dest = SchemaGenTask.this.getProject().getBaseDir();
      }
      this.file = new File(dest, fileName);
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\tools\jxc\SchemaGenTask.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */