package com.wurmonline.mesh;

public final class CaveMesh
  extends Mesh
{
  public CaveMesh(Mesh mesh, int width, int height)
  {
    super(width, height, mesh.getMeshWidth());
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++)
      {
        this.nodes[x][y] = new CaveNode();
        this.nodes[x][y].setTexture(CaveTile.TILE_ROCK.id);
        ((CaveNode)this.nodes[x][y]).setCeilingTexture(CaveTile.TILE_ROCK.id);
        
        this.nodes[x][y].setHeight((float)(Math.random() * Math.random() * Math.random()) * 100.0F + 0.2F);
        this.nodes[x][y].setNormals(new float[3]);
      }
    }
    processData();
    calculateNormals();
  }
  
  public CaveNode getCaveNode(int x, int y)
  {
    return (CaveNode)getNode(x, y);
  }
  
  public CaveMesh(int width, int height, int meshWidth)
  {
    super(width, height, meshWidth);
    setWraparound();
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < width; y++)
      {
        this.nodes[x][y] = new CaveNode();
        this.nodes[x][y].setTexture(CaveTile.TILE_ROCK.id);
        getCaveNode(x, y).setCeilingTexture(CaveTile.TILE_ROCK.id);
        getCaveNode(x, y).setHeight(Float.MAX_VALUE);
        getCaveNode(x, y).setData(0.0F);
        getCaveNode(x, y).setSpecial(1);
      }
    }
    processData();
    calculateNormals();
  }
  
  public boolean isTransition(float xp, float yp)
  {
    while (xp < 0.0F) {
      xp += getWidth() * getMeshWidth();
    }
    while (yp < 0.0F) {
      yp += getHeight() * getMeshWidth();
    }
    int xx = (int)(xp / getMeshWidth());
    int yy = (int)(yp / getMeshWidth());
    
    return getCaveNode(xx, yy).getSpecial() != 0;
  }
  
  public void processData(int x1, int y1, int x2, int y2)
  {
    for (int x = x1; x < x2; x++) {
      for (int y = y1; y < y2; y++)
      {
        Node node = getNode(x, y);
        float b = node.getHeight();
        float t = node.getHeight() + getCaveNode(x, y).getData();
        if (getNode(x + 1, y).getHeight() < b) {
          b = getNode(x + 1, y).getHeight();
        }
        if (getNode(x + 1, y).getHeight() + getCaveNode(x + 1, y).getData() > t) {
          t = getNode(x + 1, y).getHeight() + getCaveNode(x + 1, y).getData();
        }
        if (getNode(x + 1, y + 1).getHeight() < b) {
          b = getNode(x + 1, y + 1).getHeight();
        }
        if (getNode(x + 1, y + 1).getHeight() + getCaveNode(x + 1, y + 1).getData() > t) {
          t = getNode(x + 1, y + 1).getHeight() + getCaveNode(x + 1, y + 1).getData();
        }
        if (getNode(x, y + 1).getHeight() < b) {
          b = getNode(x, y + 1).getHeight();
        }
        if (getNode(x, y + 1).getHeight() + getCaveNode(x, y + 1).getData() > t) {
          t = getNode(x, y + 1).getHeight() + getCaveNode(x, y + 1).getData();
        }
        float h = t - b;
        
        node.setBbBottom(b);
        node.setBbHeight(h);
      }
    }
  }
  
  public void calculateNormals(int x1, int y1, int x2, int y2)
  {
    for (int x = x1; x < x2; x++) {
      for (int y = y1; y < y2; y++)
      {
        CaveNode n1 = getCaveNode(x, y);
        
        float v1x = getMeshWidth();
        float v1y = getNode(x + 1, y).getHeight() - n1.getHeight();
        float v1z = 0.0F;
        
        float v2x = 0.0F;
        float v2y = getNode(x, y + 1).getHeight() - n1.getHeight();
        float v2z = getMeshWidth();
        
        float vx = v1y * v2z - v1z * v2y;
        float vy = v1z * v2x - v1x * v2z;
        float vz = v1x * v2y - v1y * v2x;
        
        v1x = -getMeshWidth();
        v1y = getNode(x - 1, y).getHeight() - n1.getHeight();
        v1z = 0.0F;
        
        v2x = 0.0F;
        v2y = getNode(x, y - 1).getHeight() - n1.getHeight();
        v2z = -getMeshWidth();
        
        vx += v1y * v2z - v1z * v2y;
        vy += v1z * v2x - v1x * v2z;
        vz += v1x * v2y - v1y * v2x;
        
        float dist = (float)Math.sqrt(vx * vx + vy * vy + vz * vz);
        vx /= dist;
        vy /= dist;
        vz /= dist;
        
        n1.normals[0] = (-vx);
        n1.normals[1] = (-vy);
        n1.normals[2] = (-vz);
      }
    }
  }
  
  public boolean setTiles(int xStart, int yStart, int w, int h, int[][] tiles)
  {
    for (int x = 0; x < w; x++) {
      for (int y = 0; y < h; y++)
      {
        CaveNode node = getCaveNode(x + xStart, y + yStart);
        node.setHeight(CaveTile.decodeHeightAsFloat(tiles[x][y]));
        node.setTexture(CaveTile.decodeFloorTexture(tiles[x][y]));
        
        node.setCeilingTexture(CaveTile.decodeCeilingTexture(tiles[x][y]));
        node.setData(CaveTile.decodeCeilingHeightAsFloat(tiles[x][y]));
        node.setSpecial(0);
        if (node.getData() < 0.0F)
        {
          node.setData(-node.getData());
          node.setSpecial(1);
        }
      }
    }
    return false;
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\com\wurmonline\mesh\CaveMesh.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */