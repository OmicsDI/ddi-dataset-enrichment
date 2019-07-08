package uk.ac.ebi.ddi.task.ddidatasetenrichment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.ddidomaindb.dataset.Field;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.service.db.utils.DatasetUtils;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.configuration.DatasetEnrichmentTaskProperties;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.models.EnrichedDataset;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.services.EnrichmentService;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.utils.EnrichmentUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static uk.ac.ebi.ddi.ddidomaindb.dataset.DSField.Additional.*;

@SpringBootApplication
public class DdiDatasetEnrichmentApplication implements CommandLineRunner {

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private IDatasetService datasetService;

    @Autowired
    private DatasetEnrichmentTaskProperties properties;

    private static final Logger LOGGER = LoggerFactory.getLogger(DdiDatasetEnrichmentApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DdiDatasetEnrichmentApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        datasetService.readDatasetHashCode(properties.getDatabaseName())
                .stream()
                .filter(x -> x.getCurrentStatus().equalsIgnoreCase(DatasetCategory.INSERTED.getType()) ||
                        x.getCurrentStatus().equalsIgnoreCase(DatasetCategory.UPDATED.getType()))
                .forEach(this::process);
    }

    private void process(Dataset datasetShort) {
        try {
            Dataset dataset = datasetService.read(datasetShort.getAccession(), datasetShort.getDatabase());
            Map<String, String> fields = new HashMap<>();
            fields.put(DSField.NAME.getName(), dataset.getName());
            fields.put(DSField.DESCRIPTION.getName(), dataset.getDescription());
            fields.put(DATA.getName(), DatasetUtils.getFirstAdditional(dataset, DATA.getName()));
            fields.put(SAMPLE.getName(), DatasetUtils.getFirstAdditional(dataset, SAMPLE.getName()));
            fields.put(PUBMED_ABSTRACT.key(), DatasetUtils.getFirstAdditional(dataset, PUBMED_ABSTRACT.key()));
            fields.put(PUBMED_TITLE.key(), DatasetUtils.getFirstAdditional(dataset, PUBMED_TITLE.key()));
            EnrichedDataset enrichedDataset = enrichmentService.enrichment(dataset, fields, properties.isOverwrite());

            Map<Field, String> toBeEnriched = new HashMap<>();
            Map<String, String> enrichedAttributes = enrichedDataset.getEnrichedAttributes();
            toBeEnriched.put(ENRICH_TITLE, enrichedAttributes.get(DSField.NAME.getName()));
            toBeEnriched.put(ENRICH_ABSTRACT, enrichedAttributes.get(DSField.DESCRIPTION.getName()));
            toBeEnriched.put(ENRICH_SAMPLE, enrichedAttributes.get(SAMPLE.getName()));
            toBeEnriched.put(ENRICH_DATA, enrichedAttributes.get(DATA.getName()));
            toBeEnriched.put(ENRICHE_PUBMED_TITLE, enrichedAttributes.get(PUBMED_TITLE.getName()));
            toBeEnriched.put(ENRICH_PUBMED_ABSTRACT, enrichedAttributes.get(PUBMED_ABSTRACT.getName()));

            toBeEnriched.entrySet()
                    .stream()
                    .filter(x -> x.getValue() != null)
                    .forEach(x -> dataset.addAdditional(x.getKey().key(),
                            Collections.singleton(EnrichmentUtils.removeRedundantSynonyms(x.getValue()))));

            if (!dataset.getCurrentStatus().equalsIgnoreCase(DatasetCategory.DELETED.getType())) {
                dataset.setCurrentStatus(DatasetCategory.ENRICHED.getType());
            }
            datasetService.update(dataset.getId(), dataset);

        } catch (Exception e) {
            LOGGER.error("Exception occurred when processing dataset {},", datasetShort.getAccession(), e);
        }
    }
}
