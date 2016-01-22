package plugins.sample3;
//package plugin;

//~--- non-JDK imports --------------------------------------------------------
import plugins.sample2.*;
import mpv5.logging.Log;

import mpv5.pluginhandling.YabsPlugin;

import java.awt.Image;
import java.awt.image.BufferedImage;


import java.util.Map;

import javax.imageio.ImageIO;

import javax.swing.JPanel;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseObjectModifier;
import mpv5.db.objects.ValueProperty;
import mpv5.ui.dialogs.Notificator;
import mpv5.ui.panels.ChangeNotApprovedException;

/**
 * This is the sample of a plugin compatible with the MP5 Plugin system.<br/>
 * <b>In real plugins, this needs to go to a package named "plugin"</b> to be
 * recognized as a valid plugin.
 *
 */
public class Main extends JPanel implements YabsPlugin, Runnable, DatabaseObjectModifier {

   private static final long serialVersionUID = -2334458558243L;
   private boolean loaded = false;

   @Override
   public YabsPlugin load(Object frame) {
      Notificator.raiseNotification("'Script after save' is active", false);
      loaded = true;
      return this;
   }

   @Override
   public void unload() {
      Notificator.raiseNotification("'Script after save' is deactivated", false);
      loaded = false;
   }

   @Override
   public String getName() {
      return "'Script after save' Plugin for YABS";
   }

   @Override
   public String getVendor() {
      return "Andreas";
   }

   @Override
   public Long getUID() {
      return serialVersionUID;
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   @Override
   public boolean isComponent() {
      return false;
   }

   @Override
   public boolean isRunnable() {
      return false;
   }

   @Override
   public void run() {
   }

   @Override
   public boolean isLoaded() {
      return loaded;
   }

   @Override
   public Image getIcon() {
      BufferedImage img = null;

      try {
         img = ImageIO.read(getClass().getResource("/mpv5/resources/images/16/kwikdisk.png"));
      } catch (Exception e) {
         Log.Debug(e);
      }

      return img;
   }

   public DatabaseObject modifyOnExplode(DatabaseObject object) {
      return object;
   }

   public DatabaseObject executeAfterSave(DatabaseObject object) {
      try {
         if (ValueProperty.hasProperty(object.getContext(), "executeAfterSave")) {
            ValueProperty script = ValueProperty.getProperty(object.getContext(), "executeAfterSave");
            Object s = script.getValue();
            if (s != null) {
               object.evaluateAll(String.valueOf(s));
            }
         }
         if (ValueProperty.hasProperty(object, "executeAfterSave")) {
            ValueProperty script = ValueProperty.getProperty(object, "executeAfterSave");
            Object s = script.getValue();
            if (s != null) {
               object.evaluateAll(String.valueOf(s));
            }
         }
      } catch (Exception ex) {
         Notificator.raiseNotification(ex, true, true, object);
      }
      return object;
   }
   
   public DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException {
      return object;
   }

   public DatabaseObject executeAfterCreate(DatabaseObject object) {
      return object;
   }

   public Map<String, Object> modifyOnResolve(Map<String, Object> map) {
      return map;
   }

   public DatabaseObject modifyOnDelete(DatabaseObject object) throws ChangeNotApprovedException {
      return object;
   }

}
