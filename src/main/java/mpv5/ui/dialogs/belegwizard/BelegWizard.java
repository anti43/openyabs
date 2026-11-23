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
package mpv5.ui.dialogs.belegwizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import mpv5.YabsViewProxy;
import mpv5.db.objects.Item;
import mpv5.globals.Messages;
import mpv5.logging.Log;
import mpv5.ui.dialogs.Popup;
import mpv5.ui.modern.ModernTheme;
import mpv5.ui.modern.PrimaryButton;

/**
 * Step-by-step "Beleg-Wizard" dialog guiding the user through creating a
 * document on four dedicated panels:
 * <ol>
 * <li>Document type</li>
 * <li>Receiver</li>
 * <li>Positions / items</li>
 * <li>Preview &amp; save</li>
 * </ol>
 * Runs modeless (owned by the main window) so helper dialogs opened by
 * {@link mpv5.ui.panels.ItemPositionsTable} (text editor, product picker) are
 * never blocked. On finish, saves the document and opens it in the normal
 * document tab ready for export.
 */
public class BelegWizard extends JDialog {

    private static final long serialVersionUID = 1L;

    public static final int STEP_TYPE = 0;
    public static final int STEP_RECEIVER = 1;
    public static final int STEP_ITEMS = 2;
    public static final int STEP_PREVIEW = 3;

    private static final String[] CARD_KEYS = {"type", "receiver", "items", "preview"};

    private final BelegWizardState state = new BelegWizardState();
    private final BelegTypeStep typeStep;
    private final BelegReceiverStep receiverStep;
    private final BelegItemsStep itemsStep;
    private final BelegPreviewStep previewStep;

    private final CardLayout stepCards = new CardLayout();
    private final JPanel stepContainer = new JPanel(stepCards);
    private final JLabel[] stepLabels = new JLabel[4];
    private final JLabel validationLabel = new JLabel(" ");
    private final JButton cancelButton = new JButton(Messages.BELEG_WIZARD_CANCEL.toString());
    private final JButton backButton = new JButton("\u2190 " + Messages.BELEG_WIZARD_BACK.toString());
    private final PrimaryButton nextButton = new PrimaryButton(Messages.BELEG_WIZARD_NEXT.toString() + " \u2192", null);

    private int currentStep = STEP_TYPE;
    private Item savedItem;
    private boolean cancelled;

    /**
     * Opens a new modeless Beleg-Wizard centered over the main YaBS window.
     */
    public static BelegWizard showWizard() {
        Window owner = null;
        try {
            owner = YabsViewProxy.instance().getIdentifierFrame();
        } catch (Exception ignored) {
        }
        BelegWizard wizard = new BelegWizard(owner);
        wizard.setLocationRelativeTo(owner);
        wizard.setVisible(true);
        return wizard;
    }

    public BelegWizard(Window owner) {
        super(owner, Messages.BELEG_WIZARD_TITLE.toString(), ModalityType.MODELESS);
        setName("belegWizard");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelWithConfirmation();
            }
        });

        typeStep = new BelegTypeStep(state, this::goNext);
        receiverStep = new BelegReceiverStep(state, () -> clearError(), this::goNext);
        itemsStep = new BelegItemsStep(state);
        previewStep = new BelegPreviewStep(state, itemsStep.getPositionsTable());

        JPanel root = new JPanel(new BorderLayout(0, ModernTheme.px(8)));
        root.setBackground(ModernTheme.background());
        root.setBorder(ModernTheme.padding(12, 16));

        root.add(buildStepperBar(), BorderLayout.NORTH);

        stepContainer.setOpaque(false);
        stepContainer.add(typeStep, CARD_KEYS[STEP_TYPE]);
        stepContainer.add(receiverStep, CARD_KEYS[STEP_RECEIVER]);
        stepContainer.add(itemsStep, CARD_KEYS[STEP_ITEMS]);
        stepContainer.add(previewStep, CARD_KEYS[STEP_PREVIEW]);
        root.add(stepContainer, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        setMinimumSize(new Dimension(ModernTheme.px(780), ModernTheme.px(540)));
        setPreferredSize(new Dimension(ModernTheme.px(920), ModernTheme.px(640)));
        pack();

        getRootPane().registerKeyboardAction(
                e -> cancelWithConfirmation(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        showStep(STEP_TYPE);
    }

    private JPanel buildStepperBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, ModernTheme.px(8), 0));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.border()),
                BorderFactory.createEmptyBorder(0, 0, ModernTheme.px(10), 0)));
        String[] titles = new String[]{
            Messages.BELEG_WIZARD_STEP_TYPE.toString(),
            Messages.BELEG_WIZARD_STEP_RECEIVER.toString(),
            Messages.BELEG_WIZARD_STEP_ITEMS.toString(),
            Messages.BELEG_WIZARD_STEP_PREVIEW.toString()
        };
        for (int i = 0; i < titles.length; i++) {
            JLabel l = new JLabel(titles[i], SwingConstants.CENTER);
            l.setOpaque(true);
            l.setBorder(ModernTheme.padding(7, 10));
            stepLabels[i] = l;
            bar.add(l);
        }
        return bar;
    }

    private JPanel buildFooter() {
        validationLabel.setForeground(new Color(185, 28, 28));
        validationLabel.setFont(ModernTheme.font(0, Font.BOLD));

        cancelButton.setName("belegWizardCancel");
        cancelButton.addActionListener(e -> cancelWithConfirmation());
        backButton.setName("belegWizardBack");
        backButton.addActionListener(e -> goBack());
        nextButton.setName("belegWizardNext");
        nextButton.addActionListener(e -> {
            if (currentStep == STEP_PREVIEW) {
                finishAndSave();
            } else {
                goNext();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(cancelButton);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, ModernTheme.px(8), 0));
        right.setOpaque(false);
        right.add(backButton);
        right.add(nextButton);

        JPanel footer = new JPanel(new BorderLayout(ModernTheme.px(12), 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.border()),
                BorderFactory.createEmptyBorder(ModernTheme.px(10), 0, 0, 0)));
        footer.add(left, BorderLayout.WEST);
        footer.add(validationLabel, BorderLayout.CENTER);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void showStep(int step) {
        currentStep = step;
        clearError();
        stepCards.show(stepContainer, CARD_KEYS[step]);
        updateStepperUI();
        backButton.setEnabled(step > STEP_TYPE);
        if (step == STEP_PREVIEW) {
            nextButton.setText(Messages.BELEG_WIZARD_SAVE_OPEN.toString());
        } else {
            nextButton.setText(Messages.BELEG_WIZARD_NEXT.toString() + " \u2192");
        }
        if (step == STEP_RECEIVER) {
            receiverStep.loadContacts();
            receiverStep.focusSearch();
        } else if (step == STEP_ITEMS) {
            itemsStep.onEnter();
        } else if (step == STEP_PREVIEW) {
            previewStep.onEnter();
        }
    }

    private void updateStepperUI() {
        Color accent = ModernTheme.accent();
        Color doneBg = ModernTheme.accentSoft();
        Color idleBg = ModernTheme.cardBackground();
        Color muted = ModernTheme.muted();
        Color fg = ModernTheme.foreground();

        for (int i = 0; i < stepLabels.length; i++) {
            JLabel l = stepLabels[i];
            if (i == currentStep) {
                l.setBackground(accent);
                l.setForeground(Color.WHITE);
                l.setFont(ModernTheme.font(0, Font.BOLD));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 1),
                        ModernTheme.padding(6, 10)));
            } else if (i < currentStep) {
                l.setBackground(doneBg);
                l.setForeground(fg);
                l.setFont(ModernTheme.font(0, Font.BOLD));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ModernTheme.border(), 1),
                        ModernTheme.padding(6, 10)));
            } else {
                l.setBackground(idleBg);
                l.setForeground(muted);
                l.setFont(ModernTheme.font(0, Font.PLAIN));
                l.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ModernTheme.border(), 1),
                        ModernTheme.padding(6, 10)));
            }
        }
    }

    /**
     * Validates the current step and advances to the next step if valid.
     *
     * @return {@code true} if navigation succeeded
     */
    public boolean goNext() {
        if (currentStep == STEP_TYPE) {
            showStep(STEP_RECEIVER);
            return true;
        }
        if (currentStep == STEP_RECEIVER) {
            if (state.getReceiver() == null || !state.getReceiver().isExisting()) {
                showError(Messages.BELEG_WIZARD_RECEIVER_NONE.toString());
                return false;
            }
            showStep(STEP_ITEMS);
            return true;
        }
        if (currentStep == STEP_ITEMS) {
            itemsStep.commitToState();
            if (!itemsStep.hasPositions()) {
                showError(Messages.BELEG_WIZARD_ITEMS_NONE.toString());
                return false;
            }
            showStep(STEP_PREVIEW);
            return true;
        }
        return false;
    }

    /**
     * Navigates one step backwards while preserving all entered data.
     *
     * @return {@code true} if navigation succeeded
     */
    public boolean goBack() {
        if (currentStep <= STEP_TYPE) {
            return false;
        }
        if (currentStep == STEP_ITEMS) {
            itemsStep.commitToState();
        } else if (currentStep == STEP_PREVIEW) {
            previewStep.commitToState();
        }
        showStep(currentStep - 1);
        return true;
    }

    /**
     * Saves the document and its positions, closes the wizard, and opens the
     * saved document in the normal YaBS document view ready to be exported.
     *
     * @return the saved {@link Item}, or {@code null} on validation/save error
     */
    public Item finishAndSave() {
        previewStep.commitToState();
        if (state.getReceiver() == null || !state.getReceiver().isExisting()) {
            showError(Messages.BELEG_WIZARD_RECEIVER_NONE.toString());
            return null;
        }
        if (!itemsStep.hasPositions()) {
            showError(Messages.BELEG_WIZARD_ITEMS_NONE.toString());
            return null;
        }
        try {
            savedItem = BelegWizardService.saveDocument(state, itemsStep.getPositionsTable());
            dispose();
            openInNormalView(savedItem);
            return savedItem;
        } catch (Exception ex) {
            Log.Debug(this, ex);
            showError(String.valueOf(ex.getMessage()));
            return null;
        }
    }

    private void openInNormalView(Item item) {
        if (item == null) {
            return;
        }
        try {
            if (YabsViewProxy.instance().getIdentifierView() != null) {
                YabsViewProxy.instance().getIdentifierView().addTab(item, true);
                YabsViewProxy.instance().addMessage(String.format(Messages.BELEG_WIZARD_SAVED.toString(),
                        item.__getCnumber() != null && !item.__getCnumber().isEmpty() ? item.__getCnumber() : item.__getCname()));
            }
        } catch (Exception ex) {
            Log.Debug(this, ex);
        }
    }

    private void cancelWithConfirmation() {
        boolean hasData = state.getReceiver() != null || itemsStep.hasPositions()
                || !state.getTitle().trim().isEmpty() || !state.getNotes().trim().isEmpty();
        if (!hasData || Popup.Y_N_dialog(Messages.BELEG_WIZARD_CANCEL_CONFIRM.toString())) {
            cancelImmediately();
        }
    }

    /**
     * Closes the wizard without saving (also used by tests).
     */
    public void cancelImmediately() {
        cancelled = true;
        savedItem = null;
        dispose();
    }

    private void showError(String message) {
        validationLabel.setText(message == null ? " " : message);
    }

    private void clearError() {
        validationLabel.setText(" ");
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public String getValidationMessage() {
        return validationLabel.getText();
    }

    public BelegWizardState getState() {
        return state;
    }

    public BelegTypeStep getTypeStep() {
        return typeStep;
    }

    public BelegReceiverStep getReceiverStep() {
        return receiverStep;
    }

    public BelegItemsStep getItemsStep() {
        return itemsStep;
    }

    public BelegPreviewStep getPreviewStep() {
        return previewStep;
    }

    public Item getSavedItem() {
        return savedItem;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
