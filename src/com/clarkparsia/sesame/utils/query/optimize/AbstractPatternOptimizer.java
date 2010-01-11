/*
 * Copyright (c) 2005-2010 Clark & Parsia, LLC. <http://www.clarkparsia.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.clarkparsia.sesame.utils.query.optimize;

import org.openrdf.sesame.sail.query.GraphPattern;
import org.openrdf.sesame.sail.query.TriplePattern;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.vocabulary.RDFS;
import org.openrdf.vocabulary.OWL;
import org.openrdf.vocabulary.RDF;

/**
 * <p>Abstract implementation of the GraphPatternOptimizer interface</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public abstract class AbstractPatternOptimizer implements GraphPatternOptimizer {
    private GraphPatternOptimizer mOptimizer;

    public AbstractPatternOptimizer() {
        this(null);
    }

    public AbstractPatternOptimizer(GraphPatternOptimizer theOpt) {
        mOptimizer = theOpt;
    }

    public GraphPattern optimize(GraphPattern thePattern) throws Exception {
        GraphPattern aPattern = thePattern;

        if (mOptimizer != null) {
            aPattern = mOptimizer.optimize(aPattern);
        }

        return performOptimization(aPattern);
    }

    protected abstract GraphPattern performOptimization(GraphPattern thePattern) throws Exception;

    protected boolean isSchemaTriple(TriplePattern thePattern) {
        return thePattern.getPredicateVar().getValue() != null &&
               (thePattern.getPredicateVar().getValue().toString().startsWith(RDF.NAMESPACE) ||
                thePattern.getPredicateVar().getValue().toString().startsWith(RDFS.NAMESPACE) ||
                thePattern.getPredicateVar().getValue().toString().startsWith(OWL.NAMESPACE));
    }


    protected boolean isTypeTriple(TriplePattern thePattern) {
        return thePattern.getPredicateVar().getValue() != null &&
               thePattern.getPredicateVar().getValue().equals(URIImpl.RDF_TYPE);
    }
}
