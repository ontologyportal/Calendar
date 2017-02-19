/* This code is copyright Articulate Software (c) 2016.  Some
portions copyright Teknowledge (c) 2003 and reused under the terms of
the GNU license.  This software is released under the GNU Public
License <http://www.gnu.org/copyleft/gpl.html>.  Users of this code
also consent, by use of this code, to credit Articulate Software and
Teknowledge in any writings, briefings, publications, presentations,
or other representations of any software which incorporates, builds
on, or uses this code.
*/

package com.articulate.calendar;

import com.articulate.calendar.WikidataJava.Item;
import com.articulate.calendar.argue.Argument;
import com.articulate.calendar.argue.ArgumentSet;
import com.articulate.calendar.gui.CalendarFrame;
import com.articulate.sigma.Formula;
import com.articulate.sigma.KBmanager;
import com.articulate.sigma.WordNet;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.File;
import java.time.LocalDate;
import java.util.HashSet;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

/**
 * CalendarApp has the main method which creates the main window and starts the
 * application.
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class CalendarApp {
  /**
   * This the main entry for the application.
   * @param args The command line arguments.
   */
  public static void main (String args[]) 
      throws FileNotFoundException, IOException 
  {
/*
    String dumpDir = "/home/jeff/temp/";
    ArrayList<String> messages = new ArrayList<>();
    boolean doDump = false;
    if (doDump) {
      WikidataJava.dumpFromJson("/home/jeff/temp/wikidata-20170213-all.json.gz", dumpDir, messages);
      for (String message : messages)
        System.out.println(message);
      if (true) return;
    }

    WikidataJava wikidata = new WikidataJava(dumpDir);
    WikidataJava.getStatistics(wikidata.items_, messages);

    try (FileWriter file = new FileWriter(new File(dumpDir, "locationIanaTimeZone.kif").getAbsolutePath());
         BufferedWriter writer = new BufferedWriter(file)) {
      Set<String> timeZoneMessages = new HashSet<>();
      Map<Integer, Integer> map = WikidataJava.getLocationIanaTimeZones
        (wikidata.items_, timeZoneMessages);
      System.out.println("Debug locationIanaTimeZone size " + map.size());
      for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
        writer.write
          ("(locationIanaTimeZone Q" + entry.getKey() + " Q" + entry.getValue() +
           ")");
        writer.newLine();
      }

      for (String message : timeZoneMessages)
        System.out.println(message);
    }

    for (String message : messages)
      System.out.println(message);

    Gson gson = new Gson();
    try (FileWriter file = new FileWriter(new File(dumpDir, "iataAbbreviation.kif").getAbsolutePath());
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<Integer, Item> entry : wikidata.items_.entrySet()) {
        Item item = entry.getValue();
        if (item.iataAirportCode_ != null && item.iataAirportCode_.length == 1) {
          writer.write
            ("(abbreviation " + gson.toJson(item.iataAirportCode_[0]) +
             " Q" + entry.getKey() + ")");
          writer.newLine();
        }
      }
    }

    if (true) return;
*/
    
    KBmanager.getMgr().initializeOnce();
    WordNet.initOnce();
    CalendarPreferences preferences = new CalendarPreferences();

    CalendarKB calendarKB = new CalendarKB(KBmanager.getMgr().getKB("SUMO"));
    // Pre-cache overlapsDate results now.
    calendarKB.overlapsDate(LocalDate.now(), preferences.getTimeZone());

    try (FileWriter file = new FileWriter("/home/jeff/temp/debugAllLocations.txt");
         BufferedWriter writer = new BufferedWriter(file)) {
      for (Map.Entry<String, String> entry : calendarKB.locationIanaTimeZone_.entrySet()) {
        String locationLabel = calendarKB.itemTermFormatEnglishLanguage_.get(entry.getKey());
        if (locationLabel == null || locationLabel.isEmpty())
          // No label for the location.
          continue;
        String timeZoneLabel = calendarKB.itemTermFormatEnglishLanguage_.get(entry.getValue());
        if (timeZoneLabel == null || timeZoneLabel.isEmpty())
          // No label for the time zone.
          continue;

        writer.write(timeZoneLabel + " " + locationLabel);
        writer.newLine();
      }
      for (Map.Entry<String, String> entry : calendarKB.iataAbbreviation_.entrySet()) {
        if (!calendarKB.locationIanaTimeZone_.containsKey(entry.getKey()))
          // No location for the airport.
          continue;
        String timeZoneLabel = calendarKB.itemTermFormatEnglishLanguage_.get(calendarKB.locationIanaTimeZone_.get(entry.getKey()));
        if (timeZoneLabel == null || timeZoneLabel.isEmpty())
          // No label for the time zone.
          continue;
        writer.write(timeZoneLabel + " " + entry.getValue());
        writer.newLine();
      }
    }

    ArgumentSet argumentSet = makeArgumentSet(calendarKB);

    try {
      CalendarFrame frame = new CalendarFrame
        (preferences, calendarKB, argumentSet);
      frame.pack();
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static ArgumentSet makeArgumentSet(CalendarKB calendarKB)
  {
    HashSet<Argument> arguments = new HashSet<>();
    // For debugging, just fill the argumentSet with known Process instances.
    for (String process : calendarKB.kb.kbCache.getInstancesForType("Process"))
      // For debugging, we don't need the rules.
      arguments.add
        (new Argument(new Formula("(instance " + process + " Process)")));

    return new ArgumentSet(arguments);
  }
}
