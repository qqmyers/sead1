package edu.illinois.ncsa.mmdb.web.common;

public enum ConfigurationKey {
    // information when sending email
    MailServer("mail.host", "localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailSubject("mail.subject", "[MEDICI]"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFrom("mail.from", "noreply@localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFullName("mail.fullname", "Medici Server"), //$NON-NLS-1$ //$NON-NLS-2$

    // sead enhancements
    VIVOQUERYURL("vivo-query.url", "http://sead-vivo.d2i.indiana.edu:3030/SEAD-VIVO/sparql?query="), //$NON-NLS-1$ //$NON-NLS-2$
    VIVOIDENTIFIERURL("vivo-identifier.url", "http://sead-vivo.d2i.indiana.edu:8080/sead-vivo/"),
    VAURL("va.url", "http://bluespruce.pti.indiana.edu:8181/dcs-nced/query/?q=resourceValue:(%s)"), //$NON-NLS-1$ //$NON-NLS-2$

    // access level
    AccessLevelLabel("access.level.label", "Access Level"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelPredicate("access.level.predicate", "http://sead-data.net/terms/hasDataMaturityLevel"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelDefault("access.level.default", "0"), //$NON-NLS-1$ //$NON-NLS-2$
    AccessLevelValues("access.level.values", "Raw, Preliminary Results, Provisional Product, Validated Product, Group Product"), //$NON-NLS-1$ //$NON-NLS-2$

    // project name and description
    ProjectName("project.name", ""), //$NON-NLS-1$
    ProjectDescription("project.description", ""), //$NON-NLS-1$
    ProjectURL("project.url", ""), //$NON-NLS-1$
    ProjectHeaderLogo("project.header.logo", "images/SEAD-small.png"), //$NON-NLS-1$
    ProjectHeaderBackground("project.header.background", "images/bkgrnd_repeat_x.png"), //$NON-NLS-1$
    ProjectHeaderTitleColor("project.header.title.color", "#000000"), //$NON-NLS-1$

    //presentation defaults
    PresentationSortOrder("presentation.sortorder", ""), //$NON-NLS-1$
    PresentationPageViewType("presentation.pageviewtype", ""), //$NON-NLS-1$
    PresentationDataViewLevel("presentation.dataviewlevel", "false"),

    // name of medici server
    MediciName("medici.name", null), //$NON-NLS-1$

    // location of lucene search index
    SearchPath("search.path", null), //$NON-NLS-1$

    // location of taxonomy file
    TaxonomyFile("taxonomy.file", "taxonomy.owl"), //$NON-NLS-1$ //$NON-NLS-2$

    // google map key
    GoogleMapKey("google.mapkey", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // remote API key
    RemoteAPIKey("remoteAPIKey", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // extractor url
    ExtractorUrl("extractor.url", "http://localhost:9856/"), //$NON-NLS-1$ //$NON-NLS-2$

    GoogleClientId("google.client_id", ""),
    GoogleDeviceClientId("google.device_client_id", ""),

    // Orcid oAuth
    OrcidClientId("orcid.client_id", ""),
    OrcidClientSecret("orcid.client_secret", ""),

    //optimizations for big data (many files)
    BigData("bigdata", "false"),

    //Token key lifetime
    TokenKeyLifetime("token.key.lifetime", "5"),

    //Use Google Document Viewer
    UseGoogleDocViewer("previewer.google_doc_viewer", "true");

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
