/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.session.jdbc;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Integration tests for {@link JdbcIndexedSessionRepository} using PostgreSQL database
 * with {@link PostgreSqlJdbcIndexedSessionRepositoryCustomizer}.
 *
 * @author Rune Flobakk
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
class PostgreSqlJdbcIndexedSessionRepositoryAdaptedQueriesITests extends PostgreSqlJdbcIndexedSessionRepositoryCustomizerITests {

	@Configuration
	static class CustomizerConfig extends Config {

		@Bean
		BeanPostProcessor addColumnToSessionTable() {
			return new BeanPostProcessor() {
				@Override
				public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
					if (bean instanceof ResourceDatabasePopulator databasePopulator) {
						databasePopulator.addScript(new ByteArrayResource("""
						        ALTER TABLE spring_session ADD COLUMN CANDIDATE_ID TEXT
						        """.getBytes(UTF_8)));
					}
					return bean;
				}
			};
		}

		@Bean
		SessionRepositoryCustomizer<JdbcIndexedSessionRepository> postgreSqlJdbcIndexedSessionRepositoryQueriesAdapter() {
			return sessionRepository -> sessionRepository.adaptCreateSessionQuery(query -> query
					.replaceQuery("""
				        INSERT INTO %TABLE_NAME% (
					              PRIMARY_ID, SESSION_ID, CREATION_TIME, LAST_ACCESS_TIME,
					              MAX_INACTIVE_INTERVAL, EXPIRY_TIME, PRINCIPAL_NAME, CANDIDATE_ID)
						VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				        """)
					.setAdditionalParams((ps, session) -> ps.setString(8, session.getAttribute("candidate_id"))));
		}

	}

}
