package uk.ac.ebi.ddi.task.ddidatasetenrichment.models;

import java.util.Map;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0

 *  ==Overview==
 *
 *  This class
 *
 * Created by ypriverol (ypriverol@gmail.com) on 05/10/15.
 */
public class EnrichedDataset {

    private String accession;
    private String database;

    private Map<String, String> enrichedAttributes;

    public EnrichedDataset(String accession, String database) {
        this.accession = accession;
        this.database = database;
    }

    public String getAccession() {
        return accession;
    }

    public void setAccession(String accession) {
        this.accession = accession;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Map<String, String> getEnrichedAttributes() {
        return enrichedAttributes;
    }

    public void setEnrichedAttributes(Map<String, String> enrichedAttributes) {
        this.enrichedAttributes = enrichedAttributes;
    }
}
