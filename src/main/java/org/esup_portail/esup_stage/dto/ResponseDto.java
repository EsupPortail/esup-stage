package org.esup_portail.esup_stage.dto;

import java.util.ArrayList;
import java.util.List;

public class ResponseDto {
    private List<String> error = new ArrayList<>();
    private List<String> warning = new ArrayList<>();
    private List<String> success = new ArrayList<>();

    public List<String> getError() {
        return error;
    }

    public void setError(List<String> error) {
        this.error = error;
    }

    public List<String> getWarning() {
        return warning;
    }

    public void setWarning(List<String> warning) {
        this.warning = warning;
    }

    public List<String> getSuccess() {
        return success;
    }

    public void setSuccess(List<String> success) {
        this.success = success;
    }
}
