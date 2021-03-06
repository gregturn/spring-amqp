/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.amqp.rabbit.config;

import org.w3c.dom.Element;

import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;

/**
 * @author Gary Russell
 *
 */
@SuppressWarnings("deprecation")
public class FederatedExchangeParser extends AbstractExchangeParser {

	private static final String BACKING_TYPE_ATTRIBUTE = "backing-type";

	private static final String UPSTREAM_SET_ATTRIBUTE = "upstream-set";

	private static final String DIRECT_BINDINGS_ELE = "direct-bindings";

	private static final String TOPIC_BINDINGS_ELE = "topic-bindings";

	private static final String TOPIC_FANOUT_ELE = "fanout-bindings";

	private static final String TOPIC_HEADERS_ELE = "headers-bindings";

	@Override
	protected Class<?> getBeanClass(Element element) {
		return org.springframework.amqp.core.FederatedExchange.class;
	}


	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {
		super.doParse(element, parserContext, builder);
		NamespaceUtils.setValueIfAttributeDefined(builder, element, BACKING_TYPE_ATTRIBUTE);
		NamespaceUtils.setValueIfAttributeDefined(builder, element, UPSTREAM_SET_ATTRIBUTE);
	}


	@Override
	protected void parseBindings(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder, String exchangeName) {
		String backingType = element.getAttribute(BACKING_TYPE_ATTRIBUTE);
		Element bindingsElement;
		bindingsElement = DomUtils.getChildElementByTagName(element, DIRECT_BINDINGS_ELE);
		if (bindingsElement != null && !ExchangeTypes.DIRECT.equals(backingType)) {
			parserContext.getReaderContext().error(
					"Cannot have direct-bindings if backing-type not 'direct'",
					element);
		}
		if (bindingsElement == null) {
			bindingsElement = DomUtils.getChildElementByTagName(element, TOPIC_BINDINGS_ELE);
			if (bindingsElement != null && !ExchangeTypes.TOPIC.equals(backingType)) {
				parserContext.getReaderContext().error(
						"Cannot have topic-bindings if backing-type not 'topic'",
						element);
			}
		}
		if (bindingsElement == null) {
			bindingsElement = DomUtils.getChildElementByTagName(element, TOPIC_FANOUT_ELE);
			if (bindingsElement != null && !ExchangeTypes.FANOUT.equals(backingType)) {
				parserContext.getReaderContext().error(
						"Cannot have fanout-bindings if backing-type not 'fanout'",
						element);
			}
		}
		if (bindingsElement == null) {
			bindingsElement = DomUtils.getChildElementByTagName(element, TOPIC_HEADERS_ELE);
			if (bindingsElement != null && !ExchangeTypes.HEADERS.equals(backingType)) {
				parserContext.getReaderContext().error(
						"Cannot have headers-bindings if backing-type not 'headers'",
						element);
			}
		}
		if (StringUtils.hasText(backingType)) {
			if (ExchangeTypes.DIRECT.equals(backingType)) {
				doParseBindings(element, parserContext, exchangeName, bindingsElement, new DirectExchangeParser());
			}
			else if (ExchangeTypes.TOPIC.equals(backingType)) {
				doParseBindings(element, parserContext, exchangeName, bindingsElement, new TopicExchangeParser());
			}
			else if (ExchangeTypes.FANOUT.equals(backingType)) {
				doParseBindings(element, parserContext, exchangeName, bindingsElement, new FanoutExchangeParser());
			}
			else if (ExchangeTypes.HEADERS.equals(backingType)) {
				doParseBindings(element, parserContext, exchangeName, bindingsElement, new HeadersExchangeParser());
			}
		}
	}

	@Override
	protected BeanDefinitionBuilder parseBinding(String exchangeName, Element binding,
			ParserContext parserContext) {
		throw new UnsupportedOperationException("Not supported for federated exchange");
	}

}
