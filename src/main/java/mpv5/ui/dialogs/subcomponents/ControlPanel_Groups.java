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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Applets;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.models.MPComboboxModel;
import mpv5.utils.trees.TreeFormat;
import mpv5.utils.ui.TextFieldUtils;

/**
 * Modern group management panel: hierarchy tree on the left, details card with
 * parent-group dropdown and cycle prevention on the right.
 */
public class ControlPanel_Groups extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    private Group dataOwner;
    private boolean initialized;
    private boolean updatingTree;

    private final JTree tree = new JTree(new DefaultMutableTreeNode("root"));
    private final JLabel title = new JLabel();
    private final JTextField cname = ModernTheme.roomy(new JTextField());
    private final JComboBox<MPComboBoxModelItem> parentCombo = new JComboBox<MPComboBoxModelItem>();
    private final JTextField pathField = ModernTheme.roomy(new JTextField());
    private final JTextArea desc = new JTextArea(4, 24);
    private final JButton newButton = new JButton();
    private final JButton deleteButton = Applets.danger(new JButton());
    private final JButton resetButton = new JButton();
    private PrimaryButton saveButton;
    private JPanel actions = new JPanel();

    // Public DataPanel binding fields (kept for DatabaseObject.setPanelData / getPanelData compatibility)
    public String description_ = "";
    public String defaults_ = "";
    public String cname_ = "";
    public int ids_;
    public Group group_;
    public int intaddedby_ = 1;
    public java.util.Date dateadded_ = new java.util.Date();
    public String hierarchypath_ = "";

    public ControlPanel_Groups() {
        this(null);
    }

    public ControlPanel_Groups(Group initial) {
        super(new BorderLayout(ModernTheme.px(12), 0));
        setOpaque(false);
        setBorder(ModernTheme.padding(12, 14));
        if (!MPSecurityManager.checkAdminAccess()) {
            add(Applets.hint(Messages.ADMIN_ACCESS.toString()), BorderLayout.NORTH);
            return;
        }
        buildUi();
        initialized = true;
        refresh();
        if (initial != null && initial.isExisting()) {
            setDataOwner(initial, true);
            selectGroupInTree(initial.__getIDS());
        } else {
            newGroup();
        }
    }

    private void buildUi() {
        ResourceBundle bundle = LanguageManager.getBundle();

        // Left: "+ New" button + group hierarchy tree
        JPanel left = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        left.setOpaque(false);
        newButton.setText("+ " + Messages.NEW_VALUE);
        newButton.setName("newGroupButton");
        newButton.addActionListener(e -> newGroup());
        left.add(newButton, BorderLayout.NORTH);

        tree.setName("tree");
        tree.setLargeModel(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            if (updatingTree) {
                return;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Group) {
                setDataOwner((Group) node.getUserObject(), true);
            }
        });
        JScrollPane treeScroll = new JScrollPane(tree);
        treeScroll.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        left.add(treeScroll, BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(ModernTheme.px(260), ModernTheme.px(320)));
        add(left, BorderLayout.WEST);

        // Right: Title + modern details Card
        title.setFont(ModernTheme.font(4, Font.BOLD));
        cname.setName("cname");
        parentCombo.setName("parentCombo");
        parentCombo.addActionListener(e -> updateComputedPath());
        pathField.setName("path");
        pathField.setEditable(false);
        pathField.setFocusable(false);
        desc.setName("desc");
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);
        descScroll.setPreferredSize(new Dimension(ModernTheme.px(260), ModernTheme.px(110)));

        Card card = new Card(null)
                .row(Card.field(bundle.getString("ControlPanel_Groups.cname._Label"), cname))
                .row(Card.field(bundle.getString("ControlPanel_Groups.parents._Label"), parentCombo),
                        Card.field(bundle.getString("ControlPanel_Groups.path._Label"), pathField))
                .row(Card.field(bundle.getString("ControlPanel_Groups.jLabel2.text"), descScroll));

        JPanel right = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        right.add(top, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        // Action bar
        deleteButton.setText(bundle.getString("ControlPanel_Groups.jButton4.text"));
        deleteButton.setName("deleteButton");
        deleteButton.addActionListener(e -> deleteSelected());

        resetButton.setText(bundle.getString("ControlPanel_Groups.jButton3.text"));
        resetButton.setName("resetButton");
        resetButton.addActionListener(e -> reset());

        saveButton = new PrimaryButton(bundle.getString("ControlPanel_Groups.jButton2.text"), "/images/16/filesave.png");
        saveButton.setName("saveButton");
        saveButton.addActionListener(e -> save());

        actions = Applets.actionBar(new JComponent[]{deleteButton}, new JComponent[]{resetButton, saveButton});
    }

    /**
     * Prepares the form to create a new group (defaulting parent to the
     * currently selected group if one is selected, otherwise the root group).
     */
    public void newGroup() {
        if (!initialized) {
            return;
        }
        int suggestedParent = (dataOwner != null && dataOwner.isExisting()) ? dataOwner.__getIDS() : 1;
        dataOwner = null;
        ids_ = -1;
        updatingTree = true;
        tree.clearSelection();
        updatingTree = false;
        cname.setText("");
        desc.setText("");
        reloadParentCombo(-1, suggestedParent);
        parentCombo.setEnabled(true);
        updateComputedPath();
        title.setText(Messages.NEW_VALUE.toString());
        deleteButton.setEnabled(false);
        cname.requestFocusInWindow();
    }

    /**
     * Creates or updates the group in the database and refreshes the tree.
     */
    public boolean save() {
        if (!initialized) {
            return false;
        }
        String name = cname.getText() == null ? "" : cname.getText().trim();
        if (name.isEmpty()) {
            showRequiredFields();
            return false;
        }
        int currentId = dataOwner != null && dataOwner.isExisting() ? dataOwner.__getIDS() : -1;
        if (isDuplicateName(name, currentId)) {
            Popup.notice(Messages.VALUE_ALREADY_EXISTS + ": " + name);
            showRequiredFields();
            return false;
        }

        Group target = (dataOwner != null && dataOwner.isExisting()) ? dataOwner : new Group();
        target.setCname(name);
        target.setDescription(desc.getText() == null ? "" : desc.getText().trim());

        if (target.isExisting() && target.__getIDS() == 1) {
            // Root group must always have groupsids = 0 to prevent infinite loops
            target.setGroupsids(0);
            target.setHierarchypath(File.separator);
        } else {
            int parentId = selectedParentId();
            if (parentId <= 0 || parentId == currentId) {
                parentId = 1;
            }
            target.setGroupsids(parentId);
            target.setHierarchypath(computePathForParent(parentId));
        }

        if (!target.save()) {
            showRequiredFields();
            return false;
        }

        DatabaseObject.clearCache(Context.getGroup());
        dataOwner = target;
        refresh();
        setDataOwner(target, true);
        selectGroupInTree(target.__getIDS());
        mpv5.YabsViewProxy.instance().addMessage(Messages.DONE + ": " + target.__getCname());
        return true;
    }

    private void deleteSelected() {
        if (!initialized || dataOwner == null || !dataOwner.isExisting()) {
            return;
        }
        if (dataOwner.__getIDS() == 1) {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.IN_USE);
            return;
        }
        if (!Popup.Y_N_dialog(Messages.REALLY_DELETE)) {
            return;
        }
        if (!dataOwner.delete()) {
            Popup.notice(Messages.NOT_POSSIBLE + "\n" + Messages.IN_USE);
            return;
        }
        DatabaseObject.clearCache(Context.getGroup());
        dataOwner = null;
        refresh();
        newGroup();
    }

    private boolean isDuplicateName(String name, int ignoreId) {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getGroup())) {
                if (o.__getIDS() != ignoreId && name.equalsIgnoreCase(String.valueOf(o.__getCname()).trim())) {
                    return true;
                }
            }
        } catch (NodataFoundException ignored) {
        }
        return false;
    }

    private void reloadParentCombo(int excludeId, int selectParentId) {
        List<Group> all = loadAllGroups();
        Map<Integer, List<Group>> children = new HashMap<Integer, List<Group>>();
        Set<Integer> ids = new HashSet<Integer>();
        for (Group g : all) {
            ids.add(g.__getIDS());
        }
        List<Group> roots = new ArrayList<Group>();
        for (Group g : all) {
            int pid = g.__getGroupsids();
            if (pid <= 0 || pid == g.__getIDS() || !ids.contains(pid)) {
                roots.add(g);
            } else {
                children.computeIfAbsent(pid, k -> new ArrayList<Group>()).add(g);
            }
        }
        Comparator<Group> byName = Comparator.comparing(g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER);
        roots.sort(byName);
        for (List<Group> l : children.values()) {
            l.sort(byName);
        }

        // Exclude the edited group and all of its descendants to prevent hierarchy cycles
        Set<Integer> forbidden = collectSubtreeIds(excludeId, children);
        List<MPComboBoxModelItem> items = new ArrayList<MPComboBoxModelItem>();
        for (Group r : roots) {
            appendParentItems(items, children, r, 0, forbidden, new HashSet<Integer>());
        }
        if (items.isEmpty()) {
            Group def = Group.getDefault();
            items.add(new MPComboBoxModelItem(def.__getIDS(), def.__getCname()));
        }
        parentCombo.setModel(new MPComboboxModel(items.toArray(new MPComboBoxModelItem[0])));

        int selIndex = 0;
        for (int i = 0; i < items.size(); i++) {
            if (String.valueOf(selectParentId).equals(items.get(i).getId())) {
                selIndex = i;
                break;
            }
        }
        if (parentCombo.getItemCount() > 0) {
            parentCombo.setSelectedIndex(selIndex);
        }
    }

    static Set<Integer> collectSubtreeIds(int rootId, Map<Integer, List<Group>> children) {
        if (rootId <= 0) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<Integer>();
        java.util.Deque<Integer> stack = new java.util.ArrayDeque<Integer>();
        stack.add(rootId);
        while (!stack.isEmpty()) {
            Integer id = stack.poll();
            if (result.add(id)) {
                for (Group child : children.getOrDefault(id, Collections.emptyList())) {
                    stack.add(child.__getIDS());
                }
            }
        }
        return result;
    }

    private static void appendParentItems(List<MPComboBoxModelItem> items, Map<Integer, List<Group>> children,
            Group g, int depth, Set<Integer> forbidden, Set<Integer> seen) {
        if (forbidden.contains(g.__getIDS()) || !seen.add(g.__getIDS())) {
            return;
        }
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("    ");
        }
        items.add(new MPComboBoxModelItem(g.__getIDS(), indent.toString() + g.__getCname()));
        for (Group child : children.getOrDefault(g.__getIDS(), Collections.emptyList())) {
            appendParentItems(items, children, child, depth + 1, forbidden, seen);
        }
    }

    private int selectedParentId() {
        Object sel = parentCombo.getSelectedItem();
        if (sel instanceof MPComboBoxModelItem) {
            try {
                return Integer.parseInt(((MPComboBoxModelItem) sel).getId());
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    private void updateComputedPath() {
        if (dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS() == 1) {
            pathField.setText(File.separator);
        } else {
            pathField.setText(computePathForParent(selectedParentId()));
        }
    }

    private String computePathForParent(int parentId) {
        Map<Integer, Group> byId = new HashMap<Integer, Group>();
        for (Group g : loadAllGroups()) {
            byId.put(g.__getIDS(), g);
        }
        List<String> parts = new ArrayList<String>();
        Set<Integer> seen = new HashSet<Integer>();
        int cur = parentId;
        while (cur > 0 && seen.add(cur)) {
            Group g = byId.get(cur);
            if (g == null) {
                break;
            }
            parts.add(g.__getCname());
            cur = g.__getGroupsids();
        }
        Collections.reverse(parts);
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(p).append(File.separator);
        }
        return sb.length() == 0 ? File.separator : sb.toString();
    }

    private List<Group> loadAllGroups() {
        List<Group> result = new ArrayList<Group>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getGroup())) {
                if (o instanceof Group) {
                    result.add((Group) o);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        return result;
    }

    private DefaultTreeModel buildTreeModel() {
        List<Group> all = loadAllGroups();
        Map<Integer, Group> byId = new HashMap<Integer, Group>();
        Map<Integer, List<Group>> children = new HashMap<Integer, List<Group>>();
        for (Group g : all) {
            byId.put(g.__getIDS(), g);
        }
        Group rootGroup = byId.get(1);
        if (rootGroup == null) {
            for (Group g : all) {
                if (g.__getGroupsids() <= 0) {
                    rootGroup = g;
                    break;
                }
            }
        }
        if (rootGroup == null) {
            rootGroup = new Group(Messages.GROUPNAMES.toString());
            rootGroup.setIDS(-1);
        }
        for (Group g : all) {
            if (g.__getIDS() == rootGroup.__getIDS()) {
                continue;
            }
            int pid = g.__getGroupsids();
            if (pid <= 0 || pid == g.__getIDS() || !byId.containsKey(pid)) {
                pid = rootGroup.__getIDS();
            }
            children.computeIfAbsent(pid, k -> new ArrayList<Group>()).add(g);
        }
        Comparator<Group> byName = Comparator.comparing(g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER);
        for (List<Group> l : children.values()) {
            l.sort(byName);
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootGroup);
        buildNodes(rootNode, rootGroup.__getIDS(), children, new HashSet<Integer>(Collections.singleton(rootGroup.__getIDS())));
        return new DefaultTreeModel(rootNode);
    }

    private static void buildNodes(DefaultMutableTreeNode parentNode, int parentId,
            Map<Integer, List<Group>> children, Set<Integer> seen) {
        for (Group child : children.getOrDefault(parentId, Collections.emptyList())) {
            if (seen.add(child.__getIDS())) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(child);
                parentNode.add(node);
                buildNodes(node, child.__getIDS(), children, seen);
            }
        }
    }

    private void selectGroupInTree(int groupId) {
        if (!initialized || groupId <= 0) {
            return;
        }
        Object rootObj = tree.getModel().getRoot();
        if (!(rootObj instanceof DefaultMutableTreeNode)) {
            return;
        }
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) rootObj;
        Enumeration<TreeNode> en = rootNode.depthFirstEnumeration();
        while (en.hasMoreElements()) {
            TreeNode tn = en.nextElement();
            if (tn instanceof DefaultMutableTreeNode) {
                Object uo = ((DefaultMutableTreeNode) tn).getUserObject();
                if (uo instanceof Group && ((Group) uo).__getIDS() == groupId) {
                    TreePath tp = new TreePath(((DefaultMutableTreeNode) tn).getPath());
                    updatingTree = true;
                    tree.setSelectionPath(tp);
                    tree.scrollPathToVisible(tp);
                    updatingTree = false;
                    return;
                }
            }
        }
    }

    @Override
    public void showRequiredFields() {
        if (initialized) {
            TextFieldUtils.blinkerRed(cname);
        }
    }

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return "Groups";
    }

    @Override
    public void reset() {
        if (!initialized) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                Group fresh = (Group) DatabaseObject.getObject(Context.getGroup(), dataOwner.__getIDS(), true);
                setDataOwner(fresh, true);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        } else {
            newGroup();
        }
    }

    @Override
    public void refresh() {
        if (!initialized) {
            return;
        }
        updatingTree = true;
        tree.setModel(buildTreeModel());
        TreeFormat.expandTree(tree);
        updatingTree = false;
    }

    @Override
    public boolean collectData() {
        if (!initialized) {
            return false;
        }
        if (cname.getText() == null || cname.getText().trim().isEmpty()) {
            return false;
        }
        cname_ = cname.getText().trim();
        description_ = desc.getText() == null ? "" : desc.getText().trim();
        hierarchypath_ = pathField.getText();
        int pid = selectedParentId();
        if (dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS() == 1) {
            group_ = null;
        } else {
            group_ = Group.getGroupOrDefault(pid);
        }
        return true;
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean p) {
        if (!(object instanceof Group)) {
            return;
        }
        dataOwner = (Group) object;
        if (p && initialized) {
            dataOwner.setPanelData(this);
            exposeData();
        }
    }

    @Override
    public void exposeData() {
        if (!initialized) {
            return;
        }
        cname.setText(cname_ == null ? "" : cname_);
        desc.setText(description_ == null ? "" : description_);
        boolean isRoot = dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS() == 1;
        int curId = dataOwner != null && dataOwner.isExisting() ? dataOwner.__getIDS() : -1;
        int parentId = dataOwner != null ? dataOwner.__getGroupsids() : 1;
        reloadParentCombo(curId, parentId);
        parentCombo.setEnabled(!isRoot);
        if (isRoot) {
            pathField.setText(File.separator);
        } else {
            pathField.setText(hierarchypath_ != null && !hierarchypath_.isEmpty()
                    ? hierarchypath_ : computePathForParent(selectedParentId()));
        }
        title.setText(cname_ != null && !cname_.isEmpty() ? cname_ : Messages.NEW_VALUE.toString());
        deleteButton.setEnabled(dataOwner != null && dataOwner.isExisting() && !isRoot);
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        if (!initialized) {
            return;
        }
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getGroup().getDbIdentity())) {
                setDataOwner(dbo, true);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
        }
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return initialized ? actions : null;
    }

    @Override
    public void actionAfterSave() {
    }

    @Override
    public void actionAfterCreate() {
    }

    @Override
    public void actionBeforeCreate() {
    }

    @Override
    public void actionBeforeSave() {
    }

    @Override
    public void mail() {
    }

    @Override
    public void print() {
        mpv5.utils.export.Export.print(this);
    }
}
