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

import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.util.StringUtils;

public record QueryAndParams<S> (String query, ParameterizedPreparedStatementSetter<S> paramsSetter) {

	public QueryAndParams<S> replaceQuery(String query) {
		return new QueryAndParams<>(query, paramsSetter);
	}

	public QueryAndParams<S> setAdditionalParams(ParameterizedPreparedStatementSetter<? super S> additionalParams) {
		return new QueryAndParams<>(query, (ps, session) -> {
			this.paramsSetter.setValues(ps, session);
			additionalParams.setValues(ps, session);
		});
	}

	PreparedStatementSetter preparedStatementSetterFor(S argument) {
		return ps -> paramsSetter.setValues(ps, argument);
	}

	QueryAndParams<S> withTableName(String tableName) {
		return replaceQuery(StringUtils.replace(query, "%TABLE_NAME%", tableName));
	}
}
