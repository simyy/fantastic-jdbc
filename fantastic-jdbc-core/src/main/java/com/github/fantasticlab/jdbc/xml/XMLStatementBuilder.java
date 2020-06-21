package com.github.fantasticlab.jdbc.xml;

import com.github.fantasticlab.jdbc.executor.key.NoKeyGenerator;
import com.github.fantasticlab.jdbc.executor.mapping.*;
import com.github.fantasticlab.jdbc.scripting.LanguageDriver;
import com.github.fantasticlab.jdbc.scripting.XMLLanguageDriver;
import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;

import java.util.Locale;

/**
 * XMLStatementBuilder is a builder for {@code MappedStatement}.
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

    // SQL Parse
    // ------------------------------------------
    // <select id="selectPerson" parameterType="int" resultType="Map" resultMap="personResultMap">
    //   SELECT * FROM PERSON WHERE ID = #{id}
    // </select>
    // ------------------------------------------
    public void parseStatementNode() {
        String id = context.getStringAttribute("id");
        if (id == null) {
            throw new ParsingException("Statement id is null!");
        }
        String parameterType = context.getStringAttribute("parameterType");
        String resultType = context.getStringAttribute("resultType");
        Class<?> parameterTypeClass = resolveClass(parameterType);
        Class<?> resultTypeClass = resolveClass(resultType);
        /* NodeName is one of select/insert/update/delete. */
        String nodeName = context.getNode().getNodeName();
        SqlCommandType sqlCommandType = SqlCommandType.valueOf(nodeName.toUpperCase(Locale.ENGLISH));

        LanguageDriver langDriver = new XMLLanguageDriver();
        SqlSource sqlSource = langDriver.createSqlSource(configuration, context, parameterTypeClass);

//        // Include Fragments before parsing TODO
//        XMLIncludeTransformer includeParser = new XMLIncludeTransformer(configuration, builderAssistant);
//        includeParser.applyIncludes(context.getNode());

        builderAssistant.addMappedStatement(
                id, sqlSource, sqlCommandType, parameterTypeClass, resultTypeClass, new NoKeyGenerator());
    }

}
