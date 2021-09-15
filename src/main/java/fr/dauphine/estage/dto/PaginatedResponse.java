package fr.dauphine.estage.dto;

import com.fasterxml.jackson.annotation.JsonView;
import fr.dauphine.estage.dto.view.Views;
import fr.dauphine.estage.model.PaginatedEntity;

import java.util.List;

public class PaginatedResponse {
    @JsonView(Views.List.class)
    private Long total;

    @JsonView(Views.List.class)
    private List<PaginatedEntity> data;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<PaginatedEntity> getData() {
        return data;
    }

    public void setData(List<PaginatedEntity> data) {
        this.data = data;
    }
}
