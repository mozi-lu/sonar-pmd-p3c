/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.plugins.pmd.rule;

import com.alibaba.p3c.pmd.I18nResources;
import com.google.common.collect.Lists;
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 描述：阿里P3C规范加载器<br>
 * 版权：Copyright (c) 2019<br>
 * 公司：北京活力天汇<br>
 * 作者：陆思骏<br>
 * 版本：1.0<br>
 * 创建日期：2019-10-22<br>
 *
 * @author 陆思骏
 */
class AliRulesLoader {

    private AliRulesLoader() {
    }

    private static final Logger LOGGER = Loggers.get(AliRulesLoader.class);

    private static List<String> configPaths = Lists.newArrayList();

    static {
        configPaths.add("rulesets/java/ali-comment.xml");
        configPaths.add("rulesets/java/ali-concurrent.xml");
        configPaths.add("rulesets/java/ali-constant.xml");
        configPaths.add("rulesets/java/ali-exception.xml");
        configPaths.add("rulesets/java/ali-flowcontrol.xml");
        configPaths.add("rulesets/java/ali-naming.xml");
        configPaths.add("rulesets/java/ali-oop.xml");
        configPaths.add("rulesets/java/ali-orm.xml");
        configPaths.add("rulesets/java/ali-other.xml");
        configPaths.add("rulesets/java/ali-set.xml");
    }

    static void load(RulesDefinition.NewRepository repository) {
        for (String configPath : configPaths) {
            List<Rule> rules = readConfigXml("/" + configPath);
            String rulesXml = parseRules(configPath, rules);
            if (rulesXml.contains("<description><![CDATA[]]>")) {
                LOGGER.info(rulesXml);
            }
            try (InputStream inputStream = new ReaderInputStream(new StringReader(rulesXml), StandardCharsets.UTF_8)) {
                new RulesDefinitionXmlLoader()
                        .load(
                                repository,
                                inputStream,
                                StandardCharsets.UTF_8
                        );
            } catch (IOException e) {
                LOGGER.error("Failed to load P3C PMD RuleSet.", e);
            }
        }
    }

    private static List<Rule> readConfigXml(String path) {
        URL resource = I18nResources.class.getResource(path);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
            XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
            xmlFactory.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
            xmlFactory.setProperty("javax.xml.stream.isNamespaceAware", Boolean.FALSE);
            xmlFactory.setProperty("javax.xml.stream.supportDTD", Boolean.FALSE);
            xmlFactory.setProperty("javax.xml.stream.isValidating", Boolean.FALSE);
            SMInputFactory inputFactory = new SMInputFactory(xmlFactory);
            SMHierarchicCursor rootCursor = inputFactory.rootElementCursor(reader);
            rootCursor.advance();
            SMInputCursor ruleCursor = rootCursor.childElementCursor("rule");

            List<Rule> rules = Lists.newArrayList();
            while (ruleCursor.getNext() != null) {
                Rule rule = processRule(ruleCursor);
                rules.add(rule);
            }
            return rules;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read: " + resource, e);
        }
    }

    private static Rule processRule(SMInputCursor ruleCursor) throws XMLStreamException {
        Rule rule = new Rule();
        String name = ruleCursor.getAttrValue("name");
        if (StringUtils.isNotBlank(name)) {
            rule.name = StringUtils.trim(name);
        }
        String message = ruleCursor.getAttrValue("message");
        if (StringUtils.isNotBlank(message)) {
            rule.message = StringUtils.trim(message);
        }
        String clazz = ruleCursor.getAttrValue("class");
        if (StringUtils.isNotBlank(clazz)) {
            rule.clazz = StringUtils.trim(clazz);
        }

        SMInputCursor cursor = ruleCursor.childElementCursor();
        while (cursor.getNext() != null) {
            String nodeName = cursor.getLocalName();
            if (StringUtils.equalsIgnoreCase("description", nodeName)) {
                rule.description = StringUtils.trim(cursor.collectDescendantText());
            }
            if (StringUtils.equalsIgnoreCase("priority", nodeName)) {
                rule.priority = Integer.valueOf(StringUtils.trim(cursor.collectDescendantText()));
            }
            if (StringUtils.equalsIgnoreCase("example", nodeName)) {
                String example = StringUtils.trim(cursor.collectDescendantText());
                if (rule.examples == null) {
                    rule.examples = Lists.newArrayList();
                }
                rule.examples.add(example);
            }
        }
        return rule;
    }

    private static String parseRules(String configPath, List<Rule> rules) {
        StringBuilder builder = new StringBuilder();
        builder.append("<rules>");
        for (Rule rule : rules) {
            buildRule(builder, configPath, rule);
        }
        builder.append("</rules>");
        return builder.toString();
    }

    private static void buildRule(StringBuilder builder, String configPath, Rule rule) {
        builder.append("<rule key=\"");
        builder.append(rule.name);
        builder.append("\">");

        builder.append("<tag>p3c</tag>");

        builder.append("<name><![CDATA[");
        builder.append(I18nResources.getMessage(rule.message));
        builder.append("]]></name>");

        builder.append("<priority>");

        switch (rule.priority) {
            case 1:
                builder.append("BLOCKER");
                break;
            case 2:
                builder.append("CRITICAL");
                break;
            case 3:
                builder.append("MAJOR");
                break;
            case 4:
                builder.append("MINOR");
                break;
            default:
                builder.append("INFO");
        }
        builder.append("</priority>");

        builder.append("<description><![CDATA[");
        if (rule.description == null && rule.examples == null) {
            builder.append("暂无介绍");
        }
        if (rule.description != null) {
            String description = I18nResources.getMessage(rule.description);
            for (String line : StringUtils.split(description, System.lineSeparator())) {
                builder.append("<p>");
                builder.append(StringEscapeUtils.escapeHtml(line));
                builder.append("</p>");
            }
        }
        if (rule.examples != null) {
            for (String example : rule.examples) {
                builder.append("<pre>");
                builder.append(StringEscapeUtils.escapeHtml(example));
                builder.append("</pre>");
            }
        }
        builder.append("]]></description>");

        builder.append("<configKey>");
        builder.append(configPath);
        builder.append("/");
        builder.append(rule.name);
        builder.append("</configKey>");

        builder.append("</rule>");
    }

    private static class Rule {
        String name;
        String message;
        String clazz;
        String description;
        Integer priority;
        List<String> examples;
    }

}
