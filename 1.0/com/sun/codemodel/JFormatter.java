package com.sun.codemodel;

import java.io.PrintWriter;

public class JFormatter
{
  private int indentLevel;
  private String indentSpace;
  private PrintWriter pw;
  
  public JFormatter(PrintWriter s, String space)
  {
    this.pw = s;
    this.indentSpace = space;
  }
  
  public JFormatter(PrintWriter s)
  {
    this(s, "    ");
  }
  
  public void close()
  {
    this.pw.close();
  }
  
  public JFormatter o()
  {
    this.indentLevel -= 1;
    return this;
  }
  
  public JFormatter i()
  {
    this.indentLevel += 1;
    return this;
  }
  
  private boolean needSpace(char c1, char c2)
  {
    if ((c1 == ']') && (c2 == '{')) {
      return true;
    }
    if (c1 == ';') {
      return true;
    }
    if ((c1 == ')') && (c2 == '{')) {
      return true;
    }
    if ((c1 == ',') || (c1 == '=')) {
      return true;
    }
    if (c2 == '=') {
      return true;
    }
    if (Character.isDigit(c1))
    {
      if ((c2 == '(') || (c2 == ')') || (c2 == ';') || (c2 == ',')) {
        return false;
      }
      return true;
    }
    if (Character.isJavaIdentifierPart(c1))
    {
      switch (c2)
      {
      case '+': 
      case '>': 
      case '{': 
      case '}': 
        return true;
      }
      return Character.isJavaIdentifierStart(c2);
    }
    if (Character.isJavaIdentifierStart(c2))
    {
      switch (c1)
      {
      case ')': 
      case '+': 
      case ']': 
      case '}': 
        return true;
      }
      return false;
    }
    if (Character.isDigit(c2))
    {
      if (c1 == '(') {
        return false;
      }
      return true;
    }
    return false;
  }
  
  private char lastChar = '\000';
  private boolean atBeginningOfLine = true;
  
  private void spaceIfNeeded(char c)
  {
    if (this.atBeginningOfLine)
    {
      for (int i = 0; i < this.indentLevel; i++) {
        this.pw.print(this.indentSpace);
      }
      this.atBeginningOfLine = false;
    }
    else if ((this.lastChar != 0) && (needSpace(this.lastChar, c)))
    {
      this.pw.print(' ');
    }
  }
  
  public JFormatter p(char c)
  {
    spaceIfNeeded(c);
    this.pw.print(c);
    this.lastChar = c;
    return this;
  }
  
  public JFormatter p(String s)
  {
    spaceIfNeeded(s.charAt(0));
    this.pw.print(s);
    this.lastChar = s.charAt(s.length() - 1);
    return this;
  }
  
  public JFormatter nl()
  {
    this.pw.println();
    this.lastChar = '\000';
    this.atBeginningOfLine = true;
    return this;
  }
  
  public JFormatter g(JGenerable g)
  {
    g.generate(this);
    return this;
  }
  
  public JFormatter d(JDeclaration d)
  {
    d.declare(this);
    return this;
  }
  
  public JFormatter s(JStatement s)
  {
    s.state(this);
    return this;
  }
  
  public JFormatter b(JVar v)
  {
    v.bind(this);
    return this;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\1.0\com\sun\codemodel\JFormatter.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */