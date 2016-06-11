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

import com.articulate.calendar.gui.CalendarFrame;

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
    try {
      CalendarFrame frame = new CalendarFrame();
      frame.pack();
      frame.setVisible(true);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
