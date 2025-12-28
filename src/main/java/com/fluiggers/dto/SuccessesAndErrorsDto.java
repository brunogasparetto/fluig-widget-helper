package com.fluiggers.dto;

import java.util.ArrayList;
import java.util.List;

public class SuccessesAndErrorsDto {
    private List<String> successes;
    private List<String> errors;

    public SuccessesAndErrorsDto() {
        successes = new ArrayList<>();
        errors = new ArrayList<>();
    }

    public void addSuccess(String success) {
        successes.add(success);
    }

    public List<String> getSuccesses() {
        return successes;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public List<String> getErrors() {
        return errors;
    }
}
