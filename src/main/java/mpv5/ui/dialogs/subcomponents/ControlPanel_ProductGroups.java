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
import mpv5.db.objects.ProductGroup;
import mpv5.globals.Messages;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.dialogs.Wizard;
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
 * Modern product group management panel: hierarchy tree on the left, details
 * card with parent product-group dropdown (cycle-safe), user-group dropdown,
 * CSV import, and immediate tree/cache refresh on the right.
 */
public class ControlPanel_ProductGroups extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 1L;
    private ProductGroup dataOwner;
    private boolean initialized;
    private boolean updatingTree;

    private final JTree tree = new JTree(new DefaultMutableTreeNode("root"));
    private final JLabel title = new JLabel();
    private final JTextField cname = ModernTheme.roomy(new JTextField());
    private final JComboBox<MPComboBoxModelItem> parentCombo = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox<MPComboBoxModelItem> groupCombo = new JComboBox<MPComboBoxModelItem>();
    private final JTextField pathField = ModernTheme.roomy(new JTextField());
    private final JTextArea desc = new JTextArea(4, 24);
    private final JButton newButton = new JButton();
    private final JButton importButton = new JButton();
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
    public int productgroupsids_;
    public java.util.Date dateadded_ = new java.util.Date();
    public String hierarchypath_ = "";

    public ControlPanel_ProductGroups() {
        this(null);
    }

    public ControlPanel_ProductGroups(ProductGroup initial) {
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
            newProductGroup();
        }
    }

    private void buildUi() {
        ResourceBundle bundle = LanguageManager.getBundle();

        // Left: "+ New" button + product group hierarchy tree
        JPanel left = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        left.setOpaque(false);
        newButton.setText("+ " + Messages.NEW_VALUE);
        newButton.setName("newProductGroupButton");
        newButton.addActionListener(e -> newProductGroup());
        left.add(newButton, BorderLayout.NORTH);

        tree.setName("tree");
        tree.setLargeModel(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addTreeSelectionListener(e -> {
            if (updatingTree) {
                return;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof ProductGroup) {
                setDataOwner((ProductGroup) node.getUserObject(), true);
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
        groupCombo.setName("groupCombo");
        pathField.setName("path");
        pathField.setEditable(false);
        pathField.setFocusable(false);
        desc.setName("desc");
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);
        descScroll.setPreferredSize(new Dimension(ModernTheme.px(260), ModernTheme.px(110)));

        Card card = new Card(null)
                .row(Card.field(bundle.getString("ControlPanel_ProductGroups.cname._Label"), cname),
                        Card.field(bundle.getString("ControlPanel_ProductGroups.gcombobox._Label"), groupCombo))
                .row(Card.field(bundle.getString("ControlPanel_ProductGroups.parents._Label"), parentCombo),
                        Card.field(bundle.getString("ControlPanel_ProductGroups.path._Label"), pathField))
                .row(Card.field(bundle.getString("ControlPanel_ProductGroups.jLabel2.text"), descScroll));

        JPanel right = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        right.add(top, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        // Action bar
        deleteButton.setText(bundle.getString("ControlPanel_ProductGroups.jButton4.text"));
        deleteButton.setName("deleteButton");
        deleteButton.addActionListener(e -> deleteSelected());

        importButton.setText(bundle.getString("ControlPanel_ProductGroups.jButton5.text"));
        importButton.setName("importButton");
        importButton.addActionListener(e -> importf());

        resetButton.setText(bundle.getString("ControlPanel_ProductGroups.jButton3.text"));
        resetButton.setName("resetButton");
        resetButton.addActionListener(e -> reset());

        saveButton = new PrimaryButton(bundle.getString("ControlPanel_ProductGroups.jButton2.text"), "/images/16/filesave.png");
        saveButton.setName("saveButton");
        saveButton.addActionListener(e -> save());

        actions = Applets.actionBar(new JComponent[]{deleteButton, importButton}, new JComponent[]{resetButton, saveButton});
    }

    /**
     * Prepares the form to create a new product group (defaulting parent to
     * the currently selected product group if one is selected).
     */
    public void newProductGroup() {
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
        reloadUserGroupCombo(1);
        parentCombo.setEnabled(true);
        updateComputedPath();
        title.setText(Messages.NEW_VALUE.toString());
        deleteButton.setEnabled(false);
        cname.requestFocusInWindow();
    }

    /**
     * Creates or updates the product group in the database and refreshes the tree.
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

        ProductGroup target = (dataOwner != null && dataOwner.isExisting()) ? dataOwner : new ProductGroup();
        target.setCname(name);
        target.setDescription(desc.getText() == null ? "" : desc.getText().trim());

        int userGroupId = selectedComboId(groupCombo, 1);
        target.setGroupsids(userGroupId);

        if (target.isExisting() && target.__getIDS() == 1) {
            target.setProductgroupsids(0);
            target.setHierarchypath(File.separator);
        } else {
            int parentId = selectedComboId(parentCombo, 1);
            if (parentId <= 0 || parentId == currentId) {
                parentId = 1;
            }
            target.setProductgroupsids(parentId);
            target.setHierarchypath(computePathForParent(parentId));
        }

        if (!target.save()) {
            showRequiredFields();
            return false;
        }

        DatabaseObject.clearCache(Context.getProductGroup());
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
        DatabaseObject.clearCache(Context.getProductGroup());
        dataOwner = null;
        refresh();
        newProductGroup();
    }

    private boolean isDuplicateName(String name, int ignoreId) {
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProductGroup())) {
                if (o.__getIDS() != ignoreId && name.equalsIgnoreCase(String.valueOf(o.__getCname()).trim())) {
                    return true;
                }
            }
        } catch (NodataFoundException ignored) {
        }
        return false;
    }

    private void reloadParentCombo(int excludeId, int selectParentId) {
        List<ProductGroup> all = loadAllProductGroups();
        Map<Integer, List<ProductGroup>> children = new HashMap<Integer, List<ProductGroup>>();
        Set<Integer> ids = new HashSet<Integer>();
        for (ProductGroup g : all) {
            ids.add(g.__getIDS());
        }
        List<ProductGroup> roots = new ArrayList<ProductGroup>();
        for (ProductGroup g : all) {
            int pid = g.__getProductgroupsids();
            if (pid <= 0 || pid == g.__getIDS() || !ids.contains(pid)) {
                roots.add(g);
            } else {
                children.computeIfAbsent(pid, k -> new ArrayList<ProductGroup>()).add(g);
            }
        }
        Comparator<ProductGroup> byName = Comparator.comparing(g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER);
        roots.sort(byName);
        for (List<ProductGroup> l : children.values()) {
            l.sort(byName);
        }

        Set<Integer> forbidden = collectSubtreeIds(excludeId, children);
        List<MPComboBoxModelItem> items = new ArrayList<MPComboBoxModelItem>();
        for (ProductGroup r : roots) {
            appendParentItems(items, children, r, 0, forbidden, new HashSet<Integer>());
        }
        if (items.isEmpty()) {
            ProductGroup def = ProductGroup.getDefault();
            items.add(new MPComboBoxModelItem(def.__getIDS(), def.__getCname()));
        }
        parentCombo.setModel(new MPComboboxModel(items.toArray(new MPComboBoxModelItem[0])));
        selectComboById(parentCombo, selectParentId);
    }

    static Set<Integer> collectSubtreeIds(int rootId, Map<Integer, List<ProductGroup>> children) {
        if (rootId <= 0) {
            return Collections.emptySet();
        }
        Set<Integer> result = new HashSet<Integer>();
        java.util.Deque<Integer> stack = new java.util.ArrayDeque<Integer>();
        stack.add(rootId);
        while (!stack.isEmpty()) {
            Integer id = stack.poll();
            if (result.add(id)) {
                for (ProductGroup child : children.getOrDefault(id, Collections.emptyList())) {
                    stack.add(child.__getIDS());
                }
            }
        }
        return result;
    }

    private static void appendParentItems(List<MPComboBoxModelItem> items, Map<Integer, List<ProductGroup>> children,
            ProductGroup g, int depth, Set<Integer> forbidden, Set<Integer> seen) {
        if (forbidden.contains(g.__getIDS()) || !seen.add(g.__getIDS())) {
            return;
        }
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("    ");
        }
        items.add(new MPComboBoxModelItem(g.__getIDS(), indent.toString() + g.__getCname()));
        for (ProductGroup child : children.getOrDefault(g.__getIDS(), Collections.emptyList())) {
            appendParentItems(items, children, child, depth + 1, forbidden, seen);
        }
    }

    private void reloadUserGroupCombo(int selectGroupId) {
        List<MPComboBoxModelItem> items = new ArrayList<MPComboBoxModelItem>();
        try {
            List<Group> groups = new ArrayList<Group>();
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getGroup())) {
                if (o instanceof Group) {
                    groups.add((Group) o);
                }
            }
            groups.sort(Comparator.comparing(g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER));
            for (Group g : groups) {
                items.add(new MPComboBoxModelItem(g.__getIDS(), g.__getCname()));
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        if (items.isEmpty()) {
            Group def = Group.getDefault();
            items.add(new MPComboBoxModelItem(def.__getIDS(), def.__getCname()));
        }
        groupCombo.setModel(new MPComboboxModel(items.toArray(new MPComboBoxModelItem[0])));
        selectComboById(groupCombo, selectGroupId > 0 ? selectGroupId : 1);
    }

    private static void selectComboById(JComboBox<MPComboBoxModelItem> combo, int id) {
        int selIndex = 0;
        for (int i = 0; i < combo.getItemCount(); i++) {
            MPComboBoxModelItem item = combo.getItemAt(i);
            if (item != null && String.valueOf(id).equals(item.getId())) {
                selIndex = i;
                break;
            }
        }
        if (combo.getItemCount() > 0) {
            combo.setSelectedIndex(selIndex);
        }
    }

    private static int selectedComboId(JComboBox<MPComboBoxModelItem> combo, int fallback) {
        Object sel = combo.getSelectedItem();
        if (sel instanceof MPComboBoxModelItem) {
            try {
                return Integer.parseInt(((MPComboBoxModelItem) sel).getId());
            } catch (NumberFormatException ignored) {
            }
        }
        return fallback;
    }

    private void updateComputedPath() {
        if (dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS() == 1) {
            pathField.setText(File.separator);
        } else {
            pathField.setText(computePathForParent(selectedComboId(parentCombo, 1)));
        }
    }

    private String computePathForParent(int parentId) {
        Map<Integer, ProductGroup> byId = new HashMap<Integer, ProductGroup>();
        for (ProductGroup g : loadAllProductGroups()) {
            byId.put(g.__getIDS(), g);
        }
        List<String> parts = new ArrayList<String>();
        Set<Integer> seen = new HashSet<Integer>();
        int cur = parentId;
        while (cur > 0 && seen.add(cur)) {
            ProductGroup g = byId.get(cur);
            if (g == null) {
                break;
            }
            parts.add(g.__getCname());
            cur = g.__getProductgroupsids();
        }
        Collections.reverse(parts);
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            sb.append(p).append(File.separator);
        }
        return sb.length() == 0 ? File.separator : sb.toString();
    }

    private List<ProductGroup> loadAllProductGroups() {
        List<ProductGroup> result = new ArrayList<ProductGroup>();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getProductGroup())) {
                if (o instanceof ProductGroup) {
                    result.add((ProductGroup) o);
                }
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex.getMessage());
        }
        return result;
    }

    private DefaultTreeModel buildTreeModel() {
        List<ProductGroup> all = loadAllProductGroups();
        Map<Integer, ProductGroup> byId = new HashMap<Integer, ProductGroup>();
        Map<Integer, List<ProductGroup>> children = new HashMap<Integer, List<ProductGroup>>();
        for (ProductGroup g : all) {
            byId.put(g.__getIDS(), g);
        }
        ProductGroup rootGroup = byId.get(1);
        if (rootGroup == null) {
            for (ProductGroup g : all) {
                if (g.__getProductgroupsids() <= 0) {
                    rootGroup = g;
                    break;
                }
            }
        }
        if (rootGroup == null) {
            rootGroup = new ProductGroup(Messages.GROUPNAMES.toString());
            rootGroup.setIDS(-1);
        }
        for (ProductGroup g : all) {
            if (g.__getIDS() == rootGroup.__getIDS()) {
                continue;
            }
            int pid = g.__getProductgroupsids();
            if (pid <= 0 || pid == g.__getIDS() || !byId.containsKey(pid)) {
                pid = rootGroup.__getIDS();
            }
            children.computeIfAbsent(pid, k -> new ArrayList<ProductGroup>()).add(g);
        }
        Comparator<ProductGroup> byName = Comparator.comparing(g -> String.valueOf(g.__getCname()), String.CASE_INSENSITIVE_ORDER);
        for (List<ProductGroup> l : children.values()) {
            l.sort(byName);
        }
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootGroup);
        buildNodes(rootNode, rootGroup.__getIDS(), children, new HashSet<Integer>(Collections.singleton(rootGroup.__getIDS())));
        return new DefaultTreeModel(rootNode);
    }

    private static void buildNodes(DefaultMutableTreeNode parentNode, int parentId,
            Map<Integer, List<ProductGroup>> children, Set<Integer> seen) {
        for (ProductGroup child : children.getOrDefault(parentId, Collections.emptyList())) {
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
                if (uo instanceof ProductGroup && ((ProductGroup) uo).__getIDS() == groupId) {
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

    private void importf() {
        Wizard w = new Wizard(false);
        w.addPanel(new wizard_CSV_ProductGroups_Import_1(w));
        w.showWiz();
        DatabaseObject.clearCache(Context.getProductGroup());
        refresh();
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
        return "ProductGroups";
    }

    @Override
    public void reset() {
        if (!initialized) {
            return;
        }
        if (dataOwner != null && dataOwner.isExisting()) {
            try {
                ProductGroup fresh = (ProductGroup) DatabaseObject.getObject(Context.getProductGroup(), dataOwner.__getIDS(), true);
                setDataOwner(fresh, true);
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
            }
        } else {
            newProductGroup();
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
        productgroupsids_ = (dataOwner != null && dataOwner.isExisting() && dataOwner.__getIDS() == 1)
                ? 0 : selectedComboId(parentCombo, 1);
        group_ = Group.getGroupOrDefault(selectedComboId(groupCombo, 1));
        return true;
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean p) {
        if (!(object instanceof ProductGroup)) {
            return;
        }
        dataOwner = (ProductGroup) object;
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
        int parentId = dataOwner != null ? dataOwner.__getProductgroupsids() : 1;
        int userGroupId = dataOwner != null ? dataOwner.__getGroupsids() : 1;
        reloadParentCombo(curId, parentId);
        reloadUserGroupCombo(userGroupId);
        parentCombo.setEnabled(!isRoot);
        if (isRoot) {
            pathField.setText(File.separator);
        } else {
            pathField.setText(hierarchypath_ != null && !hierarchypath_.isEmpty()
                    ? hierarchypath_ : computePathForParent(selectedComboId(parentCombo, 1)));
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
            if (dbo.getDbIdentity().equals(Context.getProductGroup().getDbIdentity())) {
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
