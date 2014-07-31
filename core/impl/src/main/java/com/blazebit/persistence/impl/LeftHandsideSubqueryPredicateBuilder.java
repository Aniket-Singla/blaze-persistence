/*
 * Copyright 2014 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.persistence.impl;

import com.blazebit.persistence.HavingAndBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.impl.expression.SubqueryExpression;

/**
 *
 * @author ccbem
 */
public class LeftHandsideSubqueryPredicateBuilder<T> extends SubqueryBuilderListenerImpl<T> {

    @Override
    public void onBuilderEnded(SubqueryBuilder<T> builder) {
        super.onBuilderEnded(builder);
        RestrictionBuilderImpl<HavingAndBuilder<T>> restrictionBuilder = (RestrictionBuilderImpl<HavingAndBuilder<T>>) builder.getResult();
        restrictionBuilder.setLeftExpression(new SubqueryExpression(builder));
    }
}
