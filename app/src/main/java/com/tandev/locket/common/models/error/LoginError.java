package com.tandev.locket.common.models.error;

import com.tandev.locket.common.models.login.error.ErrorDetails;

public class LoginError {
    private ErrorDetails error;

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
}

