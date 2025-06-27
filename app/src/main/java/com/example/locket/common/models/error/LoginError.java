package com.example.locket.common.models.error;

import com.example.locket.common.models.login.error.ErrorDetails;

public class LoginError {
    private ErrorDetails error;

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }
}

