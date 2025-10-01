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

DROP TABLE IF EXISTS tmp_ids_a_corriger;

CREATE TEMPORARY TABLE tmp_ids_a_corriger
SELECT idConvention
FROM Convention
WHERE IFNULL(temConventionSignee, 0) = 0
  AND (
    dateDepotEtudiant IS NOT NULL OR dateDepotEnseignant IS NOT NULL OR dateDepotTuteur IS NOT NULL
        OR dateDepotSignataire IS NOT NULL OR dateDepotViseur IS NOT NULL
        OR dateSignatureEtudiant IS NOT NULL OR dateSignatureEnseignant IS NOT NULL OR dateSignatureTuteur IS NOT NULL
        OR dateSignatureSignataire IS NOT NULL OR dateSignatureViseur IS NOT NULL
    );

UPDATE Convention c
    JOIN tmp_ids_a_corriger t USING (idConvention)
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

SELECT c.*
FROM Convention c
         JOIN tmp_ids_a_corriger t USING (idConvention)
ORDER BY c.idConvention;

DROP TABLE IF EXISTS tmp_ids_a_corriger;

-- COMMIT;
ROLLBACK;