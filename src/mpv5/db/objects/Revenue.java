package mpv5.db.objects;

import mpv5.db.common.Context;

public class Revenue extends Expense {

    public Revenue() {
        context.setDbIdentity(Context.IDENTITY_REVENUE);
        context.setIdentityClass(this.getClass());
    }
}
