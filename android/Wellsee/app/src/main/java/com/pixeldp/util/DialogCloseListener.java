package com.pixeldp.util;

import java.util.Calendar;

public interface DialogCloseListener {
    void handleDialogClose(Calendar startTime, Calendar endTime);
}