package com.sun.xml.bind.v2.schemagen;

import com.sun.xml.bind.v2.schemagen.xmlschema.ContentModelContainer;
import com.sun.xml.bind.v2.schemagen.xmlschema.Occurs;
import com.sun.xml.bind.v2.schemagen.xmlschema.Particle;
import com.sun.xml.bind.v2.schemagen.xmlschema.TypeDefParticle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

abstract class Tree
{
  Tree makeOptional(boolean really)
  {
    return really ? new Optional(this, null) : this;
  }
  
  Tree makeRepeated(boolean really)
  {
    return really ? new Repeated(this, null) : this;
  }
  
  static Tree makeGroup(GroupKind kind, List<Tree> children)
  {
    if (children.size() == 1) {
      return (Tree)children.get(0);
    }
    List<Tree> normalizedChildren = new ArrayList(children.size());
    for (Tree t : children)
    {
      if ((t instanceof Group))
      {
        Group g = (Group)t;
        if (g.kind == kind)
        {
          normalizedChildren.addAll(Arrays.asList(g.children));
          continue;
        }
      }
      normalizedChildren.add(t);
    }
    return new Group(kind, (Tree[])normalizedChildren.toArray(new Tree[normalizedChildren.size()]), null);
  }
  
  abstract boolean isNullable();
  
  boolean canBeTopLevel()
  {
    return false;
  }
  
  protected abstract void write(ContentModelContainer paramContentModelContainer, boolean paramBoolean1, boolean paramBoolean2);
  
  protected void write(TypeDefParticle ct)
  {
    if (canBeTopLevel()) {
      write((ContentModelContainer)ct._cast(ContentModelContainer.class), false, false);
    } else {
      new Group(GroupKind.SEQUENCE, new Tree[] { this }, null).write(ct);
    }
  }
  
  protected final void writeOccurs(Occurs o, boolean isOptional, boolean repeated)
  {
    if (isOptional) {
      o.minOccurs(0);
    }
    if (repeated) {
      o.maxOccurs("unbounded");
    }
  }
  
  static abstract class Term
    extends Tree
  {
    boolean isNullable()
    {
      return false;
    }
  }
  
  private static final class Optional
    extends Tree
  {
    private final Tree body;
    
    private Optional(Tree body)
    {
      this.body = body;
    }
    
    boolean isNullable()
    {
      return true;
    }
    
    Tree makeOptional(boolean really)
    {
      return this;
    }
    
    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
    {
      this.body.write(parent, true, repeated);
    }
  }
  
  private static final class Repeated
    extends Tree
  {
    private final Tree body;
    
    private Repeated(Tree body)
    {
      this.body = body;
    }
    
    boolean isNullable()
    {
      return this.body.isNullable();
    }
    
    Tree makeRepeated(boolean really)
    {
      return this;
    }
    
    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
    {
      this.body.write(parent, isOptional, true);
    }
  }
  
  private static final class Group
    extends Tree
  {
    private final GroupKind kind;
    private final Tree[] children;
    
    private Group(GroupKind kind, Tree... children)
    {
      this.kind = kind;
      this.children = children;
    }
    
    boolean canBeTopLevel()
    {
      return true;
    }
    
    boolean isNullable()
    {
      if (this.kind == GroupKind.CHOICE)
      {
        for (Tree t : this.children) {
          if (t.isNullable()) {
            return true;
          }
        }
        return false;
      }
      for (Tree t : this.children) {
        if (!t.isNullable()) {
          return false;
        }
      }
      return true;
    }
    
    protected void write(ContentModelContainer parent, boolean isOptional, boolean repeated)
    {
      Particle c = this.kind.write(parent);
      writeOccurs(c, isOptional, repeated);
      for (Tree child : this.children) {
        child.write(c, false, false);
      }
    }
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\sun\xml\bind\v2\schemagen\Tree.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       0.7.1
 */