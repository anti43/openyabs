// Copyright (C) 2007 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package mpv5.utils.files;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Monitor files for changes. This singleton class maintains a map of files to
 * monitor and objects to notify when something they change.
 */
public class FileMonitor {

  private static final FileMonitor SINGLETON = new FileMonitor();

  private Timer timer;
  private HashMap<String, TimerTask> timerTasks;

  private FileMonitor() {
    timer = new Timer(true);
    timerTasks = new HashMap<String, TimerTask>();
  }

  /**
   * Returns the singleton instance of this class.
   * @return the singleton instance
   */
  public static FileMonitor getInstance() {
    return SINGLETON;
  }

  /**
   * Start monitoring a file.
   * 
   * @param listener listener to notify when the file changed.
   * @param fileName name of the file to monitor.
   * @param period polling period in milliseconds.
   */
  public void addFileChangeListener(FileChangeListener listener,
      String fileName, long period) {
    removeFileChangeListener(listener, fileName);
    FileMonitorTask task = new FileMonitorTask(listener, fileName);
    timerTasks.put(fileName + listener.hashCode(), task);
    timer.schedule(task, period, period);
  }

  /**
   * Remove the listener from the notification list.
   * 
   * @param listener the listener to be removed.
   */
  public void removeFileChangeListener(FileChangeListener listener,
      String fileName) {
    FileMonitorTask task = (FileMonitorTask) timerTasks.remove(fileName
        + listener.hashCode());
    if (task != null) {
      task.cancel();
    }
  }

  protected void fireFileChangeEvent(FileChangeListener listener,
      String fileName) {
    listener.fileChanged(fileName);
  }

  class FileMonitorTask extends TimerTask {
    FileChangeListener listener;
    String fileName;
    File monitoredFile;
    long lastModified;

    public FileMonitorTask(FileChangeListener listener, String fileName) {
      this.listener = listener;
      this.fileName = fileName;
      this.lastModified = 0;
      monitoredFile = new File(fileName);
      this.lastModified = getLastModified();
    }

    private long getLastModified() {
      if (monitoredFile.exists()) { 
        return monitoredFile.lastModified();
      } else {
        return -1;
      }
    }

    @Override
    public void run() {
      long lastModifiedInt = getLastModified();
      if (lastModifiedInt != this.lastModified) {
        this.lastModified = lastModifiedInt;
        fireFileChangeEvent(this.listener, this.fileName);
      }
    }
  }
  
  public interface FileChangeListener {
    public void fileChanged(String fileName);
  }
}