/* This code is copyright Articulate Software (c) 2016.  Some
portions copyright Teknowledge (c) 2003 and reused under the terms of
the GNU license.  This software is released under the GNU Public
License <http://www.gnu.org/copyleft/gpl.html>.  Users of this code
also consent, by use of this code, to credit Articulate Software and
Teknowledge in any writings, briefings, publications, presentations,
or other representations of any software which incorporates, builds
on, or uses this code.
 */
package com.articulate.calendar.gui;

import com.articulate.calendar.CalendarKB;
import com.articulate.calendar.CalendarPreferences;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JPanel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilCalendarModel;

/**
 *
 * @author Jeff Thompson, jeff@thefirst.org
 */
public class NewEventDialog extends JDialog {

  /**
   * Creates new form NewEventDialog
   */
  public NewEventDialog
    (java.awt.Frame parent, CalendarKB calendarKB, CalendarPreferences preferences)
  {
    super(parent, true);
    calendarKB_ = calendarKB;
    preferences_ = preferences;

    initComponents();

    startDatePicker_ = makeDatePicker(startDatePanel_);
    endDatePicker_ = makeDatePicker(endDatePanel_);

    // Set up the airport IATA code combo boxes.
    List<String> airports = new ArrayList<>();
    airports.add("");
    for (String airport : calendarKB_.iataAbbreviation_.keySet())
      airports.add(airport);
    Object[] airportsArray = airports.toArray();
    Arrays.sort(airportsArray);
    fromAirportComboBox_.setModel(new DefaultComboBoxModel(airportsArray));
    toAirportComboBox_.setModel(new DefaultComboBoxModel(airportsArray));

    Locale locale = Locale.getDefault();
    Calendar calendar = Calendar.getInstance(preferences_.getTimeZone(), locale);
    calendar.clear();
    // Calendar object: months start at 0.
    calendar.set(2016, 10 - 1, 14, 12, 40);
    long utcMillis = calendar.getTimeInMillis();
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {

    jPanel1 = new javax.swing.JPanel();
    fromAirportComboBox_ = new javax.swing.JComboBox<>();
    fromLabel_ = new javax.swing.JLabel();
    toAirportComboBox_ = new javax.swing.JComboBox<>();
    toLabel_ = new javax.swing.JLabel();
    startDatePanel_ = new javax.swing.JPanel();
    fromLabel_1 = new javax.swing.JLabel();
    okButton_ = new javax.swing.JButton();
    cancelButton_ = new javax.swing.JButton();
    fromLabel_2 = new javax.swing.JLabel();
    endDatePanel_ = new javax.swing.JPanel();
    startTimePanel_ = new javax.swing.JPanel();
    startTimeText_ = new javax.swing.JTextField();
    endTimePanel_ = new javax.swing.JPanel();
    endTimeText_ = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    eventLabelText_ = new javax.swing.JTextField();

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 100, Short.MAX_VALUE)
    );

    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

    fromLabel_.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    fromLabel_.setText("From:");

    toLabel_.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    toLabel_.setText("To:");

    startDatePanel_.setPreferredSize(new java.awt.Dimension(188, 30));

    javax.swing.GroupLayout startDatePanel_Layout = new javax.swing.GroupLayout(startDatePanel_);
    startDatePanel_.setLayout(startDatePanel_Layout);
    startDatePanel_Layout.setHorizontalGroup(
      startDatePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 114, Short.MAX_VALUE)
    );
    startDatePanel_Layout.setVerticalGroup(
      startDatePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 0, Short.MAX_VALUE)
    );

    fromLabel_1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    fromLabel_1.setText("Start:");

    okButton_.setText("OK");
    okButton_.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        okButton_ActionPerformed(evt);
      }
    });

    cancelButton_.setText("Cancel");
    cancelButton_.addActionListener(new java.awt.event.ActionListener()
    {
      public void actionPerformed(java.awt.event.ActionEvent evt)
      {
        cancelButton_ActionPerformed(evt);
      }
    });

    fromLabel_2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    fromLabel_2.setText("End:");

    endDatePanel_.setPreferredSize(new java.awt.Dimension(188, 30));

    javax.swing.GroupLayout endDatePanel_Layout = new javax.swing.GroupLayout(endDatePanel_);
    endDatePanel_.setLayout(endDatePanel_Layout);
    endDatePanel_Layout.setHorizontalGroup(
      endDatePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 114, Short.MAX_VALUE)
    );
    endDatePanel_Layout.setVerticalGroup(
      endDatePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGap(0, 30, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout startTimePanel_Layout = new javax.swing.GroupLayout(startTimePanel_);
    startTimePanel_.setLayout(startTimePanel_Layout);
    startTimePanel_Layout.setHorizontalGroup(
      startTimePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(startTimeText_, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
    );
    startTimePanel_Layout.setVerticalGroup(
      startTimePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(startTimeText_, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
    );

    javax.swing.GroupLayout endTimePanel_Layout = new javax.swing.GroupLayout(endTimePanel_);
    endTimePanel_.setLayout(endTimePanel_Layout);
    endTimePanel_Layout.setHorizontalGroup(
      endTimePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(endTimeText_, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
    );
    endTimePanel_Layout.setVerticalGroup(
      endTimePanel_Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(endTimeText_, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
    );

    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    jLabel1.setText("Label:");

    eventLabelText_.setText("New Event");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(fromLabel_2, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(endDatePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addComponent(okButton_))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(startTimePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
              .addComponent(endTimePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(fromLabel_, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(toLabel_, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                  .addComponent(toAirportComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                  .addComponent(fromAirportComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
              .addComponent(cancelButton_)))
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
              .addComponent(jLabel1)
              .addComponent(fromLabel_1, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(startDatePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
              .addGroup(layout.createSequentialGroup()
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventLabelText_, javax.swing.GroupLayout.PREFERRED_SIZE, 393, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        .addContainerGap(27, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(19, 19, 19)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(eventLabelText_, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addGap(20, 20, 20)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(startTimePanel_, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
            .addComponent(fromAirportComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(fromLabel_))
          .addComponent(startDatePanel_, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
            .addComponent(fromLabel_1)
            .addGap(6, 6, 6)))
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(toAirportComboBox_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(toLabel_))
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(endDatePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                  .addComponent(fromLabel_2)
                  .addGap(6, 6, 6))))
            .addGap(35, 35, 35)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(okButton_)
              .addComponent(cancelButton_)))
          .addComponent(endTimePanel_, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void okButton_ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButton_ActionPerformed
  {//GEN-HEADEREND:event_okButton_ActionPerformed
    Calendar startCalendar = (Calendar)startDatePicker_.getModel().getValue();
  }//GEN-LAST:event_okButton_ActionPerformed

  private void cancelButton_ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButton_ActionPerformed
  {//GEN-HEADEREND:event_cancelButton_ActionPerformed
    setVisible(false);
    dispose();
  }//GEN-LAST:event_cancelButton_ActionPerformed

  /**
   * @param args the command line arguments
   */
  public static void main(String args[])
  {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
    /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(NewEventDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(NewEventDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(NewEventDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(NewEventDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the dialog */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run()
      {
        NewEventDialog dialog = new NewEventDialog
          (new javax.swing.JFrame(), null, null);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e)
          {
            System.exit(0);
          }
        });
        dialog.setVisible(true);
      }
    });
  }

  /**
   * Make a JDatePicker, adding it to the given panel.
   * @param panel The JPanel for the date picker.
   * @return The new JDatePicker.
   */
  JDatePicker
  makeDatePicker(JPanel panel)
  {
    Locale locale = Locale.getDefault();
    Calendar calendar = Calendar.getInstance(preferences_.getTimeZone(), locale);
    UtilCalendarModel model = new UtilCalendarModel(calendar);
    Properties p = new Properties();
    p.put("text.today", "Today");
    p.put("text.month", "Month");
    p.put("text.year", "Year");
    JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
    // TODO: Fix: The date panel appears far from the date picker.
    JDatePickerImpl datePicker = new JDatePickerImpl
      (datePanel, new DateLabelFormatter());
    datePicker.setLocation(0, 0);
    datePicker.setSize(panel.getSize().width, panel.getSize().width);
    panel.add(datePicker);

    return datePicker;
  }

  private static class DateLabelFormatter extends AbstractFormatter {
    private String datePattern = "yyyy-MM-dd";
    private SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

    @Override
    public Object stringToValue(String text) throws ParseException {
        return dateFormatter.parseObject(text);
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        if (value != null) {
            Calendar cal = (Calendar)value;
            return dateFormatter.format(cal.getTime());
        }

        return "";
    }
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton cancelButton_;
  private javax.swing.JPanel endDatePanel_;
  private javax.swing.JPanel endTimePanel_;
  private javax.swing.JTextField endTimeText_;
  private javax.swing.JTextField eventLabelText_;
  private javax.swing.JComboBox<String> fromAirportComboBox_;
  private javax.swing.JLabel fromLabel_;
  private javax.swing.JLabel fromLabel_1;
  private javax.swing.JLabel fromLabel_2;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JButton okButton_;
  private javax.swing.JPanel startDatePanel_;
  private javax.swing.JPanel startTimePanel_;
  private javax.swing.JTextField startTimeText_;
  private javax.swing.JComboBox<String> toAirportComboBox_;
  private javax.swing.JLabel toLabel_;
  // End of variables declaration//GEN-END:variables
  private final CalendarKB calendarKB_;
  private final CalendarPreferences preferences_;
  private JDatePicker startDatePicker_;
  private JDatePicker endDatePicker_;
}
