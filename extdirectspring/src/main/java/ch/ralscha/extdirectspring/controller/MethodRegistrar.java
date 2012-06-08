/**
 * Copyright 2010-2012 Ralph Schaer <ralphschaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.ralscha.extdirectspring.controller;

import java.lang.reflect.Method;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;
import org.springframework.util.StringUtils;

import ch.ralscha.extdirectspring.annotation.ExtDirectMethod;
import ch.ralscha.extdirectspring.util.ExtDirectSpringUtil;
import ch.ralscha.extdirectspring.util.MethodInfoCache;

@Service
public class MethodRegistrar implements ApplicationListener<ContextRefreshedEvent> {

	private static final Log log = LogFactory.getLog(RouterController.class);

	public void onApplicationEvent(final ContextRefreshedEvent event) {

		ApplicationContext context = (ApplicationContext) event.getSource();

		String[] beanNames = context.getBeanNamesForType(Object.class);

		for (String beanName : beanNames) {

			Class<?> handlerType = context.getType(beanName);
			final Class<?> userType = ClassUtils.getUserClass(handlerType);

			Set<Method> methods = ExtDirectSpringUtil.selectMethods(userType, new MethodFilter() {
				public boolean matches(final Method method) {
					return AnnotationUtils.findAnnotation(method, ExtDirectMethod.class) != null;
				}
			});

			for (Method method : methods) {
				ExtDirectMethod directMethodAnnotation = AnnotationUtils.findAnnotation(method, ExtDirectMethod.class);
				final String beanMethodName = beanName + "." + method.getName();
				if (directMethodAnnotation.value().isValid(beanMethodName, userType, method)) {
					MethodInfoCache.INSTANCE.put(beanName, handlerType, method);

					if (log.isDebugEnabled()) {
						String info = "Register " + beanMethodName + "(" + directMethodAnnotation.value();
						if (StringUtils.hasText(directMethodAnnotation.group())) {
							info += ", " + directMethodAnnotation.group();
						}
						info += ")";
						log.debug(info);
					}
				}
			}

		}
	}

}
