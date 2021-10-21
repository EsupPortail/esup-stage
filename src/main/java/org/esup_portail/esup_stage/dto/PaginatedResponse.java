package org.esup_portail.esup_stage.dto;

import com.fasterxml.jackson.annotation.JsonView;
import org.esup_portail.esup_stage.dto.view.Views;

import java.util.List;

public class PaginatedResponse<T> {
    @JsonView(Views.List.class)
    private Long total;

    @JsonView(Views.List.class)
    private List<T> data;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
