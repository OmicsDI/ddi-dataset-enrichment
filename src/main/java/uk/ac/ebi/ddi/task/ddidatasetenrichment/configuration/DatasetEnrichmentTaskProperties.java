package uk.ac.ebi.ddi.task.ddidatasetenrichment.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("enrichment")
public class DatasetEnrichmentTaskProperties {

    private String databaseName;

    private boolean force = false;

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

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return "DatasetEnrichmentTaskProperties{" +
                "databaseName='" + databaseName + '\'' +
                ", force=" + force +
                ", overwrite=" + overwrite +
                '}';
    }
}
