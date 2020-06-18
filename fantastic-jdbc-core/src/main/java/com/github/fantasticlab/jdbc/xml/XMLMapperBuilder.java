
package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.io.Resources;
import com.github.fantasticlab.jdbc.mapping.ResultFlag;
import com.github.fantasticlab.jdbc.mapping.ResultMapping;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.executor.type.JdbcType;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;
import com.github.fantasticlab.jdbc.xml.parsing.XPathParser;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * XMLMapperBuilder is a XML Mapper parser,
 * which use {@code XPathParser} to resolve XML.
 */
public class XMLMapperBuilder extends BaseBuilder {

    /* Mapper.xml */
    private String resource;
    private XPathParser parser;
    /* Sql fragment map in Mapper.xml.  Key is id, and value is SQL fragment. */
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

    public void parse() {
        configurationElement(parser.evalNode("/mapper"));
        // 绑定映射器到namespace
        bindMapperForNamespace();
    }

    public XNode getSqlFragment(String refid) {
        return sqlFragments.get(refid);
    }

    // Parse Mapper
    // ------------------------------------------
    // <mapper namespace="org.mybatis.example.BlogMapper">
    //   <select id="selectBlog" parameterType="int" resultType="Blog">
    //     select * from Blog where id = #{id}
    //   </select>
    // </mapper>
    // ------------------------------------------
    private void configurationElement(XNode context) {
        if (context == null) {
            throw new ParsingException("Need one mapper element!");
        }
        try {
            String namespace = context.getStringAttribute("namespace");
            if ("".equals(namespace)) {
                throw new ParsingException("Mapper's namespace cannot be empty");
            }
            builderAssistant.setCurrentNamespace(namespace);
            // ResultMap Parse
            resultMapElements(context.evalNodes("/mapper/resultMap"));
            // SqlFragment Parse
            sqlElements(context.evalNodes("/mapper/sql"));
            // SQL parse
            statementElements(context.evalNodes("select|insert|update|delete"));
        } catch (Exception e) {
            throw new ParsingException("Error parsing Mapper XML. Cause: " + e, e);
        }
    }

    // Parse SQL
    // ------------------------------------------
    //  <update id="updateUser">
    //		update user set name = 'Jerry' where id = #{id}
    //	</update>
    //  <select id="selectBlog" parameterType="int" resultType="Blog">
    //     select * from Blog where id = #{id}
    //  </select>
    // ------------------------------------------
    private void statementElements(List<XNode> list) {
        for (XNode context : list) {
            XMLStatementBuilder statementParser = new XMLStatementBuilder(
                    configuration, builderAssistant, context);
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

    // Parse ResultMap
    // ------------------------------------------
    //    <resultMap id="userResultMap" type="User">
    //      <id property="id" column="user_id" />
    //      <result property="username" column="username"/>
    //      <result property="password" column="password"/>
    //    </resultMap>
    // ------------------------------------------
    private void resultMapElement(XNode resultMapNode) throws Exception {
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
        builderAssistant.addResultMap(id, typeClass, resultMappings);
    }

    // Parse SQL
    // ------------------------------------------
    //   <sql id="userColumns"> id,username,password </sql>
    // ------------------------------------------
    private void sqlElements(List<XNode> list) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            id = builderAssistant.applyCurrentNamespace(id, false);
            /* Avoid SQL fragment coverage */
            if (this.sqlFragments.containsKey(id)) {
                throw new ParsingException("sqlFragments duplicate id:" + id);
            }
            sqlFragments.put(id, context);
        }
    }

    // Build ResultMapping From XNode Context,
    // each rows at below is a ResultMapping.
    // ------------------------------------------
    //  <id property="id" jdbcType="BIGINT" column="user_id" />
    //  <result property="username" jdbcType="VARCHAR" column="username"/>
    // ------------------------------------------
    private ResultMapping buildResultMappingFromContext(XNode context, Class<?> resultType, List<ResultFlag> flags) {
        String property = context.getStringAttribute("property");
        String column = context.getStringAttribute("column");
        String javaType = context.getStringAttribute("javaType");
        String jdbcType = context.getStringAttribute("jdbcType");
        Class<?> javaTypeClass = resolveClass(javaType);
        JdbcType jdbcTypeEnum = resolveJdbcType(jdbcType);
        return builderAssistant.buildResultMapping(
                resultType, property, column, javaTypeClass, jdbcTypeEnum, flags);
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
