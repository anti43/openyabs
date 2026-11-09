/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package mpv5.plugin.builtin;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import mpv5.db.common.DatabaseObject;
import mpv5.globals.Messages;
import mpv5.handler.Activity;
import mpv5.handler.SearchService;
import mpv5.logging.Log;
import mpv5.plugin.api.ExtensionContext;
import mpv5.plugin.api.SearchResult;
import mpv5.plugin.api.YabsExtension;

/**
 * Built-in example plugin: keeps the activity (created files, created,
 * changed and deleted records) in a CSV journal beyond the session; the
 * journal is searchable in Search anything.
 */
public final class ActivityJournal implements YabsExtension {

    private File journal;
    private ExtensionContext ctx;

    @Override
    public String id() {
        return "yabs.activity-journal";
    }

    @Override
    public String name() {
        return Messages.JOURNAL_NAME.toString();
    }

    @Override
    public String description() {
        return Messages.JOURNAL_DESCRIPTION.toString();
    }

    @Override
    public String author() {
        return "YaBS";
    }

    @Override
    public void start(ExtensionContext ctx) {
        this.ctx = ctx;
        journal = new File(ctx.dataFolder(), "journal.csv");
        ctx.onActivity(this::write);
        ctx.addCommand(Messages.JOURNAL_OPEN.toString(), Messages.JOURNAL_DESCRIPTION.toString(), this::openJournal);
        ctx.addSearchProvider(this::search);
        ctx.addSettingsPage(Messages.JOURNAL_NAME.toString(), Messages.JOURNAL_DESCRIPTION.toString(), this::page);
    }

    private void write(Activity.Entry e) {
        if (e.kind == Activity.Kind.MESSAGE || e.kind == Activity.Kind.WARNING) {
            if (!ctx.settings().getBoolean("messages", false)) {
                return;
            }
        }
        String line = String.join(";", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(e.time), e.kind.name(),
                csv(e.text), csv(e.detail), e.file == null ? "" : csv(e.file.getAbsolutePath()),
                e.context == null ? "" : e.context.getDbIdentity(), String.valueOf(e.id)) + "\n";
        try {
            Files.write(journal.toPath(), line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            Log.Debug(ActivityJournal.class, ex);
        }
    }

    private static String csv(String s) {
        return s == null ? "" : s.replace(";", ",").replace("\n", " ");
    }

    private List<SearchResult> search(String needle) {
        List<SearchResult> l = new ArrayList<SearchResult>();
        if (journal == null || !journal.isFile() || needle.trim().length() < 3) {
            return l;
        }
        try {
            List<String> lines = Files.readAllLines(journal.toPath(), StandardCharsets.UTF_8);
            Collections.reverse(lines);
            for (String line : lines) {
                if (l.size() >= 5) {
                    break;
                }
                final String[] p = line.split(";", -1);
                if (p.length < 7 || !SearchService.matches(p[2] + " " + p[3] + " " + p[4], needle)) {
                    continue;
                }
                l.add(new SearchResult(p[2], p[0] + "  ·  " + p[1], () -> openLine(p)));
            }
        } catch (IOException ex) {
            Log.Debug(ActivityJournal.class, ex);
        }
        return l;
    }

    private void openLine(String[] p) {
        try {
            if (!p[4].isEmpty() && new File(p[4]).exists()) {
                Desktop.getDesktop().open(new File(p[4]));
            } else if (!p[5].isEmpty() && Integer.parseInt(p[6]) > 0) {
                mpv5.db.common.Context c = mpv5.db.common.Context.getMatchingContext(p[5]);
                mpv5.YabsViewProxy.instance().getIdentifierView().addTab(DatabaseObject.getObject(c, Integer.parseInt(p[6]), true));
            } else {
                openJournal();
            }
        } catch (Exception ex) {
            Log.Debug(ActivityJournal.class, ex);
            ctx.notify(Messages.NOT_POSSIBLE + ": " + p[2]);
        }
    }

    private void openJournal() {
        try {
            if (!journal.exists()) {
                Files.write(journal.toPath(), new byte[0]);
            }
            Desktop.getDesktop().open(journal);
        } catch (Exception ex) {
            Log.Debug(ActivityJournal.class, ex);
            ctx.notify(journal.getAbsolutePath());
        }
    }

    private JComponent page() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(new JLabel(String.format(Messages.JOURNAL_FILE.toString(), journal.getAbsolutePath())));
        JCheckBox messages = new JCheckBox(Messages.JOURNAL_MESSAGES.toString(), ctx.settings().getBoolean("messages", false));
        messages.setOpaque(false);
        messages.addActionListener(e -> ctx.settings().setBoolean("messages", messages.isSelected()));
        p.add(messages);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setOpaque(false);
        JButton open = new JButton(Messages.JOURNAL_OPEN.toString());
        open.addActionListener(e -> openJournal());
        buttons.add(open);
        p.add(buttons);
        return p;
    }
}
