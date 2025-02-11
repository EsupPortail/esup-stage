package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;

@Entity
@Table(name = "TicketStructure")
public class TicketStructure {

    @Id
    @Column(nullable = false)
    private String ticket;

    @ManyToOne
    @JoinColumn(name = "idStructure", nullable = false)
    private Structure structure;

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }
}
