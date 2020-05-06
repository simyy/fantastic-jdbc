package com.github.fantasticlab.jdbc.xml;


import com.github.fantasticlab.jdbc.session.Configuration;
import com.github.fantasticlab.jdbc.xml.parsing.ParsingException;
import com.github.fantasticlab.jdbc.xml.parsing.PropertyParser;
import com.github.fantasticlab.jdbc.xml.parsing.XNode;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * XML include转换器
 */
public class XMLIncludeTransformer {

    private final Configuration configuration;
    private final MapperBuilderAssistant builderAssistant;

    public XMLIncludeTransformer(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        this.configuration = configuration;
        this.builderAssistant = builderAssistant;
    }

    //<select id="selectUsers" resultType="map">
    //  select <include refid="userColumns"/>
    //  from some_table
    //  where id = #{id}
    //</select>
    public void applyIncludes(Node source) {
        if (source.getNodeName().equals("include")) {
            //走到这里，单独解析<include refid="userColumns"/>
            Node toInclude = findSqlFragment(getStringAttribute(source, "refid"));
            //递归调用自己
            applyIncludes(toInclude);
            //总之下面就是将字符串拼接进来，看不懂。。。
            if (toInclude.getOwnerDocument() != source.getOwnerDocument()) {
                toInclude = source.getOwnerDocument().importNode(toInclude, true);
            }
            source.getParentNode().replaceChild(toInclude, source);
            while (toInclude.hasChildNodes()) {
                toInclude.getParentNode().insertBefore(toInclude.getFirstChild(), toInclude);
            }
            toInclude.getParentNode().removeChild(toInclude);
        } else if (source.getNodeType() == Node.ELEMENT_NODE) {
            // 遍历所有子include节点
            NodeList children = source.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                applyIncludes(children.item(i));
            }
        }
    }

    private Node findSqlFragment(String refid) {
        refid = PropertyParser.parse(refid, configuration.getVariables());
        refid = builderAssistant.applyCurrentNamespace(refid, true);
        try {
            XNode nodeToInclude = configuration.getSqlFragments().get(refid);
            return nodeToInclude.getNode().cloneNode(true);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Could not find SQL statement to include with refid '" + refid + "'", e);
        }
    }

    private String getStringAttribute(Node node, String name) {
        return node.getAttributes().getNamedItem(name).getNodeValue();
    }
}
