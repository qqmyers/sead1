package edu.illinois.ncsa.mmdb.web.common;

public enum ConfigurationKey {
    // information when sending email
    MailServer("mail.host", "localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailSubject("mail.subject", "[MEDICI]"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFrom("mail.from", "noreply@localhost"), //$NON-NLS-1$ //$NON-NLS-2$
    MailFullName("mail.fullname", "Medici Server"), //$NON-NLS-1$ //$NON-NLS-2$
    //Start - Added by Ram
    VIVOJOSEKIURL("vivo-joseki.url", "http://sead-dev.ccni.rpi.edu/joseki/sparql?query="), //$NON-NLS-1$ //$NON-NLS-2$
    //END - Added by Ram

    // name of medici server
    MediciName("medici.name", null), //$NON-NLS-1$ 

    // location of lucene search index
    SearchPath("search.path", null), //$NON-NLS-1$ 

    // location of taxonomy file
    TaxonomyFile("taxonomy.file", "taxonomy.owl"), //$NON-NLS-1$ //$NON-NLS-2$ 

    // google map key
    GoogleMapKey("google.mapkey", ""), //$NON-NLS-1$ //$NON-NLS-2$

    // extractor url
    ExtractorUrl("extractor.url", "http://localhost:9856/"); //$NON-NLS-1$ //$NON-NLS-2$

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
