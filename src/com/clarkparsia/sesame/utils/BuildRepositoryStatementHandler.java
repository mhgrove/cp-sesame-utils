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

package com.clarkparsia.sesame.utils;

import org.openrdf.rio.StatementHandler;
import org.openrdf.sesame.sail.RdfRepository;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * <p>Implementation of the Sesame StatementHandler interface that will add statements to an RdfRepository object.</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class BuildRepositoryStatementHandler implements StatementHandler {
    private RdfRepository mRepository;

    public BuildRepositoryStatementHandler(RdfRepository theRepository) {
        mRepository = theRepository;
        //mRepository.startTransaction();
    }

    public void handleStatement(Resource theSubject, URI thePredicate, Value theObject) {
        try {
            mRepository.addStatement(theSubject, thePredicate, theObject);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}