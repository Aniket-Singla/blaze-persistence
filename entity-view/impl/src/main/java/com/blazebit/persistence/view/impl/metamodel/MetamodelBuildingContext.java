/*
 * Copyright 2014 - 2017 Blazebit.
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

package com.blazebit.persistence.view.impl.metamodel;

import com.blazebit.persistence.impl.EntityMetamodel;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.spi.JpqlFunction;
import com.blazebit.persistence.view.impl.proxy.ProxyFactory;
import com.blazebit.persistence.view.metamodel.Type;

import java.util.Map;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public interface MetamodelBuildingContext {

    public <X> Type<X> getBasicType(Class<X> basicClass);

    public Map<String, JpqlFunction> getJpqlFunctions();

    public EntityMetamodel getEntityMetamodel();

    public ExpressionFactory getExpressionFactory();

    public ExpressionFactory createMacroAwareExpressionFactory();

    public ExpressionFactory createMacroAwareExpressionFactory(String viewRoot);

    public ProxyFactory getProxyFactory();

    public void addError(String error);

    public boolean hasErrors();

    public boolean isEntityView(Class<?> clazz);
}
