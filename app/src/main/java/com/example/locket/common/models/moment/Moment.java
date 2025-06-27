package com.example.locket.common.models.moment;

import java.io.Serializable;

public class Moment implements Serializable {
    private Result result;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
