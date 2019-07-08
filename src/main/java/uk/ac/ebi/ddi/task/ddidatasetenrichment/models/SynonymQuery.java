package uk.ac.ebi.ddi.task.ddidatasetenrichment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

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
 * Created by ypriverol (ypriverol@gmail.com) on 29/05/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SynonymQuery {

    @JsonProperty("prefLabel")
    private String prefLabel;

    @JsonProperty("synonym")
    private String[] synonyms;

    @JsonProperty("definition")
    private String[] definition;

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String[] getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String[] synonyms) {
        this.synonyms = synonyms;
    }

    public String[] getDefinition() {
        return definition;
    }

    public void setDefinition(String[] definition) {
        this.definition = definition;
    }

    @Override
    public String toString() {
        return "SynonymQuery{" +
                "prefLabel='" + prefLabel + '\'' +
                ", synonyms=" + Arrays.toString(synonyms) +
                ", definition='" + Arrays.toString(definition) + '\'' +
                '}';
    }
}
