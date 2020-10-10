package com.wurmonline.website;

import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.ServerEntry;
import com.wurmonline.server.Servers;
import com.wurmonline.server.WurmCalendar;
import com.wurmonline.server.weather.Weather;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class StatsXMLWriter
{
  public static final void createXML(File outputFile)
    throws Exception
  {
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    
    Document doc = docBuilder.newDocument();
    Element rootElement = doc.createElement("statistics");
    doc.appendChild(rootElement);
    
    Element timestamp = doc.createElement("timestamp");
    timestamp.setTextContent(System.currentTimeMillis() / 1000L);
    rootElement.appendChild(timestamp);
    
    Element status = doc.createElement("status");
    status.setTextContent(Server.getMillisToShutDown() > 0L ? "shutting down" : Servers.localServer.maintaining ? "offline" : "online");
    if (Server.getMillisToShutDown() > 0L) {
      timestamp.setAttribute("ttl", new StringBuilder().append("").append(Server.getMillisToShutDown() / 1000L).toString());
    }
    rootElement.appendChild(status);
    
    Element uptime = doc.createElement("uptime");
    uptime.setTextContent(Server.getSecondsUptime());
    rootElement.appendChild(uptime);
    
    Element wurmtime = doc.createElement("wurmtime");
    wurmtime.setTextContent(WurmCalendar.getTime());
    rootElement.appendChild(wurmtime);
    
    Element weather = doc.createElement("weather");
    weather.setTextContent(Server.getWeather().getWeatherString(false));
    rootElement.appendChild(weather);
    
    Element serverselm = doc.createElement("servers");
    
    ServerEntry[] servers = Servers.getAllServers();
    
    int epic = 0;
    int epicMax = 0;
    for (ServerEntry entry : servers) {
      if (!entry.EPIC)
      {
        Element srv = doc.createElement("server");
        if (!entry.isLocal)
        {
          srv.setAttribute("name", entry.getName());
          srv.setAttribute("players", entry.currentPlayers);
          srv.setAttribute("maxplayers", entry.pLimit);
        }
        else
        {
          srv.setAttribute("name", entry.getName());
          srv.setAttribute("players", 
            Players.getInstance().getNumberOfPlayers());
          srv.setAttribute("maxplayers", entry.pLimit);
        }
        serverselm.appendChild(srv);
      }
      else
      {
        epic += entry.currentPlayers;
        epicMax += entry.pLimit;
      }
    }
    Element srv = doc.createElement("server");
    srv.setAttribute("name", "Epic cluster");
    srv.setAttribute("players", epic);
    srv.setAttribute("maxplayers", epicMax);
    serverselm.appendChild(srv);
    rootElement.appendChild(serverselm);
    
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    StreamResult result = new StreamResult(new BufferedWriter(new FileWriter(outputFile)));
    
    transformer.transform(source, result);
  }
}


/* Location:              C:\Games\SteamLibrary\steamapps\common\Wurm Unlimited Dedicated Server\server.jar!\target\classes\com\wurmonline\website\StatsXMLWriter.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       0.7.1
 */