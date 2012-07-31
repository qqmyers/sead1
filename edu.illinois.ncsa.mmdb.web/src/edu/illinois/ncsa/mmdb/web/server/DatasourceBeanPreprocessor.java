package edu.illinois.ncsa.mmdb.web.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.kernel.beans.SaveBeanPreprocessor;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

public class DatasourceBeanPreprocessor implements SaveBeanPreprocessor {
    private static Log             log = LogFactory.getLog(DatasourceBeanPreprocessor.class);

    private final DataSource       datasource;
    private final MimeMap          mimemap;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static public DatasourceBeanPreprocessor createDatasourceBeanPreprocessor(Context context) {
        if (context instanceof MysqlContext) {
            MysqlContext c = (MysqlContext) context;
            BasicDataSource datasource = new BasicDataSource();
            datasource.setDriverClassName("com.mysql.jdbc.Driver"); //$NON-NLS-1$
            datasource.setUsername(c.getUser());
            datasource.setPassword(c.getPassword());
            datasource.setRemoveAbandoned(true);
            datasource.setRemoveAbandonedTimeout(10);
            datasource.setMaxActive(10);
            datasource.setUrl(String.format("jdbc:mysql://%s:%d/%s?characterEncoding=utf8", c.getHost(), c.getPort(), c.getSchema())); //$NON-NLS-1$

            try {
                Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                statement.execute("SELECT * FROM dataset LIMIT 1;");
                statement.close();
                connection.close();
            } catch (Exception exc) {
                log.info("dataset table does not exist. Creating table now.", exc);
                createDatasetTableMysql(datasource, context);
            }
            return new DatasourceBeanPreprocessor(datasource, new MimeMap(context));
        }

        if (context instanceof NewMysqlContext) {
            NewMysqlContext c = (NewMysqlContext) context;
            BasicDataSource datasource = new BasicDataSource();
            datasource.setDriverClassName("com.mysql.jdbc.Driver"); //$NON-NLS-1$
            datasource.setUsername(c.getUsername());
            datasource.setPassword(c.getPassword());
            datasource.setRemoveAbandoned(true);
            datasource.setRemoveAbandonedTimeout(10);
            datasource.setMaxActive(10);
            datasource.setUrl(String.format("jdbc:mysql://%s:%d/%s?characterEncoding=utf8", c.getHostname(), c.getPort(), c.getDatabase())); //$NON-NLS-1$

            try {
                Connection connection = datasource.getConnection();
                Statement statement = connection.createStatement();
                statement.execute("SELECT * FROM dataset LIMIT 1;");
                statement.close();
                connection.close();
            } catch (Exception exc) {
                log.info("dataset table does not exist. Creating table now.", exc);
                createDatasetTableMysql(datasource, context);
            }
            return new DatasourceBeanPreprocessor(datasource, new MimeMap(context));
        }

        if (context instanceof ContentStoreContext) {
            return createDatasourceBeanPreprocessor(((ContentStoreContext) context).getMetadataContext());
        }

        return null;
    }

    static private void createDatasetTableMysql(final DataSource datasource, final Context context) {
        MimeMap mimemap = new MimeMap(context);

        try {
            String query;

            Connection connection = datasource.getConnection();

            // create table
            query = "CREATE TABLE `dataset` (";
            query += "  `uri` text NOT NULL,";
            query += "  `title` text NOT NULL,";
            query += "  `creator` text NOT NULL,";
            query += "  `size` int(11) NOT NULL,";
            query += "  `mimetype` varchar(45) NOT NULL,";
            query += "  `category` varchar(45) NOT NULL,";
            query += "  `date` datetime NOT NULL,";
            query += "  UNIQUE KEY `uri` (`uri`(100)),";
            query += "  KEY `title` (`title`(100)),";
            query += "  KEY `creator` (`creator`(100)),";
            query += "  KEY `size` (`size`),";
            query += "  KEY `mimetype` (`mimetype`),";
            query += "  KEY `category` (`category`),";
            query += "  KEY `date` (`date`) );";
            log.debug(query);
            Statement s = connection.createStatement();
            s.execute(query);
            s.close();

            // populate
            Unifier uf = new Unifier();
            uf.addPattern("s", Rdf.TYPE, Cet.DATASET);
            uf.addPattern("s", Dc.TITLE, "t", true);
            uf.addPattern("s", Dc.CREATOR, "c", true);
            uf.addPattern("s", Files.LENGTH, "l", true);
            uf.addPattern("s", Dc.FORMAT, "f", true);
            uf.addPattern("s", Dc.DATE, "d", true);
            uf.setColumnNames("s", "t", "c", "l", "f", "d");
            context.perform(uf);

            PreparedStatement add = connection.prepareStatement("INSERT IGNORE INTO `dataset` VALUES(?, ?, ?, ?, ?, ?, ?);");
            connection.setAutoCommit(false);

            int b = 0;
            for (Tuple<Resource> row : uf.getResult() ) {
                add.setString(1, row.get(0).getString());
                add.setString(2, (row.get(1) == null) ? "" : row.get(1).getString());
                add.setString(3, (row.get(2) == null) ? PersonBeanUtil.getAnonymousURI().getString() : row.get(2).getString());
                add.setLong(4, (row.get(3) == null) ? -1 : Long.parseLong(row.get(3).getString()));
                String mt = (row.get(4) == null) ? MimeMap.UNKNOWN_TYPE : row.get(4).getString();
                add.setString(5, mt);
                add.setString(6, mimemap.getCategory(mt));
                add.setString(7, (row.get(5) == null) ? new Date().toString() : row.get(5).getString());

                add.addBatch();
                b++;
                if (b > 10000) {
                    log.debug("Intermediate converted 10,000 datasets.");
                    add.executeBatch();
                    b = 0;
                }
            }
            log.debug("Final converted " + b + " datasets.");
            add.executeBatch();

            connection.commit();
            connection.setAutoCommit(true);
            connection.close();
        } catch (Exception exc) {
            log.error("Could not create/populate dataset table.", exc);
        }
    }

    public DatasourceBeanPreprocessor(DataSource datasource, MimeMap mimemap) {
        this.datasource = datasource;
        this.mimemap = mimemap;
    }

    public void preprocess(Object arg0) {
    }

    public void postprocess(Object arg0) {
        if (arg0 instanceof DatasetBean) {
            DatasetBean db = (DatasetBean) arg0;

            try {
                Connection connection = datasource.getConnection();

                PreparedStatement del = connection.prepareStatement("DELETE IGNORE FROM `dataset` WHERE `uri`=?;");
                del.setString(1, db.getUri());
                del.execute();

                PreparedStatement add = connection.prepareStatement("INSERT IGNORE INTO `dataset` VALUES(?, ?, ?, ?, ?, ?, ?);");
                add.setString(1, db.getUri());
                add.setString(2, db.getTitle());
                add.setString(3, db.getCreator().getUri());
                add.setLong(4, db.getSize());
                add.setString(5, db.getMimeType());
                add.setString(6, mimemap.getCategory(db.getMimeType()));
                add.setDate(7, new java.sql.Date(db.getDate().getTime()));
                add.execute();
                connection.close();
            } catch (Throwable thr) {
                thr.printStackTrace();
            }
        }
    }

    public DataSource getDataSource() {
        return datasource;
    }

}
