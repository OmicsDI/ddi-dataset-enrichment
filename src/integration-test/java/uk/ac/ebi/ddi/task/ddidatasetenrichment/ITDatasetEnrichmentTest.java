package uk.ac.ebi.ddi.task.ddidatasetenrichment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ddi.service.db.model.dataset.Dataset;
import uk.ac.ebi.ddi.service.db.service.dataset.IDatasetService;
import uk.ac.ebi.ddi.service.db.utils.DatasetCategory;
import uk.ac.ebi.ddi.task.ddidatasetenrichment.configuration.DatasetEnrichmentTaskProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static uk.ac.ebi.ddi.ddidomaindb.dataset.DSField.Additional.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DdiDatasetEnrichmentApplication.class,
		initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {
		"enrichment.database_name=ArrayExpress"
})
public class ITDatasetEnrichmentTest {

	@Autowired
	private DatasetEnrichmentTaskProperties taskProperties;

	@Autowired
	private DdiDatasetEnrichmentApplication application;

	@Autowired
	private IDatasetService datasetService;

	@Before
	public void setUp() throws Exception {
		Dataset dataset = new Dataset();
		dataset.setAccession("E-ATMX-10");
		dataset.setDatabase(taskProperties.getDatabaseName());
		dataset.setName("Transcription profiling of Arabidopsis wild type, FIE- and MIA- loss-of-function mutants");
		dataset.setDescription("Plants with genotypes wild type, FIE loss-of-function and MIA loss-of-function" +
				" were compared. Plants were grown in growth chambers at 70% humidity and daily cycles of 16 h" +
				" light and 8 h darkness at 21 C. Plant material used for the experiments was pooled from 10 plants." +
				" Siliques without withering flower organs were harvested. The experiment was performed twice" +
				" providing independent biological replicates. This for testing only:  Libraries were prepared as " +
				"described in DOI:10.1038/NMETH.1315. Each library");
		Map<String, Set<String>> dates = new HashMap<>();
		dates.put("publication", Collections.singleton("2007-01-18"));
		dates.put("updated", Collections.singleton("2014-05-01"));
		dataset.setDates(dates);

		Map<String, Set<String>> additional = new HashMap<>();
		additional.put("omics_type", Collections.singleton("Transcriptomics"));
		additional.put("submitter", Collections.singleton("Lars Hennig"));
		additional.put("instrument_platform", Collections.singleton("418 [Affymetrix]"));
		additional.put("software", Collections.singleton("MicroArraySuite 5.0"));
		additional.put("species", Collections.singleton("Arabidopsis Thaliana"));
		additional.put("submitter_keywords", Collections.singleton("transcription profiling by array"));
		additional.put("full_dataset_link",
				Collections.singleton("https://www.ebi.ac.uk/arrayexpress/experiments/E-ATMX-10"));
		additional.put("submitter_email", Collections.singleton("lhennig@ethz.ch"));
		additional.put("repository", Collections.singleton("ArrayExpress"));
		additional.put("sample_protocol", Collections.singleton("Growth Protocol - Plants were grown in growth " +
				"chambers at 70% humidity and daily cycles of 16 h light and 8 h darkness at 21 C." +
				" Plant material used for the experiments was pooled from 10 plants. Siliques without withering " +
				"flower organs were harvested.\n" +
				" Hybridization - Affymetrix Generic Hybridization\n" +
				" Labeling - Fifteen micrograms of total RNA were used to prepare cDNA with the Superscript " +
				"Double-Stranded cDNA Synthesis Kit (Invitrogen) according to manufacturers instructions using " +
				"oligodT-T7 oligonucleotides (GGCCAGTGAATTGTAATACGACTCACTATAGGGAGGCGG(dT)24). The cDNA was " +
				"subjected to in vitro transcription in the presence of 2 mM each biotin-11-CTP and biotin-16-UTP " +
				"(ENZO Life Sciences, Farmingdale, NY) using the MegaScript High Yield Transcription Kit " +
				"(Ambion, Austin, TX). After purification of the cRNA on RNeasy columns (Quiagen, Hilden, Germany)," +
				" fifteen micrograms cRNA were fragmented in a volume of 40 Ýl as recommended by Affymetrix.\n" +
				" Nucleic Acid Extraction - Total RNA was prepared from frozen tissue using Trizol and purified with " +
				"RNeasy columns (Quiagen, Hilden, Germany).\n" +
				" Scaning - P-AFFY-6 Affymetrix CEL analysis"));
		additional.put("data_protocol",
				Collections.singleton("Assay Data Transformation - Affymetrix CHP Analysis (ExpressionStat)."));
		additional.put("pubmed_abstract", Collections.singleton("The Polycomb-group (PcG) proteins MEDEA, " +
				"FERTILIZATION INDEPENDENT ENDOSPERM, and FERTILIZATION INDEPENDENT SEED2 regulate seed " +
				"development in Arabidopsis by controlling embryo and endosperm proliferation. All three of " +
				"these FIS-class proteins are likely subunits of a multiprotein PcG complex, which epigenetically " +
				"regulates downstream target genes that were previously unknown. Here we show that the MADS-box " +
				"gene PHERES1 (PHE1) is commonly deregulated in the fis-class mutants. PHE1 belongs to the " +
				"evolutionarily ancient type I class of MADS-box proteins that have not yet been assigned any " +
				"function in plants. Both MEDEA and FIE directly associate with the promoter region of PHE1, " +
				"suggesting that PHE1 expression is epigenetically regulated by PcG proteins. PHE1 is expressed " +
				"transiently after fertilization in both the embryo and the endosperm; however, it remains " +
				"up-regulated in the fis mutants, consistent with the proposed function of the FIS genes as " +
				"transcriptional repressors. Reduced expression levels of PHE1 in medea mutant seeds can suppress " +
				"medea seed abortion, indicating a key role of PHE1 repression in seed development. PHE1 expression " +
				"in a hypomethylated medea mutant background resembles the wild-type expression pattern and is " +
				"associated with rescue of the medea seed-abortion phenotype. In summary, our results demonstrate " +
				"that seed abortion in the medea mutant is largely mediated by deregulated expression of the " +
				"type I MADS-box gene PHE1."));
		additional.put("pubmed_title", Collections.singleton("The Polycomb-group protein MEDEA regulates seed " +
				"development by controlling expression of the MADS-box gene PHERES1."));
		dataset.setAdditional(additional);

		Map<String, Set<String>> crossReference = new HashMap<>();
		crossReference.put("pubmed", Collections.singleton("12815071"));
		dataset.setCrossReferences(crossReference);
		dataset.setCurrentStatus("Inserted");
		datasetService.save(dataset);
	}

	@Test
	public void contextLoads() throws Exception {
		application.run();
		Dataset dataset = datasetService.read("E-ATMX-10", taskProperties.getDatabaseName());
		Assert.assertNotNull(dataset);
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICH_TITLE.key()));
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICH_ABSTRACT.key()));
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICH_SAMPLE.key()));
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICH_DATA.key()));
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICHE_PUBMED_TITLE.key()));
		Assert.assertTrue(dataset.getAdditional().containsKey(ENRICH_PUBMED_ABSTRACT.key()));

		Assert.assertEquals("DMDA1, function., Cresses, A., Arabidopsis thaliana, Mouse-ear Cress, " +
				"thaliana, A4, A. thaliana, Mouse-ear Cresses, Mouse-ear, TYPE, Cardaminopsis, LGMD2C, DAGA4, " +
				"transcription profiling, Arabidopsis, Arabidopsis thalianas, DMDA, A. thalianas, SCARMD2, " +
				"thalianas, Cress, Mouse ear, MAM, Arabidopses, SCG3, gene expression profiling, CD-RAP",
				dataset.getAdditionalField(ENRICH_TITLE.key()).iterator().next());

		Assert.assertEquals(DatasetCategory.ENRICHED.getType(), dataset.getCurrentStatus());
	}

}
