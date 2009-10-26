package com.clarkparsia.sesame.utils;

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.vocabulary.XmlSchema;
import com.clarkparsia.utils.BasicUtils;

import java.util.Date;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Apr 29, 2009 3:10:06 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class SesameValueFactory extends ValueFactoryImpl {
	private static SesameValueFactory INSTANCE;
	public static SesameValueFactory instance() {
		if (INSTANCE == null) {
			INSTANCE = new SesameValueFactory();
		}

		return INSTANCE;
	}

	public URI createURI(java.net.URI theURI) {
		return super.createURI(theURI.toString());
	}

	public Literal createTypedLiteral(String theValue) {
		return super.createLiteral(theValue, createURI(XmlSchema.STRING));
	}

	public Literal createTypedLiteral(Date theDate) {
		return super.createLiteral(BasicUtils.datetime(theDate), createURI(XmlSchema.DATETIME));
	}

	public Literal createTypedLiteral(int theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.INT));
	}

	public Literal createTypedLiteral(boolean theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.BOOLEAN));
	}

	public Literal createTypedLiteral(char theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.STRING));
	}

	public Literal createTypedLiteral(long theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.LONG));
	}

	public Literal createTypedLiteral(double theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.DOUBLE));
	}

	public Literal createTypedLiteral(float theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.FLOAT));
	}

	public Literal createTypedLiteral(short theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.SHORT));
	}
}
