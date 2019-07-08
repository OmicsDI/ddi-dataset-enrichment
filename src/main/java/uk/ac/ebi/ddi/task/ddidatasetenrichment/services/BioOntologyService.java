package uk.ac.ebi.ddi.task.ddidatasetenrichment.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.AnnotatedOntologyQuery;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.SynonymQuery;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.utils.Constants;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.utils.RetryClient;

import java.net.URI;

@Service
public class BioOntologyService extends RetryClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(BioOntologyService.class);

    private static final String REST_URL = "http://data.bioontology.org";

    private RestTemplate restTemplate = new RestTemplate();


    public JsonNode getAnnotatedSynonyms(String query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(REST_URL)
                .path("/annotator")
                .queryParam("ontologies", String.join(",", Constants.OBO_ONTOLOGIES))
                .queryParam("longest_only", true)
                .queryParam("whole_word_only", true)
                .queryParam("include", "prefLabel,synonym,definition")
                .queryParam("max_level", 3);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "apikey token=" + Constants.OBO_KEY);
        headers.add("Content-Type", "application/json");
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("text", query);
        HttpEntity<?> httpEntity = new HttpEntity<>(map, headers);
        URI uri = builder.build().encode().toUri();
        return execute(ctx -> restTemplate.postForObject(uri, httpEntity, JsonNode.class));
    }

    public AnnotatedOntologyQuery[] getAnnotatedTerms(String query, String[] ontologies) throws RestClientException {
        String ontology = String.join(",", ontologies);

        String url = String.format(
                "%s/annotator?ontologies=%s&longest_only=true&whole_word_only=false&apikey=%s&text=%s",
                REST_URL, ontology, Constants.OBO_KEY, query);

        return restTemplate.getForObject(url, AnnotatedOntologyQuery[].class);

    }

    public SynonymQuery getAllSynonyms(String ontology, String term) throws RestClientException {

        String url = String.format("%s/ontologies/%s/classes/%s?apikey=%s",
                REST_URL, ontology, term, Constants.OBO_KEY);
        LOGGER.debug(url);

        return restTemplate.getForObject(url, SynonymQuery.class);
    }
}
