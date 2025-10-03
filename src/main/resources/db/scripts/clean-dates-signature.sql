START TRANSACTION;

CREATE TABLE IF NOT EXISTS backup_convention_dates AS
SELECT *
FROM Convention
WHERE IFNULL(temConventionSignee, 0) = 0
  AND (
    dateDepotEtudiant IS NOT NULL OR dateDepotEnseignant IS NOT NULL OR dateDepotTuteur IS NOT NULL
        OR dateDepotSignataire IS NOT NULL OR dateDepotViseur IS NOT NULL
        OR dateSignatureEtudiant IS NOT NULL OR dateSignatureEnseignant IS NOT NULL OR dateSignatureTuteur IS NOT NULL
        OR dateSignatureSignataire IS NOT NULL OR dateSignatureViseur IS NOT NULL
    );

-- Backup
SELECT * FROM backup_convention_dates;

DROP TABLE IF EXISTS __idConventionACorriger;

CREATE TABLE __idConventionACorriger
SELECT idConvention
FROM Convention
WHERE IFNULL(temConventionSignee, 0) = 0
  AND (
    dateDepotEtudiant IS NOT NULL OR dateDepotEnseignant IS NOT NULL OR dateDepotTuteur IS NOT NULL
        OR dateDepotSignataire IS NOT NULL OR dateDepotViseur IS NOT NULL
        OR dateSignatureEtudiant IS NOT NULL OR dateSignatureEnseignant IS NOT NULL OR dateSignatureTuteur IS NOT NULL
        OR dateSignatureSignataire IS NOT NULL OR dateSignatureViseur IS NOT NULL
    );

DROP VIEW IF EXISTS __vueConventionACorriger;
CREATE VIEW __vueConventionACorriger AS
SELECT idConvention,
       dateSignatureEtudiant,
       dateSignatureEnseignant,
       dateSignatureTuteur,
       dateSignatureSignataire,
       dateSignatureViseur
FROM Convention
WHERE idConvention IN (SELECT idConvention FROM __idConventionACorriger)
;
-- avant
SELECT * FROM __vueConventionACorriger;

UPDATE Convention c
    JOIN __idConventionACorriger t USING (idConvention)
SET c.dateDepotEtudiant = NULL,
    c.dateDepotEnseignant = NULL,
    c.dateDepotTuteur = NULL,
    c.dateDepotSignataire = NULL,
    c.dateDepotViseur = NULL,
    c.dateSignatureEtudiant = NULL,
    c.dateSignatureEnseignant = NULL,
    c.dateSignatureTuteur = NULL,
    c.dateSignatureSignataire = NULL,
    c.dateSignatureViseur = NULL,
    c.dateActualisationSignature = NULL;

-- apres
SELECT * FROM __vueConventionACorriger;

DROP TABLE IF EXISTS __idConventionACorriger;
DROP VIEW IF EXISTS __vueConventionACorriger;

-- COMMIT;
ROLLBACK;