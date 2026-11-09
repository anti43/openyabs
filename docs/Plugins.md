# Writing plugins for YaBS

Plugins add **commands**, **search results**, **settings pages** and **reactions** to YaBS – in **Groovy** (a single file, no build) or in **Java** (a jar).

- They live in the folder **`plugins`** in your YaBS folder (*Settings › Plugin Manager › Open folder*).
- *Settings › Plugin Manager* shows every plugin with its state. There you can enable, disable, reload, edit and remove plugins, and install one by choosing a file or dropping a `.jar` / `.groovy` file onto the page.
- Commands of plugins appear in the menu **Plugins** and in **Search anything** (`Shift Shift`).
- A plugin that fails is marked *failed* with its error message; YaBS and the other plugins go on.
- Everything a plugin adds is removed again when it stops (disable, reload, remove), so a plugin can be developed and reloaded without restarting YaBS.

## A plugin in Groovy – 30 seconds

*Settings › Plugin Manager › New Groovy plugin…* creates a working plugin from a template and opens it in your editor. Or create `plugins/hello.groovy` yourself:

```groovy
// @name Hello
// @description Says hello and notices new invoices
// @version 1.0
// @author You

// a command: menu "Plugins" and Search anything
yabs.addCommand('Say hello', 'A first command', {
    yabs.notify('Hello from Groovy!')
})

// react to new records (and created files, changed and deleted records)
yabs.onActivity({ entry ->
    if (entry.kind == mpv5.handler.Activity.Kind.CREATED && entry.detail == 'Rechnung') {
        yabs.notify('New invoice: ' + entry.text)
    }
})

// results in Search anything
yabs.addSearchProvider({ text ->
    text == 'weather' ? [new mpv5.plugin.api.SearchResult('Open the weather', 'wttr.in', {
        java.awt.Desktop.desktop.browse(new URI('https://wttr.in'))
    })] : []
})

// called when the plugin stops
onStop = { }
```

Save, then click **Reload** in the Plugin Manager – done.

- The comment lines at the top (`// @name`, `@description`, `@version`, `@author`, `@id`) describe the plugin; the id defaults to `groovy.<file name>`.
- The script runs when the plugin starts, with **`yabs`** bound to the [`ExtensionContext`](../src/main/java/mpv5/plugin/api/ExtensionContext.java). Closures work wherever the API expects a function.
- A script may also `return` an object that implements `YabsExtension` – then that object is the plugin.

## A plugin in Java

1. Implement [`mpv5.plugin.api.YabsExtension`](../src/main/java/mpv5/plugin/api/YabsExtension.java):

```java
package com.example;

import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.YabsExtension;

public class Hello implements YabsExtension {
    public String id()          { return "com.example.hello"; }
    public String name()        { return "Hello"; }
    public String description() { return "Says hello"; }

    public void start(ExtensionContext ctx) {
        ctx.addCommand("Say hello", "A first command", () -> ctx.notify("Hello!"));
    }
}
```

2. List the class in **`META-INF/services/mpv5.plugin.api.YabsExtension`** (one class per line – a jar may contain several plugins):

```
com.example.Hello
```

3. Compile against `openyabs.jar` (and `lib/*` if you use them), build a jar, put it into `plugins` or install it in the Plugin Manager.

A minimal `build.gradle`:

```groovy
plugins { id 'java' }
dependencies { compileOnly files('/path/to/openyabs/openyabs.jar') }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
```

## What the context offers

| Method | What it does |
|---|---|
| `addCommand(title, description, action)` | a command in the menu *Plugins* and in Search anything |
| `addSearchProvider(text -> List<SearchResult>)` | results in Search anything (runs in the background; keep it quick) |
| `addSettingsPage(title, description, () -> JComponent)` | a page in *Settings* (group *Plugins*), found by the settings search |
| `onActivity(entry -> …)` | told about created / changed / deleted records, created files (PDF, ODT, e-invoices) and messages – see [`Activity.Entry`](../src/main/java/mpv5/handler/Activity.java): `kind`, `text`, `detail`, `file`, `context`, `id` |
| `addRecordModifier(DatabaseObjectModifier)` | take part when records are saved, deleted, created (check or change them) |
| `openTab(title, component)` | open a tab in the main window |
| `notify(message)` | a message in the status bar and the activity list |
| `settings()` | key/value settings of the plugin, stored on this computer |
| `dataFolder()` | a folder for the plugin's own files |
| `background(work, then)` | run work in the background and continue on the Swing thread; errors are reported |

The lifecycle methods `start` and `stop` run on the Swing thread. A plugin only needs `stop()` for what it created itself (threads, connections) – everything registered through the context is removed by YaBS.

Records, contexts and queries are the normal YaBS classes (`mpv5.db.common.DatabaseObject`, `Context`, `QueryHandler`, …); the Groovy console (*Settings › GroovyConsole*) is a good place to try things out.

## Built-in example

The **Activity journal** ([source](../src/main/java/mpv5/plugin/builtin/ActivityJournal.java)) ships with YaBS. It uses most of the API:
- it writes created files and records to a CSV journal;
- it makes the journal searchable in Search anything;
- it offers a command and a settings page.

You can disable it like any other plugin.

## Classic plugins

Plugins of older YaBS versions (a class `plugin.Main` implementing `mpv5.pluginhandling.YabsPlugin`, stored in the database) still work; they are listed at the bottom of the Plugin Manager.
