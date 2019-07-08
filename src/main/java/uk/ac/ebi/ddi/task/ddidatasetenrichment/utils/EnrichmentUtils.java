package uk.ac.ebi.ddi.task.ddidatasetenrichment.utils;

import java.util.HashSet;
import java.util.Set;

public class EnrichmentUtils {

    private EnrichmentUtils() {
    }

    public static String removeRedundantSynonyms(String synonyms) {
        if (synonyms != null) {
            Set<String> resultStringSet = new HashSet<>();
            StringBuilder resultSynonym = new StringBuilder();
            String[] synonymsArr = synonyms.split(";");
            for (String synonym: synonymsArr) {
                if (synonym != null && !synonym.isEmpty()) {
                    String[] redudantSynonyms = synonym.split(",");
                    for (String redundantSynom: redudantSynonyms) {
                        resultStringSet.add(redundantSynom.trim());
                    }
                }
            }
            for (String synonym: resultStringSet) {
                resultSynonym.append(synonym).append(", ");
            }
            if ((resultSynonym.length() > 0) && resultSynonym.length() > 2) {
                resultSynonym = new StringBuilder(resultSynonym.substring(0, resultSynonym.length() - 2));
            }
            return resultSynonym.toString();

        }
        return null;
    }
}
