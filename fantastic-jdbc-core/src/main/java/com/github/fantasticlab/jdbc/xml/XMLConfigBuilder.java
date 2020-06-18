package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.datasource.DataSourceFactory;
import com.github.fantasticlab.jdbc.datasource.UnpooledDataSourceFactory;
import com.github.fantasticlab.jdbc.io.Resources;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;
import com.github.fantasticlab.jdbc.xml.parsing.XPathParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * XMLConfigBuilder is a XML config parser,
 * which use {@code XPathParser} to resolve XML.
 */
public class XMLConfigBuilder {

    private XPathParser parser;
    private Configuration configuration;

    public XMLConfigBuilder(InputStream inputStream) {
        this(new XPathParser(inputStream, true, null, new XMLMapperEntityResolver()));
    }

    private XMLConfigBuilder(XPathParser parser) {
        this.configuration = new Configuration();
        this.parser = parser;
    }

    // Parse configuration, and below is the format of configuration,
    // ------------------------------------------
    //  <?xml version="1.0" encoding="UTF-8" ?>
    //  <!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
    //    "http://mybatis.org/dtd/mybatis-3-config.dtd">
    //  <configuration>
    //    <environments default="development">
    //      <environment id="development">
    //        <transactionManager type="JDBC"/>
    //        <dataSource type="POOLED">
    //          <property name="driver" value="${driver}"/>
    //          <property name="url" value="${url}"/>
    //          <property name="username" value="${username}"/>
    //          <property name="password" value="${password}"/>
    //        </dataSource>
    //      </environment>
    //    </environments>
    //    <mappers>
    //      <mapper resource="org/mybatis/example/BlogMapper.xml"/>
    //    </mappers>
    //  </configuration>
    // ------------------------------------------
    public Configuration parseConfiguration() {
        try {
            XNode root = parser.evalNode("/configuration");
            propertiesElement(root.evalNode("properties"));
            mapperElement(root.evalNode("mappers"));
            dataSourceElement(root.evalNode("dataSource"));
            return configuration;
        } catch (Exception e) {
            throw new ParsingException("Error parsing Configuration. Cause: " + e, e);
        }
    }

    // Parse properties
    // ------------------------------------------
    // <properties resource="org/mybatis/example/config.properties">
    //   <property name="username" value="dev_user"/>
    //   <property name="password" value="F2Fa3!33TYyg"/>
    // </properties>
    // ------------------------------------------
    private void propertiesElement(XNode context) throws Exception {
        if (context == null) {
            throw new ParsingException("Environment declaration requires a properties." +
                    " This is a Test, it may not include");
        }
        Properties properties = context.getChildrenAsProperties();
        String resource = context.getStringAttribute("resource");
        String url = context.getStringAttribute("url");
        /* Only One Way */
        if (resource != null && url != null) {
            throw new ParsingException(
                    "The properties element cannot specify both a URL " +
                            "and a resource based property file reference.  " +
                            "Please specify one or the other.");
        }
        if (resource != null) {
            properties.putAll(Resources.getResourceAsProperties(resource));
        } else if (url != null) {
            properties.putAll(Resources.getUrlAsProperties(url));
        }
        parser.setVariables(properties);
        configuration.addVariables(properties);
    }

    // Parse DataSource
    // ------------------------------------------
    // <dataSource type="POOLED">
    //  <property name="driver" value="${driver}"/>
    //  <property name="url" value="${url}"/>
    //  <property name="username" value="${username}"/>
    //  <property name="password" value="${password}"/>
    // </dataSource>
    // ------------------------------------------
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context == null) {
            throw new ParsingException("Environment declaration requires a dataSource.");
        }
        Properties props = context.getChildrenAsProperties();
        DataSourceFactory factory = new UnpooledDataSourceFactory(configuration, props);
        configuration.setDataSource(factory.getDataSource());
        return factory;
    }

    // Parse Mapper
    // ------------------------------------------
    // <mappers>
    //	 <package name="com.github.fantasticlab.jdbc.test.dao"/>
    // </mappers>
    // <mappers>
    //    <mapper class="com.github.fantasticlab.jdbc.test.dao.UserMapper" resource="mappers/UserMapper.xml"/>
    // </mappers>
    // ------------------------------------------
    private void mapperElement(XNode parent) throws Exception {
        if (parent == null) {
            throw new ParsingException("Environment declaration requires a mappers.");
        }
        for (XNode child : parent.getChildren()) {

            if ("package".equals(child.getName())) {
                /* Scan mapper from package */
                String mapperPackage = child.getStringAttribute("name");
                configuration.addMappers(mapperPackage);
            } else {
                /* Scan mapper from mapper config */
                /* Register Mapper */
                String mapperClass = child.getStringAttribute("class");
                if (mapperClass == null) {
                    throw new ParsingException("A mapper need to contain a specify class");
                }
                Class<?> mapperInterface = Resources.classForName(mapperClass);
                configuration.addMapper(mapperInterface);
                String mapperResource = child.getStringAttribute("resource");
                if (mapperResource == null) {
                    /* default Mapper.xml */
                    mapperResource= "mappers/" + mapperInterface.getSimpleName() + ".xml";
                }
                /* Parse SQL from Mapper.xml */
                XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration, mapperResource);
                xmlMapperBuilder.parse();

            }
        }
    }

    public static void main(String[] args) throws IOException {

        String resource = "MapperConfig.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
        Configuration config = builder.parseConfiguration();
        assert config != null;

    }

}
