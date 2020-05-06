
package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.io.Resources;
import com.github.fantasticlab.jdbc.mapping.ParameterMapping;
import com.github.fantasticlab.jdbc.mapping.ResultFlag;
import com.github.fantasticlab.jdbc.mapping.ResultMap;
import com.github.fantasticlab.jdbc.mapping.ResultMapping;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.transaction.type.JdbcType;
import com.github.fantasticlab.jdbc.transaction.type.TypeHandler;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;
import com.github.fantasticlab.jdbc.xml.parsing.XPathParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * XML映射构建器
 */
public class XMLMapperBuilder extends BaseBuilder {

    private String resource;
    private XPathParser parser;
    private Map<String, XNode> sqlFragments;
    private MapperBuilderAssistant builderAssistant;


    public XMLMapperBuilder(Configuration configuration, String resource) throws IOException {

        super(configuration);
        this.resource = resource;
        this.sqlFragments = configuration.getSqlFragments();
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);;
        InputStream inputStream = Resources.getResourceAsStream(resource);
        this.parser = new XPathParser(inputStream, true,
                configuration.getVariables(), new XMLMapperEntityResolver());
    }

    //解析
    public void parse() {
        // 配置Mapper
        configurationElement(parser.evalNode("/mapper"));
        // 标记已加载
        configuration.addLoadedResource(resource);
        // 绑定映射器到namespace
        bindMapperForNamespace();
    }

    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }

//	<mapper namespace="org.mybatis.example.BlogMapper">
//	  <select id="selectBlog" parameterType="int" resultType="Blog">
//	    select * from Blog where id = #{id}
//	  </select>
//	</mapper>
    private void configurationElement(XNode context) {
        try {
            String namespace = context.getStringAttribute("namespace");
            if ("".equals(namespace)) {
                throw new ParsingException("Mapper's namespace cannot be empty");
            }
            builderAssistant.setCurrentNamespace(namespace);
            // 配置resultMap
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            // 配置sql(定义可重用的 SQL 代码段)
            sqlElement(context.evalNodes("/mapper/sql"));
            // 配置select|insert|update|delete
            buildStatementFromContext(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new ParsingException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }


    // 配置select|insert|update|delete
    private void buildStatementFromContext(List<XNode> list) {
        for (XNode context : list) {
            XMLStatementBuilder statementParser =
                    new XMLStatementBuilder(configuration, builderAssistant, context);
            statementParser.parseStatementNode();
        }
    }

    private void resultMapElements(List<XNode> list) throws Exception {
        if (list == null) {
            throw new ParsingException("resultMap not exist");
        }
        if (list.size() != 1) {
            throw new ParsingException("resultMap has more than one");
        }
        resultMapElement(list.get(0));
    }

    //    <resultMap id="userResultMap" type="User">
    //      <id property="id" column="user_id" />
    //      <result property="username" column="username"/>
    //      <result property="password" column="password"/>
    //    </resultMap>
    private ResultMap resultMapElement(XNode resultMapNode) throws Exception {
        String id = resultMapNode.getStringAttribute("id",
                resultMapNode.getValueBasedIdentifier());
        String type = resultMapNode.getStringAttribute("type");

        Class<?> typeClass = resolveClass(type);
        List<ResultMapping> resultMappings = new ArrayList<>();
        List<XNode> resultChildren = resultMapNode.getChildren();
        for (XNode resultChild : resultChildren) {
            List<ResultFlag> flags = new ArrayList<ResultFlag>();
            if ("id".equals(resultChild.getName())) {
                flags.add(ResultFlag.ID);
            }
            resultMappings.add(buildResultMappingFromContext(resultChild, typeClass, flags));
        }

        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, typeClass, resultMappings);
        return resultMapResolver.resolve();
    }


    // 配置sql(定义可重用的 SQL 代码段)
    // <sql id="userColumns"> id,username,password </sql>
    private void sqlElement(List<XNode> list) throws Exception {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            if (this.sqlFragments.containsKey(id)) {
                throw new ParsingException("sqlFragments duplicate id:" + id);
            }
            sqlFragments.put(id, context);
        }
    }

    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) {
        //<id property="id" column="author_id"/>
        //<result property="username" column="author_username"/>
        String property = context.getStringAttribute("property");
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        String typeHandler = context.getStringAttribute("typeHandler");
        String resulSet = context.getStringAttribute("resultSet");
        Class<?> javaTypeClass = resolveClass(javaType);
        Class<? extends TypeHandler<?>> typeHandlerClass
                = (Class<? extends TypeHandler<?>>) resolveClass(typeHandler);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return builderAssistant.buildResultMapping(
                resultType,
                property,
                column,
                javaTypeClass,
                jdbcTypeEnum,
                typeHandlerClass,
                flags,
                resulSet);
    }

    private void bindMapperForNamespace() {
        String namespace = builderAssistant.getCurrentNamespace();
        if (namespace != null) {
            Class<?> boundType = null;
            try {
                boundType = Resources.classForName(namespace);
            } catch (ClassNotFoundException e) {
                //ignore, bound type is not required
            }
            if (boundType != null) {
                if (!configuration.hasMapper(boundType)) {
                    configuration.addMapper(boundType);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Configuration configuration = new Configuration();
        String resource = "mappers/UserMapper.xml";
        XMLMapperBuilder builder = new XMLMapperBuilder(configuration, resource);
        builder.parse();
        assert builder != null;
    }

}
