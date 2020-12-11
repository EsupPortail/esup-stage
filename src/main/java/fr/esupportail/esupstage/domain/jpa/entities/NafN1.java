package fr.esupportail.esupstage.domain.jpa.entities;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.List;

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
    @Column(unique = true, nullable = false, length = 1)
    private String codeNAF_N1;
    @Column(nullable = false, length = 150)
    private String libelleNAF_N1;
    @Column(nullable = false, length = 1)
    private String temEnServNAF_N1;
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
