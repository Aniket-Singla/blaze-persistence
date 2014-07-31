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

import com.blazebit.persistence.RestrictionBuilder;
import com.blazebit.persistence.SubqueryBuilder;
import com.blazebit.persistence.SubqueryInitiator;
import com.blazebit.persistence.impl.expression.Expression;
import com.blazebit.persistence.impl.expression.ExpressionFactory;
import com.blazebit.persistence.impl.expression.SubqueryExpression;
import com.blazebit.persistence.impl.predicate.AndPredicate;
import com.blazebit.persistence.impl.predicate.BetweenPredicate;
import com.blazebit.persistence.impl.predicate.EqPredicate;
import com.blazebit.persistence.impl.predicate.ExistsPredicate;
import com.blazebit.persistence.impl.predicate.GePredicate;
import com.blazebit.persistence.impl.predicate.GtPredicate;
import com.blazebit.persistence.impl.predicate.LePredicate;
import com.blazebit.persistence.impl.predicate.LikePredicate;
import com.blazebit.persistence.impl.predicate.LtPredicate;
import com.blazebit.persistence.impl.predicate.NotPredicate;
import com.blazebit.persistence.impl.predicate.Predicate;
import com.blazebit.persistence.impl.predicate.PredicateBuilder;
import com.blazebit.persistence.impl.predicate.UnaryExpressionPredicate;

/**
 *
 * @author ccbem
 */
public abstract class PredicateManager<U> extends AbstractManager {
    protected final SubqueryInitiatorFactory subqueryInitFactory;
    final RootPredicate rootPredicate;
    private RightHandsideSubqueryPredicateBuilder rightSubqueryPredicateBuilderListener;
    private final LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>> leftSubqueryPredicateBuilderListener = new LeftHandsideSubqueryPredicateBuilder<RestrictionBuilder<?>>();
    protected final ExpressionFactory expressionFactory;

    PredicateManager(QueryGenerator queryGenerator, ParameterManager parameterManager, SubqueryInitiatorFactory subqueryInitFactory, ExpressionFactory expressionFactory) {
        super(queryGenerator, parameterManager);
        this.rootPredicate = new RootPredicate();
        this.subqueryInitFactory = subqueryInitFactory;
        this.expressionFactory = expressionFactory;
    }

    RootPredicate getRootPredicate() {
        return rootPredicate;
    }

    RestrictionBuilder<U> restrict(AbstractBaseQueryBuilder<?, ?> builder, Expression expr) {
        return rootPredicate.startBuilder(new RestrictionBuilderImpl<U>((U) builder, rootPredicate, expr, subqueryInitFactory, expressionFactory));
    }
    
    SubqueryInitiator<RestrictionBuilder<U>> restrict(AbstractBaseQueryBuilder<?, ?> builder) {
        RestrictionBuilder<U> restrictionBuilder = (RestrictionBuilder<U>) rootPredicate.startBuilder( new RestrictionBuilderImpl<U>((U) builder, rootPredicate, subqueryInitFactory, expressionFactory));
        return subqueryInitFactory.createSubqueryInitiator(restrictionBuilder, leftSubqueryPredicateBuilderListener);
    }

    SubqueryInitiator<U> restrictExists(U result) {
        rightSubqueryPredicateBuilderListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder(rootPredicate, new ExistsPredicate()));
        return subqueryInitFactory.createSubqueryInitiator(result, rightSubqueryPredicateBuilderListener);
    }
    
    SubqueryInitiator<U> restrictNotExists(U result) {
        RightHandsideSubqueryPredicateBuilder subqueryListener = rootPredicate.startBuilder(new RightHandsideSubqueryPredicateBuilder(rootPredicate, new NotPredicate(new ExistsPredicate())));
        return subqueryInitFactory.createSubqueryInitiator(result, subqueryListener);
    }

    void applyTransformer(ArrayExpressionTransformer transformer) {
        // carry out transformations
        rootPredicate.predicate.accept(new ArrayTransformationVisitor(transformer));
    }

    void verifyBuilderEnded() {
        rootPredicate.verifyBuilderEnded();
        leftSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        if(rightSubqueryPredicateBuilderListener != null){
            rightSubqueryPredicateBuilderListener.verifySubqueryBuilderEnded();
        }
    }

    void acceptVisitor(Predicate.Visitor v) {
        rootPredicate.predicate.accept(v);
    }

    void buildClause(StringBuilder sb) {
        queryGenerator.setQueryBuffer(sb);
        applyPredicate(queryGenerator, sb);
    }

    protected abstract String getClauseName();

    void applyPredicate(QueryGenerator queryGenerator, StringBuilder sb) {
        if (rootPredicate.predicate.getChildren().isEmpty()) {
            return;
        }
        sb.append(' ').append(getClauseName()).append(' ');
        rootPredicate.predicate.accept(queryGenerator);
    }

    class RootPredicate extends PredicateBuilderEndedListenerImpl {

        final AndPredicate predicate;

        public RootPredicate() {
            this.predicate = new AndPredicate();
        }

        @Override
        public void onBuilderEnded(PredicateBuilder builder) {
            super.onBuilderEnded(builder);
            Predicate pred = builder.getPredicate();

            // register parameter expressions
            registerParameterExpressions(pred);
            
            predicate.getChildren()
                    .add(pred);
        }
    }

    private static class ArrayTransformationVisitor extends VisitorAdapter {

        private final ArrayExpressionTransformer transformer;

        public ArrayTransformationVisitor(ArrayExpressionTransformer transformer) {
            this.transformer = transformer;
        }

        @Override
        public void visit(BetweenPredicate predicate) {
            predicate.setStart(transformer.transform(predicate.getStart()));
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setEnd(transformer.transform(predicate.getEnd()));
        }

        @Override
        public void visit(GePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }

        @Override
        public void visit(GtPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }
        
        @Override
        public void visit(LikePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }

        @Override
        public void visit(EqPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }

        @Override
        public void visit(LePredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }

        @Override
        public void visit(LtPredicate predicate) {
            predicate.setLeft(transformer.transform(predicate.getLeft()));
            predicate.setRight(transformer.transform(predicate.getRight()));
        }
        
    }
}
