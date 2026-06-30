package org.esup_portail.esup_stage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Entity
@Table(name = "EvaluationTuteurToken")
@Data
@NoArgsConstructor
public class EvaluationTuteurToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idEvaluationTuteurToken", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "idConvention")
    private Convention convention;

    @ManyToOne
    @JoinColumn(name = "idTuteur")
    private Contact contact;

    @Column(name = "Token", nullable = false, unique = true, length = 1024)
    private String token;

    @Column(name = "DateExpiration", nullable = false, updatable = false)
    private Date expiresAt;

    @Column(name = "DateCreation", nullable = false, updatable = false)
    private Date createdAt;

    @Column(name = "Utilise")
    private Boolean utilise = false;

    @Column(name = "Revoque")
    private Boolean revoque = false;


    @Transient public boolean isExpired() { return expiresAt != null && expiresAt.before(new Date()); }
    @Transient public boolean isActive()  { return !utilise && !revoque && !isExpired(); }

    public EvaluationTuteurToken(Convention convention, Contact contact, Duration ttl,
                                 org.esup_portail.esup_stage.security.EvaluationJwtService jwtService) {

        if (convention == null) throw new IllegalArgumentException("convention is null");
        if (contact == null) throw new IllegalArgumentException("contact is null");
        if (ttl == null || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("ttl must be positive");

        this.convention = convention;
        this.contact = contact;

        Instant now = Instant.now();
        this.createdAt = Date.from(now);
        this.expiresAt = Date.from(now.plus(ttl));

        this.token = jwtService.createToken(convention.getId(), contact.getId(), now, now.plus(ttl));
    }
}
