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
import java.io.Reader;
import java.util.Properties;

/**
 * XML配置构建器
 */
public class XMLConfigBuilder {

    private boolean parsed;
    private XPathParser parser;
    private Configuration configuration;

    public XMLConfigBuilder(Reader reader) {
        this(reader, null);
    }

    public XMLConfigBuilder(Reader reader, Properties props) {
        this(new XPathParser(reader, true, props, new XMLMapperEntityResolver()), props);
    }

    public XMLConfigBuilder(InputStream inputStream) {
        this(inputStream, null);
    }

    public XMLConfigBuilder(InputStream inputStream, Properties props) {
        this(new XPathParser(inputStream, true, props, new XMLMapperEntityResolver()), props);
    }

    private XMLConfigBuilder(XPathParser parser, Properties props) {
        this.configuration = new Configuration();
        this.configuration.setVariables(props);
        this.parsed = false;
        this.parser = parser;
    }

    //解析配置
    public Configuration parse() {
        if (parsed) {
            throw new ParsingException("Each XMLConfigBuilder can only be used once.");
        }
        parsed = true;
//  <?xml version="1.0" encoding="UTF-8" ?> 
//  <!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" 
//  "http://mybatis.org/dtd/mybatis-3-config.dtd"> 
//  <configuration> 
//  <environments default="development"> 
//  <environment id="development"> 
//  <transactionManager type="JDBC"/> 
//  <dataSource type="POOLED"> 
//  <property name="driver" value="${driver}"/> 
//  <property name="url" value="${url}"/> 
//  <property name="username" value="${username}"/> 
//  <property name="password" value="${password}"/> 
//  </dataSource> 
//  </environment> 
//  </environments>
//  <mappers> 
//  <mapper resource="org/mybatis/example/BlogMapper.xml"/> 
//  </mappers> 
//  </configuration>

        // 解析/configuration
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    //解析配置
    private void parseConfiguration(XNode root) {
        try {
            propertiesElement(root.evalNode("properties"));
            mapperElement(root.evalNode("mappers"));
            dataSourceElement(root.evalNode("dataSource"));
        } catch (Exception e) {
            throw new ParsingException("Error parsing SQL Mapper Configuration. Cause: " + e, e);
        }
    }

    //1.properties
    //<properties resource="org/mybatis/example/config.properties">
    //    <property name="username" value="dev_user"/>
    //    <property name="password" value="F2Fa3!33TYyg"/>
    //</properties>
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            // resource 和 url 二选一
            if (resource != null && url != null) {
                throw new ParsingException("The properties element cannot specify both a URL and a resource based property file reference.  Please specify one or the other.");
            }
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            Properties vars = configuration.getVariables();
            if (vars != null) {
                defaults.putAll(vars);
            }
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    //2.数据源
    //<dataSource type="POOLED">
    //  <property name="driver" value="${driver}"/>
    //  <property name="url" value="${url}"/>
    //  <property name="username" value="${username}"/>
    //  <property name="password" value="${password}"/>
    //</dataSource>
    private DataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            Properties props = context.getChildrenAsProperties();
            DataSourceFactory factory = new UnpooledDataSourceFactory();
            factory.setProperties(props);
            configuration.setDataSource(factory.getDataSource());
            return factory;
        }
        throw new ParsingException("Environment declaration requires a DataSourceFactory.");
    }
    //3.自动扫描包下所有映射器
    //<mappers>
    //	 <package name="com.github.fantasticlab.jdbc.test.dao"/>
    //</mappers>
    // 或 类名
    //<mappers>
    //    <mapper class="com.github.fantasticlab.jdbc.test.dao.UserMapper" resource="mappers/UserMapper.xml"/>
    //</mappers>
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    // 扫描package种的所有mapper
                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);

                } else {
                    String mapperClass = child.getStringAttribute("class");
                    String mapperResource = child.getStringAttribute("resource");
                    if (mapperClass != null) {
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        //直接把这个映射加入配置
                        configuration.addMapper(mapperInterface);
                        if (mapperResource == null) {
                            mapperResource= "mappers/" + mapperInterface.getSimpleName() + ".xml";
                        }
                        XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(configuration, mapperResource);
                        xmlMapperBuilder.parse();
                    } else {
                        throw new ParsingException("A mapper element may only specify class, but not more than one.");
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {

        String resource = "MapperConfig.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        XMLConfigBuilder builder = new XMLConfigBuilder(inputStream);
        Configuration config = builder.parse();
        assert config != null;

    }

}
