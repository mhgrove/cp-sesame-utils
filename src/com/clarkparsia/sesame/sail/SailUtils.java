/*
 * Created on Sep 21, 2006
 */
package com.clarkparsia.sesame.sail;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.StatementImpl;

public class SailUtils {
    public static Statement createStatement( Resource subj, URI pred, Value obj ) {
        return new StatementImpl( subj, pred, obj );
    }
}
