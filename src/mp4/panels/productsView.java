/*
 * customers.java
 *
 * Created on 28. Dezember 2007, 19:17
 */
package mp4.panels;

import mp3.classes.visual.sub.*;
import mp3.classes.layer.People;
import mp4.klassen.objekte.Product;
import mp4.klassen.objekte.Lieferant;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import mp3.classes.interfaces.panelInterface;
import mp3.classes.utils.Formater;
import mp3.classes.utils.Log;
import mp3.classes.layer.Popup;
import mp3.classes.layer.visual.ProductPicker;
import mp4.datenbank.verbindung.ConnectionHandler;
import mp3.classes.layer.visual.SupplierPicker;
import mp4.benutzerverwaltung.User;
import mp4.frames.mainframe;
import mp4.klassen.objekte.Angebot;
import mp4.klassen.objekte.Hersteller;
import mp4.klassen.objekte.Rechnung;
import mp4.panels.rechnungen.billsView;
import mp4.panels.rechnungen.offersView;
import mp4.utils.datum.DateConverter;
import mp4.utils.files.DialogOpenFile;
import mp4.utils.tabellen.SelectionCheck;
import mp4.utils.tabellen.TableFormat;

/**
 *
 * @author  anti43
 */
public class productsView extends javax.swing.JPanel implements mp4.datenbank.struktur.Tabellen, panelInterface {

    private mainframe mainframe;
    private Product current;
    private Lieferant lieferant;
    private Hersteller hersteller;
    private String[][] liste;
    private boolean autoProductNumber = true;//settings?
    private Integer currentProductGroupId = 0;
    private boolean edited = false;
    private Image currentImage;

    /** Creates new form customers
     * @param frame 
     */
    public productsView(mainframe frame) {
        initComponents();
        this.current = new Product();
        this.lieferant = new Lieferant(1);
        this.hersteller = new Hersteller(1);
        this.mainframe = frame;
        updateListTable();

        jTextField19.setText(DateConverter.getDefDateString(new Date()));
        jTextField9.setText(DateConverter.getDefDateString(new Date()));

        jTextField20.setText("0.0");
        jTextField21.setText("0.0");
        jTextField8.setText("0.0");
        jTextField7.setText("0.0");

        jTextField20.setInputVerifier(Formater.getDoubleInputVerfier(jTextField20));
        jTextField21.setInputVerifier(Formater.getDoubleInputVerfier(jTextField21));
        jTextField8.setInputVerifier(Formater.getDoubleInputVerfier(jTextField8));
        jTextField7.setInputVerifier(Formater.getDoubleInputVerfier(jTextField7));

        jTextField19.setInputVerifier(Formater.getDateInputVerfier(jTextField19));
        jTextField9.setInputVerifier(Formater.getDateInputVerfier(jTextField9));
    }

    public Product getProduct() {
        return current;
    }

    /**
     * 
     * @param p 
     */
    public void setProduct(Product p) {
        this.current = p;
        explode();
    }

    Product getCurrent() {
        return current;
    }

    void setCurrentImage(Image coverImg) {
        this.currentImage = coverImg;
    }

    private void clear() {
        for (int i = 0; i < this.getComponents().length; i++) {
        }
    }

    private void deactivate() {

        if (current.getId() < 0) {
            if ((JOptionPane.showConfirmDialog(this, "Wirklich löschen?", "Sicher?", JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION) {
                current.destroy();
                current = new Product();
                lieferant = new Lieferant(1);
                hersteller = new Hersteller(1);
                getMainframe().setMessage("Produkt Nummer " + current.getNummer() + " gelöscht.");
            }

            updateListTable();

        } else {
            getMainframe().setMessage("Kein Produkt gewählt.");
        }
    }

    private void explode() {

        this.setSupplier(current.getSupplier());
        this.jTextField4.setText(current.getNummer());
        this.jTextField5.setText(current.getName());
        if (current.getHersteller() != null) {
            this.jTextField6.setText(current.getHersteller().getFirma());
        }
        if (current.getSupplier() != null) {
            this.jTextField10.setText(current.getSupplier().getFirma());
        }
        this.jTextField8.setText(current.getVK().toString());
        this.jTextField7.setText(current.getEK().toString());
        this.jTextField16.setText(current.getTAX().toString());
        this.jTextField9.setText(current.getDatum().toString());
        this.jTextField11.setText(current.getUrl());
        if (!current.getWarengruppenId().equals("0")) {
            this.getJTextField12().setText(current.getProductgroupPath());
        } else {
            this.getJTextField12().setText("");
        }
        this.jTextField13.setText(current.getEan());
        this.jEditorPane1.setText(current.getText());

        GetProductImage t = new GetProductImage(this);
        t.execute();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
private void initComponents() {

jPanel1 = new javax.swing.JPanel();
jToolBar2 = new javax.swing.JToolBar();
jButton11 = new javax.swing.JButton();
jButton20 = new javax.swing.JButton();
jButton3 = new javax.swing.JButton();
jButton4 = new javax.swing.JButton();
jSeparator2 = new javax.swing.JToolBar.Separator();
jButton9 = new javax.swing.JButton();
jButton10 = new javax.swing.JButton();
jSeparator3 = new javax.swing.JToolBar.Separator();
jButton18 = new javax.swing.JButton();
jTabbedPane1 = new javax.swing.JTabbedPane();
jPanel2 = new javax.swing.JPanel();
jPanel5 = new javax.swing.JPanel();
jScrollPane5 = new javax.swing.JScrollPane();
jTable3 = new javax.swing.JTable()

{
    public boolean isCellEditable(int x, int y) {
        return false;
    }
}
;
jPanel4 = new javax.swing.JPanel();
jLabel2 = new javax.swing.JLabel();
jTextField1 = new javax.swing.JTextField();
jTextField2 = new javax.swing.JTextField();
jLabel3 = new javax.swing.JLabel();
jTextField3 = new javax.swing.JTextField();
jLabel4 = new javax.swing.JLabel();
jLabel19 = new javax.swing.JLabel();
jTextField14 = new javax.swing.JTextField();
jPanel6 = new javax.swing.JPanel();
jPanel7 = new javax.swing.JPanel();
jLabel5 = new javax.swing.JLabel();
jTextField4 = new javax.swing.JTextField();
jTextField5 = new javax.swing.JTextField();
jLabel6 = new javax.swing.JLabel();
jLabel7 = new javax.swing.JLabel();
jTextField6 = new javax.swing.JTextField();
jButton17 = new javax.swing.JButton();
jButton5 = new javax.swing.JButton();
jTextField10 = new javax.swing.JTextField();
jLabel11 = new javax.swing.JLabel();
jPanel8 = new javax.swing.JPanel();
jTextField7 = new javax.swing.JTextField();
jTextField9 = new javax.swing.JTextField();
jLabel18 = new javax.swing.JLabel();
jLabel9 = new javax.swing.JLabel();
jLabel17 = new javax.swing.JLabel();
jTextField8 = new javax.swing.JTextField();
jTextField16 = new javax.swing.JTextField();
jLabel8 = new javax.swing.JLabel();
jButton22 = new javax.swing.JButton();
jPanel9 = new javax.swing.JPanel();
jLabel15 = new javax.swing.JLabel();
jLabel10 = new javax.swing.JLabel();
jLabel16 = new javax.swing.JLabel();
jTextField13 = new javax.swing.JTextField();
jScrollPane1 = new javax.swing.JScrollPane();
jEditorPane1 = new javax.swing.JEditorPane();
jTextField12 = new javax.swing.JTextField();
jButton21 = new javax.swing.JButton();
jTextField22 = new javax.swing.JTextField();
jLabel25 = new javax.swing.JLabel();
jPanel10 = new javax.swing.JPanel();
jScrollPane2 = new javax.swing.JScrollPane();
jLabel13 = new javax.swing.JLabel();
jLabel14 = new javax.swing.JLabel();
jTextField11 = new javax.swing.JTextField();
jButton1 = new javax.swing.JButton();
jPanel11 = new javax.swing.JPanel();
jLabel1 = new javax.swing.JLabel();
jLabel12 = new javax.swing.JLabel();
jTextField15 = new javax.swing.JTextField();
jTextField17 = new javax.swing.JTextField();
jLabel20 = new javax.swing.JLabel();
jTextField18 = new javax.swing.JTextField();
jLabel21 = new javax.swing.JLabel();
jTextField19 = new javax.swing.JTextField();
jLabel22 = new javax.swing.JLabel();
jTextField20 = new javax.swing.JTextField();
jLabel23 = new javax.swing.JLabel();
jTextField21 = new javax.swing.JTextField();
jLabel24 = new javax.swing.JLabel();
jPanel3 = new javax.swing.JPanel();
jScrollPane4 = new javax.swing.JScrollPane();
jTable2 = new javax.swing.JTable()
 {
    public boolean isCellEditable(int x, int y) {
        return false;
    }
}



;
jButton6 = new javax.swing.JButton();

jPanel1.setBackground(new java.awt.Color(255, 255, 204));
jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jToolBar2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
jToolBar2.setFloatable(false);
jToolBar2.setRollover(true);

jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/undo.png"))); // NOI18N
jButton11.setToolTipText("Rueckgaengig");
jButton11.setFocusable(false);
jButton11.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton11.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton11.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton11ActionPerformed(evt);
}
});
jToolBar2.add(jButton11);

jButton20.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/tab_remove.png"))); // NOI18N
jButton20.setToolTipText("Kunde deaktivieren und Tab schliessen");
jButton20.setFocusable(false);
jButton20.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton20.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton20.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton20ActionPerformed(evt);
}
});
jToolBar2.add(jButton20);

jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/filesave.png"))); // NOI18N
jButton3.setToolTipText("Speichern");
jButton3.setFocusable(false);
jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton3MouseClicked(evt);
}
});
jButton3.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton3ActionPerformed(evt);
}
});
jToolBar2.add(jButton3);

jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/new_window.png"))); // NOI18N
jButton4.setToolTipText("Neu anlegen");
jButton4.setFocusable(false);
jButton4.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton4MouseClicked(evt);
}
});
jButton4.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton4ActionPerformed(evt);
}
});
jToolBar2.add(jButton4);
jToolBar2.add(jSeparator2);

jButton9.setText("Neue Rechnung");
jButton9.setFocusable(false);
jButton9.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton9.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton9.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton9ActionPerformed(evt);
}
});
jToolBar2.add(jButton9);

jButton10.setText("Neues Angebot");
jButton10.setFocusable(false);
jButton10.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton10.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton10.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton10ActionPerformed(evt);
}
});
jToolBar2.add(jButton10);
jToolBar2.add(jSeparator3);

jButton18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/medium/print_printer.png"))); // NOI18N
jButton18.setToolTipText("Drucken");
jButton18.setFocusable(false);
jButton18.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton18.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton18.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton18MouseClicked(evt);
}
});
jToolBar2.add(jButton18);

javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
jPanel1.setLayout(jPanel1Layout);
jPanel1Layout.setHorizontalGroup(
jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
);
jPanel1Layout.setVerticalGroup(
jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel1Layout.createSequentialGroup()
.addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addContainerGap())
);

jTabbedPane1.setBackground(new java.awt.Color(255, 255, 255));
jTabbedPane1.setOpaque(true);

jPanel2.setBackground(new java.awt.Color(255, 255, 255));

jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Datensätze"));

jScrollPane5.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

jTable3.setAutoCreateRowSorter(true);
jTable3.setModel(new javax.swing.table.DefaultTableModel(
	new Object [][] {
		{null, null}
	},
	new String [] {
		"Name", "Hersteller"
	}
) {
	boolean[] canEdit = new boolean [] {
		false, false
	};

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit [columnIndex];
	}
});
jTable3.setRequestFocusEnabled(false);
jTable3.setUpdateSelectionOnSort(false);
jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jTable3MouseClicked(evt);
}
});
jScrollPane5.setViewportView(jTable3);

javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
jPanel5.setLayout(jPanel5Layout);
jPanel5Layout.setHorizontalGroup(
jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 156, Short.MAX_VALUE)
);
jPanel5Layout.setVerticalGroup(
jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
);

jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Suche"));

jLabel2.setText("Nummer:");

jTextField1.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField1ActionPerformed(evt);
}
});

jTextField2.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField2ActionPerformed(evt);
}
});

jLabel3.setText("Hersteller:");

jTextField3.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField3ActionPerformed(evt);
}
});

jLabel4.setText("Lieferant:");

jLabel19.setText("EAN:");

jTextField14.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField14ActionPerformed(evt);
}
});

javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
jPanel4.setLayout(jPanel4Layout);
jPanel4Layout.setHorizontalGroup(
jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel4Layout.createSequentialGroup()
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel2)
.addComponent(jLabel3)
.addComponent(jLabel4)
.addComponent(jLabel19))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
.addComponent(jTextField14)
.addComponent(jTextField3)
.addComponent(jTextField2)
.addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE))
.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
);
jPanel4Layout.setVerticalGroup(
jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel4Layout.createSequentialGroup()
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel2)
.addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel3)
.addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel4)
.addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jTextField14, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jLabel19))
.addContainerGap(19, Short.MAX_VALUE))
);

jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Details"));

jPanel7.setBackground(new java.awt.Color(227, 219, 202));
jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jLabel5.setText("Nummer");

jTextField4.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField4ActionPerformed(evt);
}
});

jTextField5.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField5ActionPerformed(evt);
}
});

jLabel6.setText("Name");

jLabel7.setText("Hersteller");

jTextField6.setEditable(false);
jTextField6.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField6ActionPerformed(evt);
}
});

jButton17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/search.png"))); // NOI18N
jButton17.setFocusable(false);
jButton17.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton17.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton17.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton17MouseClicked(evt);
}
});

jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/search.png"))); // NOI18N
jButton5.setFocusable(false);
jButton5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton5.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton5MouseClicked(evt);
}
});

jTextField10.setEditable(false);

jLabel11.setText("Lieferant");

javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
jPanel7.setLayout(jPanel7Layout);
jPanel7Layout.setHorizontalGroup(
jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
.addContainerGap(17, Short.MAX_VALUE)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
.addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING))
.addGap(10, 10, 10)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jButton17, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
.addContainerGap())
);
jPanel7Layout.setVerticalGroup(
jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel5)
.addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addGap(7, 7, 7)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel6)
.addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel7Layout.createSequentialGroup()
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel7)
.addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel11)
.addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))))
.addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
.addContainerGap())
);

jPanel8.setBackground(new java.awt.Color(227, 219, 202));
jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jTextField7.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField7ActionPerformed(evt);
}
});

jLabel18.setText("Steuersatz");

jLabel9.setText("VK");

jLabel17.setText("Datum");

jTextField8.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField8ActionPerformed(evt);
}
});

jTextField16.setEditable(false);
jTextField16.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField16ActionPerformed(evt);
}
});

jLabel8.setText("EK");

jButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/search.png"))); // NOI18N
jButton22.setFocusable(false);
jButton22.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton22.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton22.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton22MouseClicked(evt);
}
});

javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
jPanel8.setLayout(jPanel8Layout);
jPanel8Layout.setHorizontalGroup(
jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel8Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel17)
.addComponent(jLabel9)
.addComponent(jLabel8)
.addComponent(jLabel18))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel8Layout.createSequentialGroup()
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jTextField7, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
.addComponent(jTextField8, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE)
.addComponent(jTextField16, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
.addComponent(jTextField9, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE))
.addContainerGap())
);
jPanel8Layout.setVerticalGroup(
jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel8Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addGroup(jPanel8Layout.createSequentialGroup()
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel9)
.addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel8)
.addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel18)
.addComponent(jTextField16, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
.addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel17)
.addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
);

jPanel9.setBackground(new java.awt.Color(227, 219, 202));
jPanel9.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jLabel15.setText("Warengruppe");

jLabel10.setText("Langtext");

jLabel16.setText("Info Url");

jTextField13.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField13ActionPerformed(evt);
}
});

jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

jEditorPane1.setFont(jEditorPane1.getFont());
jScrollPane1.setViewportView(jEditorPane1);

jTextField12.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField12ActionPerformed(evt);
}
});

jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/search.png"))); // NOI18N
jButton21.setFocusable(false);
jButton21.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
jButton21.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
jButton21.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jButton21MouseClicked(evt);
}
});

jTextField22.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jTextField22ActionPerformed(evt);
}
});

jLabel25.setText("EAN");

javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
jPanel9.setLayout(jPanel9Layout);
jPanel9Layout.setHorizontalGroup(
jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel9Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel25)
.addComponent(jLabel16)
.addComponent(jLabel10)
.addComponent(jLabel15))
.addGap(4, 4, 4)
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
.addComponent(jTextField12, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
.addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
.addComponent(jTextField22, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE)
.addComponent(jTextField13, javax.swing.GroupLayout.DEFAULT_SIZE, 129, Short.MAX_VALUE))
.addContainerGap())
);
jPanel9Layout.setVerticalGroup(
jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel9Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
.addComponent(jLabel15)
.addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
.addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addGap(6, 6, 6)
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel10)
.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel16)
.addComponent(jTextField13, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel25)
.addComponent(jTextField22, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
.addContainerGap(21, Short.MAX_VALUE))
);

jPanel10.setBackground(new java.awt.Color(227, 219, 202));
jPanel10.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jScrollPane2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

jLabel13.setBackground(new java.awt.Color(230, 230, 220));
jLabel13.setBorder(javax.swing.BorderFactory.createEtchedBorder());
jLabel13.setOpaque(true);
jScrollPane2.setViewportView(jLabel13);

jLabel14.setText("Bild");

jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/bilder/small/search.png"))); // NOI18N
jButton1.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton1ActionPerformed(evt);
}
});

javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
jPanel10.setLayout(jPanel10Layout);
jPanel10Layout.setHorizontalGroup(
jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
.addGroup(jPanel10Layout.createSequentialGroup()
.addComponent(jLabel14)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
.addComponent(jTextField11, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
.addContainerGap())
);
jPanel10Layout.setVerticalGroup(
jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
.addContainerGap()
.addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
.addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel14)
.addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addComponent(jButton1))
.addContainerGap())
);

jPanel11.setBackground(new java.awt.Color(227, 219, 202));
jPanel11.setBorder(javax.swing.BorderFactory.createEtchedBorder());

jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
jLabel1.setText("Referenzen");

jLabel12.setText("Bestellnr");

jLabel20.setText("Herst.Nr.");

jLabel21.setText("Liefer.Nr.");

jLabel22.setText("Best.Dat.");

jLabel23.setText("BstMenge");

jLabel24.setText("Lager");

javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
jPanel11.setLayout(jPanel11Layout);
jPanel11Layout.setHorizontalGroup(
jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel11Layout.createSequentialGroup()
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel11Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jLabel12)
.addComponent(jLabel20)
.addComponent(jLabel22)
.addComponent(jLabel21)
.addComponent(jLabel23)
.addComponent(jLabel24))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
.addComponent(jTextField15, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
.addComponent(jTextField17, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
.addComponent(jTextField18, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
.addComponent(jTextField19, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
.addComponent(jTextField20, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
.addComponent(jTextField21, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)))
.addGroup(jPanel11Layout.createSequentialGroup()
.addGap(10, 10, 10)
.addComponent(jLabel1)))
.addContainerGap())
);
jPanel11Layout.setVerticalGroup(
jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel11Layout.createSequentialGroup()
.addContainerGap()
.addComponent(jLabel1)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel12)
.addComponent(jTextField15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel20)
.addComponent(jTextField17, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel21)
.addComponent(jTextField18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel22)
.addComponent(jTextField19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel23)
.addComponent(jTextField20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addGap(18, 18, 18)
.addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
.addComponent(jLabel24)
.addComponent(jTextField21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addContainerGap(22, Short.MAX_VALUE))
);

javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
jPanel6.setLayout(jPanel6Layout);
jPanel6Layout.setHorizontalGroup(
jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel6Layout.createSequentialGroup()
.addContainerGap()
.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
.addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
.addContainerGap())
);
jPanel6Layout.setVerticalGroup(
jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel6Layout.createSequentialGroup()
.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
.addGroup(jPanel6Layout.createSequentialGroup()
.addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
);

javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
jPanel2.setLayout(jPanel2Layout);
jPanel2Layout.setHorizontalGroup(
jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel2Layout.createSequentialGroup()
.addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
.addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
.addGap(2, 2, 2)
.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
);
jPanel2Layout.setVerticalGroup(
jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(jPanel2Layout.createSequentialGroup()
.addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
.addGap(1, 1, 1)
.addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
.addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
);

jTabbedPane1.addTab("Allgemein", jPanel2);

jPanel3.setBackground(new java.awt.Color(255, 255, 255));

jScrollPane4.setAutoscrolls(true);

jTable2.setAutoCreateRowSorter(true);
jTable2.setModel(new javax.swing.table.DefaultTableModel(
	new Object [][] {
		{null, null, null, null},
		{null, null, null, null},
		{null, null, null, null},
		{null, null, null, null}
	},
	new String [] {
		"Nummer", "Name", "Hersteller", "Datum"
	}
) {
	boolean[] canEdit = new boolean [] {
		false, false, false, false
	};

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return canEdit [columnIndex];
	}
});
jTable2.addMouseListener(new java.awt.event.MouseAdapter() {
public void mouseClicked(java.awt.event.MouseEvent evt) {
jTable2MouseClicked(evt);
}
});
jScrollPane4.setViewportView(jTable2);

jButton6.setText("Markierte entfernen");
jButton6.addActionListener(new java.awt.event.ActionListener() {
public void actionPerformed(java.awt.event.ActionEvent evt) {
jButton6ActionPerformed(evt);
}
});

javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
jPanel3.setLayout(jPanel3Layout);
jPanel3Layout.setHorizontalGroup(
jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
.addContainerGap()
.addComponent(jButton6))
.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 642, Short.MAX_VALUE)
);
jPanel3Layout.setVerticalGroup(
jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
.addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jButton6))
);

jTabbedPane1.addTab("Liste", jPanel3);

javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
this.setLayout(layout);
layout.setHorizontalGroup(
layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addComponent(jPanel1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
.addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
);
layout.setVerticalGroup(
layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
.addGroup(layout.createSequentialGroup()
.addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
.addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE))
);
}// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

        String[][] list = current.select("id,produktnummer,name", "produktnummer", jTextField1.getText(), "produktnummer", true);
        String k = "id, " + "Nummer,Name";
        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        TableFormat.stripFirst(jTable3);
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
//        String[][] list = current.select("id,produktnummer,name", "hersteller", jTextField2.getText(), "hersteller", true);
//        String k = "id, " + "Nummer,Name";
//
//        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
//        TableFormat.stripFirst(jTable3);
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed

        String[][] list = current.select("id,ean,name", "ean", jTextField3.getText(), "ean", true, true);
        String k = "id, " + "EAN,Name";

        this.jTable3.setModel(new DefaultTableModel(list, k.split(",")));
        TableFormat.stripFirst(jTable3);
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jTextField6ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jTextField7ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField8ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jTextField16ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField16ActionPerformed
    }//GEN-LAST:event_jTextField16ActionPerformed

    private void jButton6ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed


        jTabbedPane1.setSelectedIndex(1);

        SelectionCheck selection = new SelectionCheck(jTable2);
        if (selection.checkID()) {
            if ((JOptionPane.showConfirmDialog(getMainframe(), "Wirklich löschen?", "Sicher?", JOptionPane.YES_NO_OPTION)) == JOptionPane.YES_OPTION) {
                for (int i = 0; i < jTable2.getSelectedRowCount(); i++) {
                    int[] ik = jTable2.getSelectedRows();
                    current.delete(selection.getId());
                }
            }
        } else {
            new Popup("Sie müssen mindestens einen Eintrag in der Liste wählen.", Popup.NOTICE);
        }

        liste = current.getAll();
        String k = "id, " + TABLE_PRODUCTS_LIST_COLUMNS;
        this.jTable2.setModel(new DefaultTableModel(liste, k.split(",")));
        TableFormat.stripFirst(jTable2);
        this.clear();
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jTable2MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable2MouseClicked
        jTabbedPane1.setSelectedIndex(1);

        SelectionCheck selection = new SelectionCheck(jTable2);

        if (evt.getClickCount() >= 2 && selection.checkID()) {
            this.setProduct(new Product(selection.getId()));
            jTabbedPane1.setSelectedIndex(0);
        }
    }//GEN-LAST:event_jTable2MouseClicked

    private void jTable3MouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable3MouseClicked

        SelectionCheck selection = new SelectionCheck(jTable3);

        if (evt.getClickCount() >= 2 && selection.checkID()) {
            this.setProduct(new Product(selection.getId()));
        }
    }//GEN-LAST:event_jTable3MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        save();
    }//GEN-LAST:event_jButton3MouseClicked

    private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
    }//GEN-LAST:event_jTextField12ActionPerformed

    private void jTextField13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField13ActionPerformed
    }//GEN-LAST:event_jTextField13ActionPerformed

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked

        createNew();
        updateListTable();
        ProductPicker.update();
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton5MouseClicked
    }//GEN-LAST:event_jButton5MouseClicked

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed

        if (current.getId() != 0) {
            getMainframe().addBillPanel(new Rechnung());
            ((billsView) getMainframe().getTabPane().getSelectedComponent()).addProductToBillsTable(current);
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        if (current.getId() != 0) {
            getMainframe().addAngebotPanel(new Angebot());
            ((offersView) getMainframe().getTabPane().getSelectedComponent()).addToOrder(current);
        }
    }//GEN-LAST:event_jButton10ActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
}//GEN-LAST:event_jButton3ActionPerformed

private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
    undo();
}//GEN-LAST:event_jButton11ActionPerformed

private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
    if (mainframe.getUser().doAction(User.EDITOR)) {
        deactivate();
        this.close();
    }
}//GEN-LAST:event_jButton20ActionPerformed

private void jButton18MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton18MouseClicked
}//GEN-LAST:event_jButton18MouseClicked

private void jTextField14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField14ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField14ActionPerformed

private void jButton17MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton17MouseClicked
// TODO add your handling code here:
}//GEN-LAST:event_jButton17MouseClicked

private void jButton21MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton21MouseClicked
    new groupsView(getMainframe(), current, jTextField12 ).setTreeData(true);
    setEdited(true);
}//GEN-LAST:event_jButton21MouseClicked

private void jButton22MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton22MouseClicked
// TODO add your handling code here:
}//GEN-LAST:event_jButton22MouseClicked

private void jTextField22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField22ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_jTextField22ActionPerformed

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    DialogOpenFile dialog = null;

    if (dialog == null) {
        dialog = new DialogOpenFile(DialogOpenFile.FILES_ONLY);
    }

    if (dialog.getFile(jTextField11)) {
        new GetAnyImage(this, jTextField11.getText()).execute();
    }
    setEdited(true);
}//GEN-LAST:event_jButton1ActionPerformed
// Variables declaration - do not modify//GEN-BEGIN:variables
public javax.swing.JButton jButton1;
public javax.swing.JButton jButton10;
public javax.swing.JButton jButton11;
public javax.swing.JButton jButton17;
public javax.swing.JButton jButton18;
public javax.swing.JButton jButton20;
public javax.swing.JButton jButton21;
public javax.swing.JButton jButton22;
public javax.swing.JButton jButton3;
public javax.swing.JButton jButton4;
public javax.swing.JButton jButton5;
public javax.swing.JButton jButton6;
public javax.swing.JButton jButton9;
public javax.swing.JEditorPane jEditorPane1;
public javax.swing.JLabel jLabel1;
public javax.swing.JLabel jLabel10;
public javax.swing.JLabel jLabel11;
public javax.swing.JLabel jLabel12;
public javax.swing.JLabel jLabel13;
public javax.swing.JLabel jLabel14;
public javax.swing.JLabel jLabel15;
public javax.swing.JLabel jLabel16;
public javax.swing.JLabel jLabel17;
public javax.swing.JLabel jLabel18;
public javax.swing.JLabel jLabel19;
public javax.swing.JLabel jLabel2;
public javax.swing.JLabel jLabel20;
public javax.swing.JLabel jLabel21;
public javax.swing.JLabel jLabel22;
public javax.swing.JLabel jLabel23;
public javax.swing.JLabel jLabel24;
public javax.swing.JLabel jLabel25;
public javax.swing.JLabel jLabel3;
public javax.swing.JLabel jLabel4;
public javax.swing.JLabel jLabel5;
public javax.swing.JLabel jLabel6;
public javax.swing.JLabel jLabel7;
public javax.swing.JLabel jLabel8;
public javax.swing.JLabel jLabel9;
public javax.swing.JPanel jPanel1;
public javax.swing.JPanel jPanel10;
public javax.swing.JPanel jPanel11;
public javax.swing.JPanel jPanel2;
public javax.swing.JPanel jPanel3;
public javax.swing.JPanel jPanel4;
public javax.swing.JPanel jPanel5;
public javax.swing.JPanel jPanel6;
public javax.swing.JPanel jPanel7;
public javax.swing.JPanel jPanel8;
public javax.swing.JPanel jPanel9;
public javax.swing.JScrollPane jScrollPane1;
public javax.swing.JScrollPane jScrollPane2;
public javax.swing.JScrollPane jScrollPane4;
public javax.swing.JScrollPane jScrollPane5;
public javax.swing.JToolBar.Separator jSeparator2;
public javax.swing.JToolBar.Separator jSeparator3;
public javax.swing.JTabbedPane jTabbedPane1;
public javax.swing.JTable jTable2;
public javax.swing.JTable jTable3;
public javax.swing.JTextField jTextField1;
public javax.swing.JTextField jTextField10;
public javax.swing.JTextField jTextField11;
public javax.swing.JTextField jTextField12;
public javax.swing.JTextField jTextField13;
public javax.swing.JTextField jTextField14;
public javax.swing.JTextField jTextField15;
public javax.swing.JTextField jTextField16;
public javax.swing.JTextField jTextField17;
public javax.swing.JTextField jTextField18;
public javax.swing.JTextField jTextField19;
public javax.swing.JTextField jTextField2;
public javax.swing.JTextField jTextField20;
public javax.swing.JTextField jTextField21;
public javax.swing.JTextField jTextField22;
public javax.swing.JTextField jTextField3;
public javax.swing.JTextField jTextField4;
public javax.swing.JTextField jTextField5;
public javax.swing.JTextField jTextField6;
public javax.swing.JTextField jTextField7;
public javax.swing.JTextField jTextField8;
public javax.swing.JTextField jTextField9;
public javax.swing.JToolBar jToolBar2;
// End of variables declaration//GEN-END:variables
    private void createNew() {

        current = new Product(ConnectionHandler.instanceOf());

        if (jTextField4.getText().equals("")) {
            Integer tz = current.getNextIndex("produktnummer");
            jTextField4.setText(tz.toString());
        }

        try {
            Double.valueOf(jTextField20.getText());
        } catch (NumberFormatException numberFormatException) {
            jTextField13.setText("0");
        }
        try {
            Double.valueOf(jTextField8.getText());
        } catch (NumberFormatException numberFormatException) {
            jTextField8.setText("0");
        }
        try {
            Double.valueOf(jTextField21.getText());
        } catch (NumberFormatException numberFormatException) {
            jTextField7.setText("0");
        }
//        try {
//            Double.valueOf(jTextField16.getText());
//        } catch (NumberFormatException numberFormatException) {
//            jTextField16.setText("0");
//        }

        current.setNummer(
                jTextField4.getText());
        current.setName(jTextField5.getText());
        current.setHersteller(hersteller);
        current.setSupplier(lieferant);
        current.setVK(Double.valueOf(jTextField8.getText()));
        current.setEK(Double.valueOf(jTextField7.getText()));
//        current.setTAX(Double.valueOf(jTextField16.getText()));
        current.setDatum(DateConverter.getDate(jTextField9.getText()));
        current.setUrl(jTextField11.getText());
        current.setEan(jTextField13.getText());
        current.setText(jEditorPane1.getText());
        current.setWarengruppenId(getCurrentProductGroup());
        current.save();

        getMainframe().setMessage("Produkt Nummer " + current.getNummer() + " gespeichert.");
    }

    public void save() {
        if (current.getId() <= 0) {

            if (jTextField4.getText().equals("")) {
                Integer tz = current.getNextIndex("produktnummer");
                jTextField4.setText(tz.toString());
                Log.Debug("Setting 'Productnumber' to " + tz);

            } else {

                try {
                    Integer i = Integer.valueOf(jTextField4.getText());

                    jTextField4.setText(i.toString());

                } catch (NumberFormatException numberFormatException) {

                    Integer tz = current.getNextIndex("produktnummer");
                    jTextField4.setText(tz.toString());
                    Log.Debug("Setting 'Productnumber' to " + tz);
                }
            }

            try {
                Double.valueOf(jTextField13.getText());
            } catch (NumberFormatException numberFormatException) {
                jTextField13.setText("0");
            }
            try {
                Double.valueOf(jTextField8.getText());
            } catch (NumberFormatException numberFormatException) {
                jTextField8.setText("0");
            }
            try {
                Double.valueOf(jTextField7.getText());
            } catch (NumberFormatException numberFormatException) {
                jTextField7.setText("0");
            }
            try {
                Double.valueOf(jTextField16.getText());
            } catch (NumberFormatException numberFormatException) {
                jTextField16.setText("0");
            }
            current.setNummer(
                    jTextField4.getText());
            current.setName(jTextField5.getText());
            current.setHersteller(hersteller);
            current.setSupplier(lieferant);
            current.setVK(Double.valueOf(jTextField8.getText()));
            current.setEK(Double.valueOf(jTextField7.getText()));
//            current.setTAX(Double.valueOf(jTextField16.getText()));
            current.setDatum(DateConverter.getDate(jTextField9.getText()));
            current.setUrl(jTextField11.getText());
            current.setEan(jTextField13.getText());
            current.setText(jEditorPane1.getText());
            current.save();

            getMainframe().setMessage("Produkt Nummer " + current.getNummer() + " gespeichert.");

        }

    }

    public void setSupplier(Lieferant supplier) {
        this.lieferant = supplier;

        if (supplier != null && !supplier.getId().equals("0")) {
            this.jTextField10.setText(supplier.getFirma());
            this.getProduct().setLieferantenId(supplier.getId());
        }
    }

    public javax.swing.JTextField getJTextField12() {
        return jTextField12;
    }

    public mainframe getMainframe() {
        return mainframe;
    }

    public Integer getCurrentProductGroup() {
        return currentProductGroupId;
    }

    public void setCurrentProductGroup(Integer currentProductGroupId) {
        this.currentProductGroupId = currentProductGroupId;
    }

    public void updateTables() {
        this.updateListTable();
    }

    public void close() {
        if (isEdited()) {
            if (Popup.Y_N_dialog("Änderungen verwerfen?")) {
                ((JTabbedPane) this.getParent()).remove(this);
            }
        } else {
            ((JTabbedPane) this.getParent()).remove(this);
        }
    }

    public void undo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void redo() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void changeTabText(String text) {
        ((JTabbedPane) this.getParent()).setTitleAt(((JTabbedPane) this.getParent()).getSelectedIndex(), text);
    }

    public boolean isEdited() {
        return edited;
    }

    private void setEdited(boolean edit) {
        edited = edit;
    }

    public void setContact(People contact) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public People getContact() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void switchTab(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void updateListTable() {
        liste = current.getAll();
        String k = "id, " + TABLE_PRODUCTS_LIST_COLUMNS;
        this.jTable2.setModel(new DefaultTableModel(liste, k.split(",")));
        TableFormat.stripFirst(jTable2);
    }
}

class GetProductImage extends SwingWorker<Void, Void> {

    private productsView view;

    public GetProductImage(productsView view) {
        this.view = view;
    }

    public Void doInBackground() {

//        Image coverImg = null;

        try {
//            coverImg = Toolkit.getDefaultToolkit().createImage(view.getCurrent().getUrl());
//            Image smallCoverImg = coverImg.getScaledInstance(217, 151, java.awt.Image.SCALE_FAST);
//            ImageIcon coverImgIcon = new ImageIcon(smallCoverImg);
            if (view.getCurrent().getImage().getImageIcon() != null) {
                view.jLabel13.setIcon(view.getCurrent().getImage().getImageIcon());
            }
        } catch (Exception ex) {
            Log.Debug(ex);
        }
        return null;
    }

    @Override
    public void done() {
        view.getMainframe().getMainProgress().setIndeterminate(false);
//        Toolkit.getDefaultToolkit().beep();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}

class GetAnyImage extends SwingWorker<Void, Void> {

    private productsView view;
    private String path;

    public GetAnyImage(productsView view, String path) {
        this.view = view;
        this.path = path;
    }

    public Void doInBackground() {

        Image coverImg = null;

        view.jLabel13.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        try {
            coverImg = Toolkit.getDefaultToolkit().createImage(path);
//            Image smallCoverImg = coverImg.getScaledInstance(view.jLabel13.getWidth()-3, view.jLabel13.getHeight()-3, java.awt.Image.SCALE_FAST);
            ImageIcon coverImgIcon = new ImageIcon(coverImg);
            view.jLabel13.setSize(new Dimension(coverImgIcon.getIconWidth(), coverImgIcon.getIconHeight()));
            view.jLabel13.setIcon(coverImgIcon);
            view.setCurrentImage(coverImg);
        } catch (Exception ex) {
            view.jLabel13.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            Log.Debug(ex);
        }
        return null;
    }

    @Override
    public void done() {
        view.jLabel13.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
//        Toolkit.getDefaultToolkit().beep();
        view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}

