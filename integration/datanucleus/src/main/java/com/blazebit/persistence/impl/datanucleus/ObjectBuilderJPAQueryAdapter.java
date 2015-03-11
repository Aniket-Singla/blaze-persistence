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
package com.blazebit.persistence.impl.datanucleus;

import com.blazebit.persistence.ObjectBuilder;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

/**
 *
 * @author Christian Beikov
 * @since 1.0
 */
// TODO: Document that invoking methods on unwrapped query are not yet supported
public class ObjectBuilderJPAQueryAdapter<X> extends TypedQueryWrapper<X> {

    private final ObjectBuilder<X> builder;

    public ObjectBuilderJPAQueryAdapter(TypedQuery<?> delegate, ObjectBuilder<X> builder) {
        super((TypedQuery<X>) delegate);
        this.builder = builder;
    }

    @Override
    public X getSingleResult() {
        List<X> list = getResultList();
        
        switch (list.size()) {
            case 0:
                throw new NoResultException("No results for query: " + delegate);
            case 1:
                return list.get(0);
            default:
                throw new NonUniqueResultException("Expected a single result for query: " + delegate);
        }
    }

    @Override
    public List<X> getResultList() {
        List<X> list = super.getResultList();
        
        for (int i = 0; i < list.size(); i++) {
            list.set(i, builder.build((Object[]) list.get(i)));
        }
        
        return builder.buildList(list);
    }
}
