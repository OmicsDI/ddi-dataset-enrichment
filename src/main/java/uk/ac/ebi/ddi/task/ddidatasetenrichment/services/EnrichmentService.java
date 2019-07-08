package uk.ac.ebi.ddi.task.ddidatasetenrichment.services;


import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.model.enrichment.DatasetEnrichmentInfo;
import uk.ac.ebi.ddi.service.db.model.enrichment.Synonym;
import uk.ac.ebi.ddi.service.db.model.enrichment.WordInField;
import uk.ac.ebi.ddi.service.db.service.enrichment.IEnrichmentInfoService;
import uk.ac.ebi.ddi.service.db.service.enrichment.ISynonymsService;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.AnnotatedOntologyQuery;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.Annotation;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.EnrichedDataset;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.SynonymQuery;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.utils.Constants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


/**
 * Provide service for synonym annotation
 *
 * @author Mingze
 */
@Service
public class EnrichmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentService.class);

    @Autowired
    private ISynonymsService synonymsService;

    @Autowired
    private IEnrichmentInfoService enrichmentInfoService;

    @Autowired
    private BioOntologyService bioOntologyService;

    /**
     * Enrichment on the dataset, includes title, abstraction, sample protocol, data protocol.
     *
     * @return and enriched dataset
     */
    public EnrichedDataset enrichment(Dataset dataset, Map<String, String> fields, boolean overwrite) throws Exception {

        String accession = dataset.getAccession();
        String database = dataset.getDatabase();

        EnrichedDataset enrichedDataset = new EnrichedDataset(accession, database);
        DatasetEnrichmentInfo datasetEnrichmentInfo = new DatasetEnrichmentInfo(accession, database);
        DatasetEnrichmentInfo prevDs = enrichmentInfoService.getLatest(accession, database);

        Map<String, List<WordInField>> synonyms = new HashMap<>();
        boolean hasChange = false;

        if (prevDs == null || overwrite) {
            synonyms = getWordsInFieldFromWS(fields);
            hasChange = true;
        } else {
            for (String key : fields.keySet()) {
                if (prevDs.getSynonyms() != null && prevDs.getSynonyms().containsKey(key)
                        && prevDs.getOriginalAttributes().get(key).equals(fields.get(key))) {
                    synonyms.put(key, prevDs.getSynonyms().get(key));
                } else {
                    List<WordInField> words = getWordsInFieldFromWS(fields.get(key));
                    if (words != null && !words.isEmpty()) {
                        synonyms.put(key, words);
                        hasChange = true;
                    }
                }
            }
        }

        datasetEnrichmentInfo.setSynonyms(synonyms);
        datasetEnrichmentInfo.setEnrichTime(new Date());

        datasetEnrichmentInfo.setOriginalAttributes(fields);
        if (hasChange) {
            //Only save into db when there is some changes
            enrichmentInfoService.insert(datasetEnrichmentInfo);
        }
        Map<String, String> attribute = new HashMap<>();
        for (Map.Entry<String, List<WordInField>> entry : synonyms.entrySet()) {
            attribute.put(entry.getKey(), enrichField(entry.getValue()));
        }
        enrichedDataset.setEnrichedAttributes(attribute);

        return enrichedDataset;
    }

    /**
     * Transfer the words found in field to the synonyms String
     *
     * @param wordsInField the words provided by the service
     * @return the final string of the enrichment
     */
    private String enrichField(List<WordInField> wordsInField) throws JSONException, RestClientException {
        if (wordsInField == null || wordsInField.isEmpty()) {
            return null;
        }
        StringBuilder enriched = new StringBuilder();
        for (WordInField word : wordsInField) {
            List<String> synonymsForWord = getSynonymsForWord(word.getText());
            if (synonymsForWord != null) {
                for (String synonym : synonymsForWord) {
                    enriched.append(synonym).append(", ");
                }
                if (enriched.length() > 0) {
                    enriched = new StringBuilder(enriched.substring(0, enriched.length() - 2)); //remove the last comma
                    enriched.append("; ");
                }
            }
        }
        if (enriched.length() > 0) {
            enriched = new StringBuilder(enriched.substring(0, enriched.length() - 2)); //remove the last comma
            enriched.append(".");
        }
        return enriched.toString();
    }

    /**
     * Get the biology related words in one field from WebService at bioontology.org
     *
     * @param fieldText a field Text
     * @return the words which are identified in the fieldText by recommender API from bioontology.org
     */

    private List<WordInField> getWordsInFieldFromWS(String fieldText) throws Exception {

        List<WordInField> matchedWords = new ArrayList<>();
        if (fieldText == null || fieldText.equals(Constants.NOT_AVAILABLE)) {
            return matchedWords;
        }
        fieldText = fieldText.replace("%", " ").trim(); //to avoid malformed error

        if (fieldText.isEmpty()) {
            return matchedWords;
        }
        JsonNode recommends = bioOntologyService.getAnnotatedSynonyms(fieldText);
        Map<WordInField, Set<String>> synonymsMap = new HashMap<>();
        for (JsonNode annotations: recommends) {
            if (annotations.get("annotatedClass") != null && annotations.get("annotations") != null) {
                Set<String> synonyms = new HashSet<>();
                if (annotations.get("annotatedClass") != null) {
                    if (annotations.get("annotatedClass").get("synonym") != null) {
                        for (JsonNode synonym: annotations.get("annotatedClass").get("synonym")) {
                            synonyms.add(synonym.textValue());
                        }
                    }
                }
                for (JsonNode annotationValue: annotations.get("annotations")) {
                    String actualWord = annotationValue.get("text").textValue();
                    int from = annotationValue.get("from").intValue();
                    int to = annotationValue.get("to").intValue();
                    WordInField wordInField = new WordInField(actualWord, from, to);
                    if (synonymsMap.containsKey(wordInField)) {
                        synonyms.addAll(synonymsMap.get(wordInField));
                    }
                    synonymsMap.put(wordInField, synonyms);
                }
            }
        }
        matchedWords.addAll(getDistinctWordList(synonymsMap));
        Collections.sort(matchedWords);
        return matchedWords;
    }

    private Map<String, List<WordInField>> getWordsInFieldFromWS(Map<String, String> fields) throws Exception {

        ConcurrentHashMap<String, List<WordInField>> results = new ConcurrentHashMap<>();

        if (fields == null || fields.isEmpty()) {
            return results;
        }

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            List<WordInField> matchedWords = getWordsInFieldFromWS(entry.getValue());
            if (!matchedWords.isEmpty()) {
                results.put(entry.getKey(), matchedWords);
            }
        }

        return results;
    }

    /**
     * Get all synonyms for a word from mongoDB. If this word is not in the DB, then get it's synonyms from Web Service,
     * and insert them into the mongoDB. One assumption: if word1 == word2, word2 == word3,
     *                                                  then word1 == word3, == means
     * synonym.
     *
     * @param word to retrieve the given synonyms
     * @return the list of synonyms
     */
    public List<String> getSynonymsForWord(String word) throws JSONException, RestClientException {

        List<String> synonyms;

        if (synonymsService.isWordExist(word)) {
            synonyms = synonymsService.getAllSynonyms(word);
        } else {
            synonyms = getSynonymsForWordFromWS(word);
            Synonym synonym = synonymsService.insert(word, synonyms);
            if (synonym != null && synonym.getSynonyms() != null) {
                synonyms = synonym.getSynonyms();
            }
        }

        return synonyms;
    }


    /**
     * get synonyms for a word, from the BioPortal web service API
     * the cachedSynonymRul come from the annotation process
     *
     * @param word the word to look for synonyms
     * @return get synonyms of the word, by annotator API from bioontology.org
     */
    private ArrayList<String> getSynonymsForWordFromWS(String word) throws JSONException, RestClientException {
        String lowerWord = word.toLowerCase();
        ArrayList<String> synonyms = new ArrayList<>();

        AnnotatedOntologyQuery[] annotatedTerms = bioOntologyService.getAnnotatedTerms(lowerWord,
                Constants.OBO_ONTOLOGIES);
        if (annotatedTerms == null) {
            return null;
        }

        if (annotatedTerms.length == 0) {
            synonyms.add(Constants.NOT_ANNOTATION_FOUND);
            return synonyms;
        }

        Annotation[] annotations = annotatedTerms[0].getAnnotations();
        Annotation annotation = annotations[0];

        if (annotation.getFromPosition() > 1) {
            synonyms.add(Constants.NOT_ANNOTATION_FOUND);
            return synonyms;
        }

        String matchedWord = annotation.getText().toLowerCase();

        JSONArray matchedClasses = findBioOntologyMatchclasses(matchedWord, annotatedTerms);

        for (int i = 0; i < matchedClasses.length(); i++) {

            JSONObject matchedClass = (JSONObject) matchedClasses.get(i);
            String wordId = matchedClass.getString(Constants.WORD_ID);
            String ontologyName = matchedClass.getString(Constants.ONTOLOGY_NAME);

            SynonymQuery output = bioOntologyService.getAllSynonyms(ontologyName, wordId);
            if (output == null) {
                return null;
            }

            String[] synonymsInCls = output.getSynonyms();
            Collections.addAll(synonyms, synonymsInCls);
        }
        return synonyms;
    }

    /**
     * get the clasess which has the same matched word as matchedWord
     *
     * @param matchedWord       chosen from the first annotation result from annotator API as the matched ontology word
     * @param annotationResults annotation results from annotator API, may contain multiple matched classes
     * @return a JSONArray with all the terms and annotations
     */
    private JSONArray findBioOntologyMatchclasses(String matchedWord, AnnotatedOntologyQuery[] annotationResults)
            throws JSONException {
        JSONArray matchedClasses = new JSONArray();
        for (AnnotatedOntologyQuery annotationResult : annotationResults) {

            Annotation[] annotations = annotationResult.getAnnotations();
            Annotation annotation = annotations[0];

            String matchedWordHere = annotation.getText().toLowerCase();
            if (!matchedWordHere.equals(matchedWord)) {
                continue;
            }

            String wordIdString = annotationResult.getAnnotatedClass().getId();
            if (Pattern.matches("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*?)\\/(.*?)", wordIdString)) {
                String ontologyName =
                        wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$1");
                String wordId =
                        wordIdString.replaceAll("http:\\/\\/purl\\.bioontology\\.org\\/ontology\\/(.*)\\/(.*)", "$2");
                JSONObject matchedClass = new JSONObject();
                matchedClass.put(Constants.WORD_ID, wordId);
                matchedClass.put(Constants.ONTOLOGY_NAME, ontologyName);
                matchedClasses.put(matchedClass);
                LOGGER.debug(Constants.WORD_ID + " " + matchedClass.get(Constants.WORD_ID));
            }

        }
        return matchedClasses;
    }

    private List<WordInField> getDistinctWordList(Map<WordInField, Set<String>> synonyms) {
        List<WordInField> matchedWords = new ArrayList<>();
        if (synonyms != null && synonyms.size() > 0) {
            for (Map.Entry<WordInField, Set<String>> matchedTerm : synonyms.entrySet()) {
                WordInField key = matchedTerm.getKey();
                WordInField word = new WordInField(key.getText().toLowerCase(), key.getFrom(), key.getTo());
                WordInField overlappedWordInList = findOverlappedWordInList(word, matchedWords);

                if (null == overlappedWordInList) {
                    matchedWords.add(word);
                } else {
                    modifyWordList(word, overlappedWordInList, matchedWords);
                }

                synonymsService.update(new Synonym(word.getText(), (new ArrayList<>(matchedTerm.getValue()))));
            }
        }
        return matchedWords;
    }


    /**
     * Choose the longer one between word and overlapped word, write it in the matchedWords
     *
     * @param word                 the word to be search in the
     * @param overlappedWordInList
     * @param matchedWords
     */
    private void modifyWordList(WordInField word, WordInField overlappedWordInList, List<WordInField> matchedWords) {
        int from = word.getFrom();
        int to = word.getTo();

        int overlappedFrom = overlappedWordInList.getFrom();
        int overlappedTo = overlappedWordInList.getTo();

        if (from - overlappedFrom == 0 && to - overlappedTo == 0) {
            return;
        }

        if (from <= overlappedFrom && to >= overlappedTo) {
            int index = matchedWords.indexOf(overlappedWordInList);
            matchedWords.set(index, word);
        }
    }

    /**
     * Find the words in matchedWords which is overlapped with "word"
     *
     * @param word
     * @param matchedWords
     * @return
     */
    private WordInField findOverlappedWordInList(WordInField word, List<WordInField> matchedWords) {
        WordInField overlappedWord = null;

        for (WordInField wordInList : matchedWords) {

            if (word.getFrom() == wordInList.getFrom() && word.getTo() == wordInList.getTo()) {
                LOGGER.debug("find same word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getFrom() <= wordInList.getTo() && word.getTo() >= wordInList.getTo()) {
                LOGGER.debug("find an overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
            if (word.getTo() >= wordInList.getFrom() && word.getTo() <= wordInList.getTo()) {
                LOGGER.debug("find an overlapped word for '" + word + "':" + wordInList);
                overlappedWord = wordInList;
                break;
            }
        }

        return overlappedWord;
    }

}


