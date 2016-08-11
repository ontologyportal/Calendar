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

import com.articulate.calendar.argue.Argument;
import com.articulate.calendar.argue.ArgumentSet;
import com.articulate.calendar.gui.CalendarFrame;
import com.articulate.sigma.Formula;
import com.articulate.sigma.KB;
import com.articulate.sigma.KBmanager;
import com.articulate.sigma.WordNet;
import java.util.HashSet;

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
  public static void main (String args[]) {
    KBmanager.getMgr().initializeOnce();
    WordNet.initOnce();
    KB kb = KBmanager.getMgr().getKB("SUMO");

    System.out.println("WhenFn");
    for (Formula formula : kb.ask("arg", 1, "(WhenFn Trip_Jefft0_20160601_094000)")) {
      System.out.println(formula.getArgument(1).equals("(WhenFn Trip_Jefft0_20160601_094000)"));
    }
    //if (true) return; // debug
    ArgumentSet argumentSet = makeArgumentSet(kb);

    try {
      CalendarFrame frame = new CalendarFrame
        (new CalendarPreferences(), kb, argumentSet);
      frame.pack();
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  private static ArgumentSet makeArgumentSet(KB kb)
  {
    HashSet<Argument> arguments = new HashSet<>();
    // For debugging, just fill the argumentSet with known Process instances.
    for (String process : kb.kbCache.getInstancesForType("Process"))
    {
      HashSet<String> premises = new HashSet<>();
      premises.add(process);
      // For debugging, we don't need the conclusion.
      arguments.add(new Argument(null, premises));
    }

    return new ArgumentSet(arguments);
  }
}
