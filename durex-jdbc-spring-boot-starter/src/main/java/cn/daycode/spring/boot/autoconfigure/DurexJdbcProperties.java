package cn.daycode.spring.boot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for durex-jdbc.
 *
 * @author zch
 * @since 2018/6/12
 */
@ConfigurationProperties(prefix = DurexJdbcProperties.DUREX_JDBC_PREFIX)
public class DurexJdbcProperties {

    public static final String DUREX_JDBC_PREFIX = "durex.jdbc";

    private static final String DEFAULT_SUFFIX = ".sql";


    private String directory;

    private String suffix = DEFAULT_SUFFIX;

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


}
