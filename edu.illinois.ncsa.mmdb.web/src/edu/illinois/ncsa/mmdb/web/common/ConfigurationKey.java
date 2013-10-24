package edu.illinois.ncsa.mmdb.web.common;

public enum ConfigurationKey {
    // information when sending email
    MailServer("mail.host", "localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailSubject("mail.subject", "[MEDICI]"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFrom("mail.from", "noreply@localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFullName("mail.fullname", "Medici Server"), //$NON-NLS-1$ //$NON-NLS-2$

    // sead enhancements
    VIVOJOSEKIURL("vivo-joseki.url", "http://sead-dev.ccni.rpi.edu/joseki/sparql?query="), //$NON-NLS-1$ //$NON-NLS-2$
    VAURL("va.url", "http://bluespruce.pti.indiana.edu:8181/dcs-nced/query/?q=resourceValue:(%s)"), //$NON-NLS-1$ //$NON-NLS-2$
    NCEDURL("nced.url", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // access level
    AccessLevelLabel("access.level.label", "Access Level"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelPredicate("access.level.predicate", "http://cet.ncsa.uiuc.edu/2007/accessLevel"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelMin("access.level.min", "0"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelMax("access.level.max", "0"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelDefault("access.level.default", "0"), //$NON-NLS-1$ //$NON-NLS-2$

    // project name and description
    ProjectName("project.name", ""), //$NON-NLS-1$
    ProjectDescription("project.description", ""), //$NON-NLS-1$
    ProjectURL("project.url", ""), //$NON-NLS-1$

    // name of medici server
    MediciName("medici.name", null), //$NON-NLS-1$ 

    // location of lucene search index
    SearchPath("search.path", null), //$NON-NLS-1$ 

    // location of taxonomy file
    TaxonomyFile("taxonomy.file", "taxonomy.owl"), //$NON-NLS-1$ //$NON-NLS-2$ 

    // google map key
    GoogleMapKey("google.mapkey", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // remote API key
    RemoteAPIKey("remoteAPI", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // extractor url
    ExtractorUrl("extractor.url", "http://localhost:9856/"), //$NON-NLS-1$ //$NON-NLS-2$

    GoogleClientId("google.client_id", "");

    private final String propertyKey;
    private final String defaultValue;

    private ConfigurationKey(String propertyKey, String defaultValue) {
        this.propertyKey = propertyKey;
        this.defaultValue = defaultValue;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
