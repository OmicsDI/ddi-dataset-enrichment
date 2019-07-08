package uk.ac.ebi.ddi.task.ddidatasetenrichment.utils;

public class Constants {

    private Constants() {
    }

    public static final String OBO_KEY       = "807fa818-0a7c-43be-9bac-51576e8795f5";

    public static final String[] OBO_ONTOLOGIES = {"MESH", "MS",
            "EFO", "GO-PLUS", "BIOMODELS", "BP",
            "MEDLINEPLUS", "NCBITAXON", "GEXO", "CCO", "CLO",
            "CCONT", "BTO", "OBI", "GO"};
    public static final String NOT_AVAILABLE = "Not availabel";
    public static final String NOT_ANNOTATION_FOUND = "NoAnnotationFound";
    public static final String WORD_ID = "wordId";
    public static final String ONTOLOGY_NAME = "ontologyName";
}
