package uk.ac.ebi.ddi.task.ddidatasetenrichment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
public class AnnotatedOntologyQuery {

    @JsonProperty("annotatedClass")
    private AnnotatedClass annotatedClass;

    @JsonProperty("annotations")
    private Annotation[] annotations;

    public AnnotatedClass getAnnotatedClass() {
        return annotatedClass;
    }

    public void setAnnotatedClass(AnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = annotations;
    }
}
