package mpv5.ui.dialogs.subcomponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import javax.swing.JEditorPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import mpv5.globals.Messages;
import org.junit.jupiter.api.Test;

class ControlPanel_MailTemplatesTest {

    @Test
    void testResolveSamplePlaceholders() {
        String template = "Hello {contact.cname},\nYour invoice {item.cnumber} of {item.totalbrut} is ready.\nRegards, {user.cname}";
        String resolved = ControlPanel_MailTemplates.resolveSamplePlaceholders(template);

        assertFalse(resolved.contains("{contact.cname}"), "Placeholder {contact.cname} should be resolved");
        assertFalse(resolved.contains("{item.cnumber}"), "Placeholder {item.cnumber} should be resolved");
        assertFalse(resolved.contains("{item.totalbrut}"), "Placeholder {item.totalbrut} should be resolved");
        assertFalse(resolved.contains("{user.cname}"), "Placeholder {user.cname} should be resolved");

        assertTrue(resolved.contains("Musterfirma GmbH"), "Should contain sample company name");
        assertTrue(resolved.contains("RE-2026-0042"), "Should contain sample invoice number");
        assertTrue(resolved.contains("1.190,00 €"), "Should contain sample amount");
        assertTrue(resolved.contains("Max Admin"), "Should contain sample user name");
    }

    @Test
    void testResolveSamplePlaceholdersWithBracketSyntax() {
        String template = "Invoice [item.cnumber] for <contact.cname>";
        String resolved = ControlPanel_MailTemplates.resolveSamplePlaceholders(template);

        assertTrue(resolved.contains("RE-2026-0042"));
        assertTrue(resolved.contains("Musterfirma GmbH"));
    }

    @Test
    void testTabbedLayoutAndPreview() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_MailTemplates panel = new ControlPanel_MailTemplates();
        JTabbedPane tabs = panel.getEditorTabs();
        assertNotNull(tabs, "Editor tabs should exist");
        assertEquals(2, tabs.getTabCount(), "Should have 2 tabs: Source and Preview");
        assertEquals(Messages.MAIL_PREVIEW_SOURCE.toString(), tabs.getTitleAt(0));
        assertEquals(Messages.MAIL_PREVIEW_RENDERED.toString(), tabs.getTitleAt(1));

        JTextArea source = panel.getSourceArea();
        assertNotNull(source, "Source text area should exist");

        JEditorPane preview = panel.getPreviewPane();
        assertNotNull(preview, "Preview editor pane should exist");
        assertFalse(preview.isEditable(), "Preview pane should not be editable");
        assertEquals("text/html", preview.getContentType(), "Preview pane should have text/html content type");

        // Set HTML source and render preview
        source.setText("<html><body><h1>Welcome</h1><p>Invoice {item.cnumber}</p></body></html>");
        panel.renderPreview();

        String html = preview.getText();
        assertNotNull(html);
        assertTrue(html.contains("Welcome"), "Rendered preview should contain 'Welcome'");
        assertTrue(html.contains("RE-2026-0042"), "Rendered preview should resolve '{item.cnumber}'");
    }

    @Test
    void testPlainTextPreviewWrapsHtml() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanel_MailTemplates panel = new ControlPanel_MailTemplates();
        JTextArea source = panel.getSourceArea();
        source.setText("First line\nSecond line");
        panel.renderPreview();

        String html = panel.getPreviewPane().getText();
        assertNotNull(html);
        assertTrue(html.toLowerCase().contains("<html"), "Plain text preview should be wrapped in HTML");
        assertTrue(html.contains("First line"), "Should contain first line");
        assertTrue(html.contains("Second line"), "Should contain second line");
    }
}
