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
package mpv5.ui.dialogs.subcomponents;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import mpv5.data.PropertyStore;
import mpv5.db.common.Context;
import mpv5.db.common.DatabaseObject;
import mpv5.db.common.DatabaseSearch;
import mpv5.db.common.NodataFoundException;
import mpv5.db.objects.Group;
import mpv5.db.objects.Item;
import mpv5.db.objects.User;
import mpv5.globals.Messages;
import mpv5.handler.UserService;
import mpv5.i18n.LanguageManager;
import mpv5.logging.Log;
import mpv5.ui.dialogs.ControlApplet;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.Card;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;
import mpv5.ui.modern.ResponsiveGrid;
import mpv5.ui.panels.DataPanel;
import mpv5.usermanagement.MPSecurityManager;
import mpv5.utils.date.DateConverter;
import mpv5.utils.models.MPComboBoxModelItem;
import mpv5.utils.text.MD5HashGenerator;
import mpv5.utils.ui.TextFieldUtils;

/**
 * User administration: the users on the left, the selected one in cards on
 * the right (sign-in, rights, defaults). New users get their own button; the
 * rules (required fields, password, last administrator) are in
 * {@link UserService}.
 */
public class ControlPanel_Users extends JPanel implements ControlApplet, DataPanel {

    private static final long serialVersionUID = 2L;
    /**
     * This unique name identifies this control applet
     */
    public final String UNAME = "users";
    private User dataOwner;

    private final DefaultListModel<User> users = new DefaultListModel<User>();
    private final JList<User> list = new JList<User>(users);
    private final JLabel title = new JLabel();
    private final JTextField cname = new JTextField();
    private final JTextField fullname = new JTextField();
    private final JTextField mail = new JTextField();
    private final JPasswordField password = new JPasswordField();
    private final JPasswordField password2 = new JPasswordField();
    private final JLabel passwordHint = hint("");
    private final JCheckBox enabled = new JCheckBox(Messages.USERS_ENABLED.toString());
    private final JComboBox<MPComboBoxModelItem> inthighestright = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox<MPComboBoxModelItem> groupname = new JComboBox<MPComboBoxModelItem>();
    private final JCheckBox rgroups = new JCheckBox(Messages.USERS_RESTRICT_GROUP.toString());
    private final JComboBox<MPComboBoxModelItem> companyselect = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox language = new JComboBox();
    private final JComboBox locale = new JComboBox();
    private final JComboBox countrylist = new JComboBox();
    private final JComboBox<MPComboBoxModelItem> accountlist = new JComboBox<MPComboBoxModelItem>();
    private final JComboBox statuslist = new JComboBox();
    private final JComboBox<MPComboBoxModelItem> laf = new JComboBox<MPComboBoxModelItem>();
    private final JLabel lastLogin = new JLabel();
    private final JPanel actions = new JPanel(new BorderLayout());
    private boolean loading;

    public ControlPanel_Users() {
        if (MPSecurityManager.checkAdminAccess()) {
            build();
            refresh();
            setVisible(true);
        }
    }

    public ControlPanel_Users(User user) {
        if (MPSecurityManager.checkAdminAccess()) {
            build();
            refresh();
            select(user);
            setVisible(true);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Layout">
    private void build() {
        setLayout(new BorderLayout(ModernTheme.px(12), 0));
        setBackground(ModernTheme.background());
        setBorder(ModernTheme.padding(10, 10));

        //left: the users
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new UserRenderer());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !loading && list.getSelectedValue() != null) {
                load(list.getSelectedValue().__getIDS());
            }
        });
        JButton add = new JButton("+ " + Messages.USERS_NEW);
        ModernTheme.actionButton(add, null);
        add.addActionListener(e -> newUser());
        Card left = new Card(Messages.USERS_LIST.toString()).headerAction(add);
        JScrollPane sl = new JScrollPane(list);
        sl.setBorder(BorderFactory.createLineBorder(ModernTheme.border()));
        left.addFull(sl, 1);
        left.setPreferredSize(new Dimension(ModernTheme.px(300), ModernTheme.px(420)));
        add(left, BorderLayout.WEST);

        //right: the user
        title.setFont(ModernTheme.font(8, Font.BOLD));
        title.setBorder(BorderFactory.createEmptyBorder(0, ModernTheme.px(4), ModernTheme.px(6), 0));
        ResponsiveGrid grid = new ResponsiveGrid(ModernTheme.px(340), 2);
        for (JComponent c : new JComponent[]{cname, fullname, mail, password, password2}) {
            ModernTheme.roomy((javax.swing.text.JTextComponent) c);
        }
        enabled.setOpaque(false);
        rgroups.setOpaque(false);
        grid.add(new Card(Messages.USERS_SIGN_IN.toString())
                .row(Card.field(Messages.USERS_LOGIN.toString(), cname), Card.field(Messages.USERS_FULLNAME.toString(), fullname))
                .row(Card.field(Messages.USERS_MAIL.toString(), mail))
                .row(Card.field(Messages.USERS_PASSWORD.toString(), password), Card.field(Messages.USERS_PASSWORD_REPEAT.toString(), password2))
                .addFull(passwordHint)
                .addFull(enabled));
        grid.add(new Card(Messages.USERS_RIGHTS.toString())
                .row(Card.field(Messages.USERS_ROLE.toString(), inthighestright))
                .addFull(hint(Messages.USERS_ROLE_HINT.toString()))
                .row(Card.field(Messages.USERS_GROUP.toString(), groupname))
                .addFull(rgroups)
                .row(Card.field(Messages.USERS_COMPANY.toString(), companyselect)));
        grid.add(new Card(Messages.USERS_DEFAULTS.toString())
                .row(Card.field(Messages.USERS_LANGUAGE.toString(), language), Card.field(Messages.USERS_LOCALE.toString(), locale))
                .row(Card.field(Messages.USERS_COUNTRY.toString(), countrylist), Card.field(Messages.USERS_ACCOUNT.toString(), accountlist))
                .row(Card.field(Messages.USERS_STATUS.toString(), statuslist), Card.field(Messages.USERS_LAF.toString(), laf)));
        grid.add(new Card(Messages.USERS_INFO.toString()).addFull(lastLogin));
        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        right.add(title, BorderLayout.NORTH);
        right.add(grid, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);

        //bottom: delete apart, reset, save highlighted
        JButton delete = new JButton(Messages.USERS_DELETE.toString());
        delete.setForeground(new Color(185, 28, 28));
        delete.addActionListener(e -> delete());
        JButton reset = new JButton(Messages.USERS_RESET.toString());
        reset.addActionListener(e -> reset());
        PrimaryButton save = new PrimaryButton(Messages.USERS_SAVE.toString(), "/images/16/filesave.png");
        save.addActionListener(e -> save());
        JPanel l = new JPanel(new FlowLayout(FlowLayout.LEFT, ModernTheme.px(8), ModernTheme.px(6)));
        l.setOpaque(false);
        l.add(delete);
        JPanel r = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), ModernTheme.px(6)));
        r.setOpaque(false);
        r.add(reset);
        r.add(save);
        actions.setOpaque(false);
        actions.add(l, BorderLayout.WEST);
        actions.add(r, BorderLayout.EAST);
    }

    private static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ModernTheme.font(-1, Font.PLAIN));
        l.setForeground(ModernTheme.muted());
        return l;
    }

    /**
     * Name, login and role in the list; disabled users are gray
     */
    private static final class UserRenderer extends DefaultListCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
            super.getListCellRendererComponent(l, value, index, selected, false);
            User u = (User) value;
            String name = u.__getFullname() == null || u.__getFullname().trim().isEmpty() ? u.__getCname() : u.__getFullname();
            String muted = selected ? "" : " color='#6b7280'";
            String state = u.__getIsenabled() ? "" : " · " + Messages.USERS_DISABLED;
            setText("<html><b>" + esc(name) + "</b><br><font" + muted + ">" + esc(u.__getCname()) + " · "
                    + esc(roleName(u.__getInthighestright())) + esc(state) + "</font></html>");
            setBorder(BorderFactory.createEmptyBorder(ModernTheme.px(6), ModernTheme.px(8), ModernTheme.px(6), ModernTheme.px(8)));
            if (!u.__getIsenabled() && !selected) {
                setForeground(ModernTheme.muted());
            }
            return this;
        }
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;");
    }

    /**
     * @return the role with a short explanation
     */
    static String roleName(int right) {
        switch (right) {
            case MPSecurityManager.RIGHT_TO_ADMINISTRATE:
                return Messages.USERS_ROLE_ADMIN.toString();
            case MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE:
                return Messages.USERS_ROLE_USER.toString();
            case MPSecurityManager.RIGHT_TO_EDIT:
                return Messages.USERS_ROLE_EDIT.toString();
            case MPSecurityManager.RIGHT_TO_EXPORT:
                return Messages.USERS_ROLE_EXPORT.toString();
            default:
                return Messages.USERS_ROLE_VIEW.toString();
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Actions">
    private void load(int id) {
        try {
            setDataOwner(DatabaseObject.getObject(Context.getUser(), id), true);
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
    }

    private void select(User u) {
        for (int i = 0; i < users.size(); i++) {
            if (u != null && users.get(i).__getIDS().intValue() == u.__getIDS().intValue()) {
                list.setSelectedIndex(i);
                return;
            }
        }
        if (!users.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    private void newUser() {
        loading = true;
        list.clearSelection();
        loading = false;
        User u = new User();
        User me = User.getCurrentUser();
        //defaults of the current user: language, locale, country, group, account
        u.setLanguage(me.__getLanguage());
        u.setLocale(me.__getLocale());
        u.setDefcountry(me.__getDefcountry());
        u.setIsenabled(true);
        u.setInthighestright(MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE);
        u.setIntdefaultaccount(me.__getIntdefaultaccount());
        u.setIntdefaultstatus(me.__getIntdefaultstatus());
        u.setGroupsids(me.__getGroupsids());
        u.setDateadded(new Date());
        setDataOwner(u, true);
        cname.requestFocusInWindow();
    }

    private void save() {
        if (dataOwner == null || !collectData()) {
            showRequiredFields();
            return;
        }
        List<UserService.Account> others = new ArrayList<UserService.Account>();
        for (int i = 0; i < users.size(); i++) {
            others.add(account(users.get(i)));
        }
        int id = dataOwner.isExisting() ? dataOwner.__getIDS() : -1;
        List<String> problems = UserService.check(new UserService.Account(id, cname_, inthighestright_, isenabled_),
                password.getPassword(), password2.getPassword(), others);
        if (!problems.isEmpty()) {
            Popup.notice(problems, Messages.USERS_NOT_SAVED.toString());
            return;
        }
        if (password.getPassword().length > 0) {
            try {
                password_ = MD5HashGenerator.getInstance().hashData(password.getPassword());
            } catch (NoSuchAlgorithmException ex) {
                Log.Debug(ex);
                return;
            }
        }
        User before = dataOwner.isExisting() ? User.getCurrentUser() : null;
        boolean me = before != null && before.__getIDS().intValue() == dataOwner.__getIDS().intValue();
        String look = me ? before.__getLanguage() + "|" + before.__getLocale() + "|" + before.__getLaf() : "";
        dataOwner.getPanelData(this);
        if (!dataOwner.isExisting()) {
            dataOwner.setIDS(-1);
        }
        if (dataOwner.save()) {
            final User saved = dataOwner;
            refreshList();
            select(saved);
            if (me) {
                User.getCurrentUser().reset();
                if (!look.equals(language_ + "|" + locale_ + "|" + laf_)) {
                    Popup.notice(Messages.RESTART_REQUIRED);
                }
            }
            mpv5.YabsViewProxy.instance().addMessage(Messages.USERS_SAVED + ": " + saved.__getCname());
        } else {
            showRequiredFields();
        }
    }

    private void delete() {
        if (dataOwner == null) {
            return;
        }
        List<UserService.Account> others = new ArrayList<UserService.Account>();
        for (int i = 0; i < users.size(); i++) {
            others.add(account(users.get(i)));
        }
        String why = UserService.whyNotDelete(account(dataOwner), User.getCurrentUser().__getIDS(), others);
        if (why != null) {
            Popup.notice(why);
            return;
        }
        if (Popup.Y_N_dialog(String.format(Messages.USERS_DELETE_CONFIRM.toString(), dataOwner.__getCname()))) {
            dataOwner.delete();
            refreshList();
            select(null);
        }
    }

    private static UserService.Account account(User u) {
        return new UserService.Account(u.isExisting() ? u.__getIDS() : -1, u.__getCname(), u.__getInthighestright(), u.__getIsenabled());
    }
    // </editor-fold>

    @Override
    public void setValues(PropertyStore values) {
    }

    @Override
    public String getUname() {
        return UNAME;
    }

    @Override
    public void reset() {
        if (dataOwner != null && dataOwner.isExisting()) {
            load(dataOwner.__getIDS());
        } else {
            newUser();
        }
    }
    public String laf_;
    public String language_ = "buildin_en";
    public String locale_ = "en_US";
    public String defcountry_ = "";
    public String fullname_;
    public String mail_;
    public String cname_;
    public String password_;
    public boolean isenabled_;
    public boolean isloggedin_;
    public boolean isrgrouped_;
    public int inthighestright_ = 9;
    public int intdefaultaccount_ = 9;
    public int intdefaultstatus_;
    public int intaddedby_ = 4343;
    public Date datelastlog_ = new Date();
    public int ids_;
    public Group group_;
    public int compsids_;
    public java.util.Date dateadded_ = new java.util.Date();

    @Override
    public boolean collectData() {
        cname_ = cname.getText().trim();
        if (cname_.isEmpty()) {
            return false;
        }
        MPComboBoxModelItem l = (MPComboBoxModelItem) laf.getSelectedItem();
        laf_ = l == null ? "" : String.valueOf(l.getId());
        if (language.getSelectedItem() != null) {
            language_ = String.valueOf(((MPComboBoxModelItem) language.getSelectedItem()).getId());
        }
        if (countrylist.getSelectedItem() != null) {
            defcountry_ = ((MPComboBoxModelItem) countrylist.getSelectedItem()).getId();
        }
        if (locale.getSelectedItem() != null) {
            locale_ = String.valueOf(((MPComboBoxModelItem) locale.getSelectedItem()).getId());
        }
        compsids_ = companyselect.getSelectedItem() != null
                ? Integer.parseInt(((MPComboBoxModelItem) companyselect.getSelectedItem()).getId()) : 0;
        fullname_ = fullname.getText().trim();
        mail_ = mail.getText().trim();
        isenabled_ = enabled.isSelected();
        if (inthighestright.getSelectedItem() != null) {
            inthighestright_ = Integer.parseInt(((MPComboBoxModelItem) inthighestright.getSelectedItem()).getId());
        }
        if (groupname.getSelectedItem() != null) {
            try {
                group_ = (Group) DatabaseObject.getObject(Context.getGroup(),
                        Integer.parseInt(((MPComboBoxModelItem) groupname.getSelectedItem()).getId()));
            } catch (NodataFoundException ex) {
                Log.Debug(this, ex);
                group_ = Group.getDefault();
            }
        }
        intdefaultaccount_ = accountlist.getSelectedItem() != null
                ? Integer.parseInt(((MPComboBoxModelItem) accountlist.getSelectedItem()).getId()) : 1;
        intdefaultstatus_ = statuslist.getSelectedItem() != null
                ? Integer.parseInt(String.valueOf(((MPComboBoxModelItem) statuslist.getSelectedItem()).getId())) : 1;
        isrgrouped_ = rgroups.isSelected();
        return true;
    }

    @Override
    public void exposeData() {
        boolean existing = dataOwner != null && dataOwner.isExisting();
        title.setText(existing ? (fullname_ == null || fullname_.isEmpty() ? cname_ : fullname_) : Messages.USERS_NEW.toString());
        cname.setText(cname_);
        fullname.setText(fullname_);
        mail.setText(mail_);
        //never show or keep a typed password of another user
        password.setText("");
        password2.setText("");
        passwordHint.setText(existing ? Messages.USERS_PASSWORD_KEEP.toString()
                : String.format(Messages.USERS_PASSWORD_NEW.toString(), UserService.MIN_PASSWORD));
        enabled.setSelected(isenabled_);
        rgroups.setSelected(isrgrouped_);
        selectId(inthighestright, String.valueOf(inthighestright_));
        if (group_ != null) {
            selectId(groupname, String.valueOf(group_.__getIDS()));
        }
        selectId(companyselect, String.valueOf(compsids_));
        selectId(accountlist, String.valueOf(intdefaultaccount_));
        selectId(statuslist, String.valueOf(intdefaultstatus_));
        selectId(language, language_);
        selectId(locale, locale_);
        selectId(countrylist, defcountry_);
        selectId(laf, laf_ == null ? "" : laf_);
        lastLogin.setText(existing
                ? Messages.USERS_LAST_LOGIN + " " + DateConverter.getDefDateString(datelastlog_)
                + (isloggedin_ ? "  ·  " + Messages.USERS_LOGGED_IN : "")
                : " ");
    }

    /**
     * Selects the item with the id; nothing (or the first) if it is missing
     */
    private static void selectId(JComboBox box, String id) {
        for (int i = 0; i < box.getItemCount(); i++) {
            Object o = box.getItemAt(i);
            if (o instanceof MPComboBoxModelItem && String.valueOf(((MPComboBoxModelItem) o).getId()).equals(id)) {
                box.setSelectedIndex(i);
                return;
            }
        }
    }

    @Override
    public DatabaseObject getDataOwner() {
        return dataOwner;
    }

    @Override
    public void setDataOwner(DatabaseObject object, boolean p) {
        dataOwner = (User) object;
        if (p) {
            dataOwner.setPanelData(this);
            this.exposeData();
        }
    }

    private void refreshList() {
        loading = true;
        users.clear();
        try {
            for (DatabaseObject o : DatabaseObject.getObjects(Context.getUser())) {
                users.addElement((User) o);
            }
        } catch (NodataFoundException ex) {
            Log.Debug(this, ex);
        }
        loading = false;
    }

    @Override
    public void refresh() {
        try {
            //readable names (English, Deutsch, ...) instead of the file names of the imports
            List<MPComboBoxModelItem> langs = new ArrayList<MPComboBoxModelItem>();
            for (String[] l : LanguageManager.choices()) {
                if (!l[0].startsWith("import:")) {
                    langs.add(new MPComboBoxModelItem(l[0], l[1]));
                }
            }
            javax.swing.ComboBoxModel all = LanguageManager.getLanguagesAsComboBoxModel();
            for (int i = 0; i < all.getSize(); i++) {
                MPComboBoxModelItem it = (MPComboBoxModelItem) all.getElementAt(i);
                boolean listed = false;
                for (MPComboBoxModelItem x : langs) {
                    listed |= String.valueOf(x.getId()).equals(String.valueOf(it.getId()));
                }
                if (!listed) {
                    //older imports, e.g. of a previous version
                    langs.add(new MPComboBoxModelItem(String.valueOf(it.getId()),
                            String.valueOf(it.getValue()).toLowerCase().startsWith("de-") ? "Deutsch (" + Messages.USERS_OLDER + ")" : String.valueOf(it.getValue())));
                }
            }
            language.setModel(new DefaultComboBoxModel(langs.toArray(new MPComboBoxModelItem[0])));
            locale.setModel(LanguageManager.getLocalesAsComboBoxModel());
            countrylist.setModel(LanguageManager.getCountriesAsComboBoxModel());
        } catch (Exception e) {
            Log.Debug(this, e);
        }
        List<MPComboBoxModelItem> roles = new ArrayList<MPComboBoxModelItem>();
        for (int r : new int[]{MPSecurityManager.RIGHT_TO_ADMINISTRATE, MPSecurityManager.RIGHT_TO_CREATE_OR_DELETE,
            MPSecurityManager.RIGHT_TO_EDIT, MPSecurityManager.RIGHT_TO_EXPORT, MPSecurityManager.RIGHT_TO_VIEW}) {
            roles.add(new MPComboBoxModelItem(r, roleName(r)));
        }
        inthighestright.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(roles.toArray(new MPComboBoxModelItem[0])));
        groupname.setModel(new DefaultComboBoxModel(
                MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getGroup()).getValuesFor(Context.getGroup().getSubID()))));
        MPComboBoxModelItem[] comps = MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getCompany()).getValuesFor(Context.getCompany().getSubID()));
        List<MPComboBoxModelItem> c = new ArrayList<MPComboBoxModelItem>();
        c.add(new MPComboBoxModelItem(0, "—"));
        for (MPComboBoxModelItem i : comps == null ? new MPComboBoxModelItem[0] : comps) {
            c.add(i);
        }
        companyselect.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(c.toArray(new MPComboBoxModelItem[0])));
        accountlist.setModel(new DefaultComboBoxModel(
                MPComboBoxModelItem.toItems(new DatabaseSearch(Context.getAccounts()).getValuesFor(Context.getAccounts().getSubID()))));
        statuslist.setModel(MPComboBoxModelItem.toModel(Item.getStatusStrings()));
        //look: the default (modern or classic, View > Layout) or a look and feel of its own
        List<MPComboBoxModelItem> looks = new ArrayList<MPComboBoxModelItem>();
        looks.add(new MPComboBoxModelItem("", Messages.USERS_LAF_DEFAULT.toString()));
        for (UIManager.LookAndFeelInfo i : UIManager.getInstalledLookAndFeels()) {
            looks.add(new MPComboBoxModelItem(i.getClassName(), i.getName()));
        }
        laf.setModel(new DefaultComboBoxModel<MPComboBoxModelItem>(looks.toArray(new MPComboBoxModelItem[0])));
        refreshList();
        select(dataOwner != null && dataOwner.isExisting() ? dataOwner : User.getCurrentUser());
    }

    @Override
    public void paste(DatabaseObject... dbos) {
        for (DatabaseObject dbo : dbos) {
            if (dbo.getDbIdentity().equals(Context.getUser().getDbIdentity())) {
                select((User) dbo);
            } else {
                mpv5.YabsViewProxy.instance().addMessage(Messages.NOT_POSSIBLE.toString() + Messages.ACTION_PASTE, Color.RED);
            }
        }
    }

    @Override
    public void showRequiredFields() {
        TextFieldUtils.blinkerRed(cname);
        cname.requestFocusInWindow();
    }

    @Override
    public void showSearchBar(boolean show) {
    }

    @Override
    public Component getAndRemoveActionPanel() {
        return MPSecurityManager.checkAdminAccess() ? actions : null;
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
