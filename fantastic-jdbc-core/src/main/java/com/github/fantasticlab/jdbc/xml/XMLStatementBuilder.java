package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.executor.keygen.KeyGenerator;
import com.github.fantasticlab.jdbc.executor.keygen.NoKeyGenerator;
import com.github.fantasticlab.jdbc.mapping.*;
import com.github.fantasticlab.jdbc.scripting.LanguageDriver;
import com.github.fantasticlab.jdbc.scripting.XMLLanguageDriver;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;

import java.util.List;
import java.util.Locale;

/**
 * XML语句构建器
 */
public class XMLStatementBuilder extends BaseBuilder {

    private MapperBuilderAssistant builderAssistant;
    private XNode context;


    public XMLStatementBuilder(Configuration configuration,
                               MapperBuilderAssistant builderAssistant,
                               XNode context) {
        super(configuration);
        this.builderAssistant = builderAssistant;
        this.context = context;
    }

    //解析语句(select|insert|update|delete)
    //<select
    //  id="selectPerson"
    //  parameterType="int"
    //  resultType="hashmap"
    //  resultMap="personResultMap">
    //  SELECT * FROM PERSON WHERE ID = #{id}
    //</select>
    public void parseStatementNode() {
        String id = context.getStringAttribute("id");
//        if (!hasStatement(id)) {
//            return;
//        }
        String parameterType = context.getStringAttribute("parameterType");
        Class<?> parameterTypeClass = resolveClass(parameterType);
        String resultMap = context.getStringAttribute("resultMap");
        String resultType = context.getStringAttribute("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);

//        String lang = context.getStringAttribute("lang");
//        LanguageDriver langDriver = getLanguageDriver(lang);

        LanguageDriver langDriver = new XMLLanguageDriver();

        String nodeName = context.getNode().getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);

//        // Include Fragments before parsing
//        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
//        includeParser.applyIncludes(context.getNode());

//        // Parse selectKey after includes and remove them.
//        processSelectKeyNodes(id, parameterTypeClass, langDriver);

        // Parse the SQL (pre: <selectKey> and <include> were parsed and removed)
//        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);
//        String resultSets = context.getStringAttribute("resultSets");
//        //(仅对 insert 有用) 标记一个属性, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值
//        String keyProperty = context.getStringAttribute("keyProperty");
//        //(仅对 insert 有用) 标记一个属性, MyBatis 会通过 getGeneratedKeys 或者通过 insert 语句的 selectKey 子元素设置它的值
//        String keyColumn = context.getStringAttribute("keyColumn");
//        KeyGenerator keyGenerator = new NoKeyGenerator();
        ;

        builderAssistant.addMappedStatement(id, sqlSource,
                sqlCommandType, parameterTypeClass, resultMap,
                resultTypeClass, new NoKeyGenerator());
    }

    private void processSelectKeyNodes(String id, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        List<XNode> selectKeyNodes = context.evalNodes("selectKey");
        parseSelectKeyNodes(id, selectKeyNodes, parameterTypeClass, langDriver);
        removeSelectKeyNodes(selectKeyNodes);
    }

    private void parseSelectKeyNodes(String parentId, List<XNode> list, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        for (XNode nodeToHandle : list) {
            if (hasStatement(parentId)) {
                parseSelectKeyNode(parentId, nodeToHandle, parameterTypeClass, langDriver);
            }
        }
    }

    private void parseSelectKeyNode(String id, XNode nodeToHandle, Class<?> parameterTypeClass, LanguageDriver langDriver) {
        String resultType = nodeToHandle.getStringAttribute("resultType");
        Class<?> resultTypeClass = resolveClass(resultType);
        String keyProperty = nodeToHandle.getStringAttribute("keyProperty");
        String keyColumn = nodeToHandle.getStringAttribute("keyColumn");
        boolean executeBefore = "BEFORE".equals(
                nodeToHandle.getStringAttribute("order", "AFTER"));

        KeyGenerator keyGenerator = new NoKeyGenerator();
        String parameterMap = null;
        String resultMap = null;
        ResultSetType resultSetTypeEnum = null;

        SqlSource sqlSource = langDriver.createSqlSource(
                configuration, nodeToHandle, parameterTypeClass);
        SqlCommandType sqlCommandType = SqlCommandType.SELECT;

        builderAssistant.addMappedStatement(
                id,
                sqlSource,
                sqlCommandType,
                parameterTypeClass,
                resultMap,
                resultTypeClass,
                keyGenerator);

//        id = builderAssistant.applyCurrentNamespace(id, false);
//        MappedStatement keyStatement = configuration.getMappedStatement(id, false);
//        configuration.addKeyGenerator(id, new SelectKeyGenerator(keyStatement, executeBefore));
    }

    private void removeSelectKeyNodes(List<XNode> selectKeyNodes) {
        for (XNode nodeToHandle : selectKeyNodes) {
            nodeToHandle.getParent().getNode().removeChild(nodeToHandle.getNode());
        }
    }

    private boolean hasStatement(String id) {
        // skip this statement if there is a previous one
        id = builderAssistant.applyCurrentNamespace(id, false);
        if (this.configuration.hasStatement(id)) {
            return true;
        }
        return false;
    }

//    //取得语言驱动
//    private LanguageDriver getLanguageDriver(String lang) {
//        Class<?> langClass = null;
//        if (lang != null) {
//            langClass = resolveClass(lang);
//        }
//        //调用builderAssistant
//        return builderAssistant.getLanguageDriver(langClass);
//    }

}
