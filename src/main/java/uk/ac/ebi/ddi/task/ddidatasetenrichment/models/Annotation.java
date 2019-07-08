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
public class Annotation {

    @JsonProperty("from")
    private int fromPosition;

    @JsonProperty("hierarchySize")
    private int hierarchySize;

    @JsonProperty("matchType")
    private String matchType;

    @JsonProperty("text")
    private String text;

    @JsonProperty("to")
    private int toPosition;

    @JsonProperty("annotatedClass")
    private AnnotatedClass annotatedClass;

    public int getFromPosition() {
        return fromPosition;
    }

    public void setFromPosition(int fromPosition) {
        this.fromPosition = fromPosition;
    }

    public int getHierarchySize() {
        return hierarchySize;
    }

    public void setHierarchySize(int hierarchySize) {
        this.hierarchySize = hierarchySize;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getToPosition() {
        return toPosition;
    }

    public void setToPosition(int toPosition) {
        this.toPosition = toPosition;
    }

    public AnnotatedClass getAnnotatedClass() {
        return annotatedClass;
    }
    public void setAnnotatedClass(AnnotatedClass annotatedClass) {
        this.annotatedClass = annotatedClass;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "fromPosition=" + fromPosition +
                ", hierarchySize=" + hierarchySize +
                ", matchType='" + matchType + '\'' +
                ", text='" + text + '\'' +
                ", toPosition=" + toPosition +
                ", annotatedClass=" + annotatedClass +
                '}';
    }

}
