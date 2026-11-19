package mpv5.ui.dialogs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import mpv5.db.objects.Expense;
import mpv5.db.objects.Item;
import mpv5.db.objects.Revenue;
import org.junit.jupiter.api.Test;

class MarkPaidDialogTest {

    @Test
    void onlyUnpaidExpensesAndIncomeChange() {
        Expense open = new Expense();
        open.setBrutvalue(new BigDecimal("11.90"));
        assertTrue(MarkPaidDialog.plan(open).change);
        Expense paid = new Expense();
        paid.setIspaid(true);
        assertFalse(MarkPaidDialog.plan(paid).change);

        Revenue r = new Revenue();
        r.setStatus(Item.STATUS_QUEUED);
        assertTrue(MarkPaidDialog.plan(r).change);
        r.setStatus(Item.STATUS_CANCELLED);
        assertFalse(MarkPaidDialog.plan(r).change);
        r.setStatus(Item.STATUS_PAID);
        assertFalse(MarkPaidDialog.plan(r).change);
    }
}
