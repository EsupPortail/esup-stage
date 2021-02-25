package fr.esupportail.esupstage.domain.jpa.entities;


import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The persistent class for the NAF_N1 database table.
 *
 */
@Entity
@Table(name = "NAF_N1")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "NafN1.findAll", query = "SELECT n FROM NafN1 n")
public class NafN1 implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "codeNAF_N1", unique = true, nullable = false, length = 1)
    private String code;
    @Column(name = "libelleNAF_N1", nullable = false, length = 150)
    private String label;
    @Column(name = "temEnServNAF_N1", nullable = false, length = 1)
    private String temEnServ;
    // bi-directional many-to-one association to NafN5
    @OneToMany(mappedBy = "nafN1")
    private List<NafN5> nafN5s;

    public NafN5 addNafN5(NafN5 nafN5) {
        getNafN5s().add(nafN5);
        nafN5.setNafN1(this);
        return nafN5;
    }

    public NafN5 removeNafN5(NafN5 nafN5) {
        getNafN5s().remove(nafN5);
        nafN5.setNafN1(null);
        return nafN5;
    }
}
