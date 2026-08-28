package org.esup_portail.esup_stage.repository;

import tools.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.esup_portail.esup_stage.model.Contact;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class ContactRepository extends PaginationRepository<Contact> {
    private static final String VISIBLE_FOR_CENTRES = "visibleForCentres";
    private static final String INUTILISE = "inutilise";

    /**
     * Contact « inutilisé » : plus référencé par aucune donnée active (identique au critère du
     * nettoyage automatique, voir ContactJpaRepository.findInutilisesPourNettoyage).
     *
     * Performance : un seul critère par sous-requête, jamais de OR entre deux colonnes. Un OR
     * (ex. « cv.contact = c OR cv.signataire = c ») empêche MySQL d'utiliser les index de clé
     * étrangère et dégénère en balayage complet de la table référençante pour CHAQUE contact —
     * rédhibitoire sur une volumétrie de plusieurs dizaines de milliers de contacts. Les
     * sous-requêtes sont par ailleurs ordonnées de la plus « filtrante » à la plus rare :
     * l'évaluation s'arrête au premier NOT EXISTS non satisfait.
     */
    private static final String CRITERE_INUTILISE =
            "NOT EXISTS (SELECT 1 FROM Convention cv1 WHERE cv1.contact = c)" +
            " AND NOT EXISTS (SELECT 1 FROM Convention cv2 WHERE cv2.signataire = c)" +
            " AND NOT EXISTS (SELECT 1 FROM Avenant a WHERE a.contact = c)" +
            " AND NOT EXISTS (SELECT 1 FROM EvaluationTuteurToken et WHERE et.contact = c AND et.expiresAt >= CURRENT_TIMESTAMP)" +
            " AND NOT EXISTS (SELECT 1 FROM Offre o1 WHERE o1.referent = c)" +
            " AND NOT EXISTS (SELECT 1 FROM Offre o2 WHERE o2.contactCand = c)" +
            " AND NOT EXISTS (SELECT 1 FROM Offre o3 WHERE o3.contactInfo = c)" +
            " AND NOT EXISTS (SELECT 1 FROM Offre o4 WHERE o4.contactProprio = c)" +
            " AND NOT EXISTS (SELECT 1 FROM AccordPartenariat ap WHERE ap.contact = c)";

    public ContactRepository(EntityManager em) {
        super(em, Contact.class, "c");
        this.predicateWhitelist = Arrays.asList("nom", "prenom", "mail", "fonction", "service.nom", "service.structure.raisonSociale");
        this.specificFilterWhitelist.add(VISIBLE_FOR_CENTRES);
        this.specificFilterWhitelist.add(INUTILISE);
    }

    public Long countVisibleForCentres(List<Integer> centreIds, String filters) {
        return super.count(withVisibilityFilter(filters, centreIds));
    }

    public List<Contact> findPaginatedVisibleForCentres(List<Integer> centreIds, int page, int perPage, String predicate, String sortOrder, String filters) {
        return super.findPaginated(page, perPage, predicate, sortOrder, withVisibilityFilter(filters, centreIds));
    }

    @Override
    protected void addSpecificParameter(String key, JsonNode parameter, List<String> clauses) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JsonNode centreIds = parameter.get("value");
            if (centreIds.isEmpty()) {
                clauses.add("1 = 0");
                return;
            }
            clauses.add("c.centreGestion.id IN :visibleForCentres");
        }
        if (INUTILISE.equals(key)) {
            clauses.add("(" + CRITERE_INUTILISE + ")");
        }
    }

    @Override
    protected void setSpecificParameterValue(String key, JsonNode parameter, Query query) {
        if (VISIBLE_FOR_CENTRES.equals(key)) {
            JsonNode jsonArray = parameter.get("value");
            List<Integer> values = new ArrayList<>();
            for (JsonNode item : jsonArray) {
                values.add(item.asInt());
            }
            if (!values.isEmpty()) {
                query.setParameter(VISIBLE_FOR_CENTRES, values);
            }
        }
    }

    private String withVisibilityFilter(String filters, List<Integer> centreIds) {
        JSONObject jsonFilters = new JSONObject(filters);
        JSONObject visibilityFilter = new JSONObject();
        visibilityFilter.put("specific", true);
        visibilityFilter.put("value", centreIds == null ? new JSONArray() : new JSONArray(centreIds));
        jsonFilters.put(VISIBLE_FOR_CENTRES, visibilityFilter);
        return jsonFilters.toString();
    }
}
