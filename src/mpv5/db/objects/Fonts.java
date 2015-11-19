/*
 *  This file is part of YaBS.
 *
 *      YaBS is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      YaBS is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.db.objects;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JComponent;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseConnection;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.QueryCriteria2;
import mpv5.db.common.QueryParameter;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.utils.files.FileDirectoryHandler;
import mpv5.utils.images.MPIcon;

/**
 * This class represents the Fonts used in altenative
 * document generation.
 *
 */
public class Fonts extends DatabaseObject {

    private static final long serialVersionUID = -5432724230929989412L;
    private String encoding = "";
    private boolean embedded = false;
    private String filename;
    private File font;

    public Fonts() {
        setContext(Context.getFonts());
    }

    @Override
    public JComponent getView() {
        return null;
    }

    /**
     * @return the description
     */
    public String __getEncoding() {
        return encoding;
    }

    /**
     * @param encoding the encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * @return is embedded
     */
    public boolean __isEmbedded() {
        return embedded;
    }

    /**
     * @param embedded
     */
    public void setIsEmbedded(boolean embedded) {
        this.embedded = embedded;
    }

    /**
     * @return the color
     */
    public String __getFilename() {
        return filename;
    }

    /**
     * @param filename the color to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the font
     */
    public File __getFont() {
        return font;
    }

    /**
     * @param font the font to set
     */
    public void setFont(File font) {
        this.font = font;
    }

    @Override
    public MPIcon getIcon() {
        return null;
    }

    @Override
    public boolean save(boolean silent) {
        QueryCriteria2 c2 = new QueryCriteria2();
    
        c2.and(new QueryParameter(getContext(), "cname", __getCname(), QueryParameter.EQUALS));
        c2.and(new QueryParameter(getContext(), "encoding", __getEncoding(), QueryParameter.EQUALS));
        c2.and(new QueryParameter(getContext(), "embedded", __isEmbedded(), QueryParameter.EQUALS));
        if (exists(getContext(), c2)) {
            return false;
        }

        try {
            String query = "INSERT INTO " + this.getDbIdentity() + " (cname, encoding, embedded, filename, font) VALUES (?, ?, ?, ?, ?)";
            Log.Debug(this, "Adding file: " + this.__getCname());
            Connection sqlConn = DatabaseConnection.instanceOf().getConnection();
            try {
                sqlConn.setAutoCommit(false);
                PreparedStatement ps = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, this.__getCname());
                ps.setString(2, this.__getEncoding());
                ps.setBoolean(3, this.__isEmbedded());
                ps.setString(4, this.__getFilename());
                if (font != null) {
                    int fileLength = (int) font.length();
                    if (fileLength>0) {
                        java.io.InputStream fin = new java.io.FileInputStream(font);
                        ps.setBinaryStream(5, fin, fileLength);
                    }
                } else {
                    ps.setBinaryStream(5, null, 0);
                }
                ps.execute();
                sqlConn.commit();
            } catch (FileNotFoundException ex) {
                Log.Debug(this, "Datenbankfehler: " + query);
                Log.Debug(this, ex);
            } catch (SQLException ex) {
                Log.Debug(this, "Datenbankfehler: " + query);
                Log.Debug(this, ex);
            } finally {
                try {
                    sqlConn.setAutoCommit(true);
                } catch (SQLException ex) {
                    Log.Debug(this, ex);
                }
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }

        return true;
    }

    @Override
    public boolean delete() {
        try {
            String query = "DELETE FROM " + this.getDbIdentity() + " WHERE cname = ? AND encoding = ? AND embedded = ? AND filename = ?";
            Log.Debug(this, "Deleting file: " + this.__getCname());
            Connection sqlConn = DatabaseConnection.instanceOf().getConnection();
            try {
                sqlConn.setAutoCommit(false);
                int fileLength = (int) font.length();

                java.io.InputStream fin = new java.io.FileInputStream(font);
                PreparedStatement ps = sqlConn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setString(1, this.__getCname());
                ps.setString(2, this.__getEncoding());
                ps.setBoolean(3, this.__isEmbedded());
                ps.setString(4, this.__getFilename());
                ps.execute();
                sqlConn.commit();
            } catch (FileNotFoundException ex) {
                Log.Debug(this, "Datenbankfehler: " + query);
                Log.Debug(this, ex);
                Popup.error(ex);
            } catch (SQLException ex) {
                Log.Debug(this, "Datenbankfehler: " + query);
                Log.Debug(this, ex);
            } finally {
                try {
                    sqlConn.setAutoCommit(true);
                } catch (SQLException ex) {
                    Log.Debug(this, ex);
                }
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
        return true;
    }

    public static ArrayList<Fonts> get() {
        ArrayList<Fonts> list = new ArrayList<Fonts>();
        try {
            String query = "SELECT ids, cname, encoding, embedded, font, filename FROM "
                    + Context.getFonts().getDbIdentity();
            Connection sqlConn = DatabaseConnection.instanceOf().getConnection();
           
            Statement stm = null;
            ResultSet resultSet = null;
            try { 
                sqlConn.setAutoCommit(false);
                stm = sqlConn.createStatement();
                ResultSet rs = stm.executeQuery(query);

                Log.Debug(Fonts.class, query);
                while (rs.next()) {
                    Fonts f = new Fonts();
                    f.setIDS(rs.getInt("ids"));
                    f.setCname(rs.getString("cname"));
                    f.setEncoding(rs.getString("encoding"));
                    f.setIsEmbedded(rs.getBoolean("embedded"));
                    f.setFilename(rs.getString("filename"));
                    if (!f.__getFilename().equalsIgnoreCase("empty")) {
                        String sufix = f.__getFilename().substring(f.__getFilename().lastIndexOf("."));
                        byte[] buffer = new byte[1024];

                        InputStream binaryStream = rs.getBinaryStream("font");
                        File tmp = FileDirectoryHandler.getTempFile(sufix);
                        tmp.deleteOnExit();
                        BufferedInputStream inputStream = new BufferedInputStream(binaryStream, 1024);
                        FileOutputStream outputStream = new FileOutputStream(tmp);
                        int readBytes = 0;
                        while (readBytes != -1) {
                            readBytes = inputStream.read(buffer, 0, buffer.length);
                            if (readBytes != -1) {
                                outputStream.write(buffer, 0, readBytes);
                            }
                        }
                        inputStream.close();
                        outputStream.close();
                        f.setFont(tmp);
                    } else {
                        f.setFont(null);
                    }
                    list.add(f);
                }
            } catch (SQLException ex) {
                Log.Debug(Fonts.class, ex);
            } finally {
                try {
                    sqlConn.commit();
                    sqlConn.setAutoCommit(true);
                } catch (SQLException ex) {
                    Log.Debug(ex);
                }
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException ex) {
                        Log.Debug(Fonts.class, ex);
                    }
                }
                if (stm != null) {
                    try {
                        stm.close();
                    } catch (SQLException ex) {
                        Log.Debug(Fonts.class, ex);
                    }
                }
            }
        } catch (Exception ex) {
            Log.Debug(Fonts.class, ex);
        }
        return list;
    }
}
