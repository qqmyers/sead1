package edu.illinois.ncsa.medici.ingest;

import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.ContentStoreContext;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.beans.SaveBeanPreprocessor;
import org.tupeloproject.mysql.MysqlContext;
import org.tupeloproject.mysql.NewMysqlContext;

import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.tupelo.util.MimeMap;

public class DatasourceBeanPreprocessor implements SaveBeanPreprocessor {
    private static Log       log = LogFactory.getLog(DatasourceBeanPreprocessor.class);

    private DataSource       datasource;
    private MimeMap          mimemap;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static public void setDatasourceBeanPreprocessor(BeanSession beansession) {
        beansession.setBeanPreprocessor(checkContext(beansession.getContext()));
    }

    static private DatasourceBeanPreprocessor checkContext(Context context) {
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
                return new DatasourceBeanPreprocessor(datasource, new MimeMap(context));
            } catch (Exception exc) {
                exc.printStackTrace();
            }
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
                return new DatasourceBeanPreprocessor(datasource, new MimeMap(context));
            } catch (Exception exc) {
                exc.printStackTrace();
            }
        }

        if (context instanceof ContentStoreContext) {
            return checkContext(((ContentStoreContext) context).getMetadataContext());
        }

        return null;
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
                Statement statement = connection.createStatement();
                String query = String.format("DELETE FROM `dataset` WHERE `uri`='%s'", db.getUri());
                statement.execute(query);
                query = String.format("INSERT IGNORE INTO `dataset` VALUES('%s', '%s', %d, '%s', '%s', '%s')",
                        db.getUri(), db.getTitle(), db.getSize(), db.getMimeType(), mimemap.getCategory(db.getMimeType()), sdf.format(db.getDate()));
                log.debug(query);
                statement.execute(query);
                statement.close();
                connection.close();
            } catch (Throwable thr) {
                thr.printStackTrace();
            }
        }
    }

}
