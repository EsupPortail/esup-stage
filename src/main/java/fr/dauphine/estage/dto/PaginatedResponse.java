package fr.dauphine.estage.dto;

import fr.dauphine.estage.model.PaginatedEntity;

import java.util.List;

public class PaginatedResponse {
    private Long total;
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
