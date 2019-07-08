package uk.ac.ebi.ddi.task.ddidatasetenrichment.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("enrichment")
public class DatasetEnrichmentTaskProperties {

    private String databaseName;

    private boolean overwrite = false;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    @Override
    public String toString() {
        return "DatasetEnrichmentTaskProperties{" +
                "databaseName='" + databaseName + '\'' +
                ", overwrite=" + overwrite +
                '}';
    }
}
