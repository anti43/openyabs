package plugins.sample2;
//package plugin;

//~--- non-JDK imports --------------------------------------------------------
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
      Notificator.raiseNotification("'Script on save' is active", false);
      loaded = true;
      return this;
   }

   @Override
   public void unload() {
      Notificator.raiseNotification("'Script on save' is deactivated", false);
      loaded = false;
   }

   @Override
   public String getName() {
      return "'Script on save' Plugin for YABS";
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

   public DatabaseObject modifyOnSave(DatabaseObject object) throws ChangeNotApprovedException {
      try {
         if (ValueProperty.hasProperty(object.getContext(), "modifyOnSave")) {
            ValueProperty script = ValueProperty.getProperty(object.getContext(), "modifyOnSave");
            Object s = script.getValue();
            if (s != null) {
               object.evaluate(String.valueOf(s));
            }
         }
         if (ValueProperty.hasProperty(object, "modifyOnSave")) {
            ValueProperty script = ValueProperty.getProperty(object, "modifyOnSave");
            Object s = script.getValue();
            if (s != null) {
               object.evaluate(String.valueOf(s));
            }
         }
      } catch (Exception ex) {
         Notificator.raiseNotification(ex, true, true, object);
      }
      return object;
   }

   public DatabaseObject modifyAfterCreate(DatabaseObject object) {
      return object;
   }

   public Map<String, Object> modifyOnResolve(Map<String, Object> map) {
      return map;
   }

   public DatabaseObject modifyOnDelete(DatabaseObject object) throws ChangeNotApprovedException {
      return object;
   }
}
