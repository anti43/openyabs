
package mpv5.ui.dialogs;

import com.michaelbaranov.microba.calendar.CalendarPane;
import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import mpv5.ui.misc.Position;
import mpv5.utils.date.DateConverter;

/**
 *
 * @author  anti43
 */
public class MiniCalendar extends javax.swing.JFrame {
    private static final long serialVersionUID = 1L;

    private CalendarPane xc;
    private JTextField t;

    /**
     * 
     * @param t
     * @param visible
     */
    public MiniCalendar(JTextField t, boolean visible) {
        try {
            initComponents();

            xc = new com.michaelbaranov.microba.calendar.CalendarPane();
            xc.setShowTodayButton(true);
            xc.setDate(new Date());
            xc.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jButton1ActionPerformed(evt);
                }
            });

            this.t = t;
            this.jPanel1.add(xc, BorderLayout.CENTER);
            new Position(this);
            this.setVisible(visible);
            this.setAlwaysOnTop(visible);
        } catch (PropertyVetoException ex) {
            Logger.getLogger(MiniCalendar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Date getDate() {
        return xc.getDate();
    }

    public void setDate(Date date) {
        try {
            xc.setDate(date);
            go();
        } catch (PropertyVetoException ex) {
            Logger.getLogger(MiniCalendar.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void go() {
        DateFormat format = DateConverter.DEF_DATE_FORMAT;
        if (xc.getDate() != null) {
            t.setText(format.format(xc.getDate()));
            this.setVisible(false);
        } else {
            this.setVisible(false);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Datum wählen");
        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setLocationByPlatform(true);

        jPanel2.setBackground(new java.awt.Color(227, 219, 202));
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        jButton1.setText("Ok");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jButton1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButton1KeyPressed(evt);
            }
        });

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanel1KeyPressed(evt);
            }
        });
        jPanel1.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 247, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 201, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void jButton1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        go();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton1KeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButton1KeyPressed
        go();
    }//GEN-LAST:event_jButton1KeyPressed

    private void jPanel1KeyPressed (java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanel1KeyPressed
        this.dispose();
    }//GEN-LAST:event_jPanel1KeyPressed

    private void jButton1MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        go();
    }//GEN-LAST:event_jButton1MouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JButton jButton1;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables
}
