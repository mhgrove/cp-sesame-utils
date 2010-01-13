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

import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.vocabulary.XmlSchema;
import com.clarkparsia.utils.BasicUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * <p>Extends the normal Sesame ValueFactory with some extra convenience functions for creating typed literals</p>
 *
 * @author Michael Grove
 * @since 1.0
 */
public class SesameValueFactory extends ValueFactoryImpl {

	/**
	 * A singleton instance of this factory
	 */
	private static SesameValueFactory INSTANCE;

	/**
	 * Return a singleton instance of this factory
	 * @return the global instance
	 */
	public static SesameValueFactory instance() {
		if (INSTANCE == null) {
			INSTANCE = new SesameValueFactory();
		}

		return INSTANCE;
	}

	/**
	 * Create a Sesame URI object from a Java {@link URI} object.
	 * @param theURI the java URI
	 * @return the Java URI as a Sesame URI
	 */
	public URI createURI(java.net.URI theURI) {
		return super.createURI(theURI.toString());
	}

	/**
	 * Create a xsd:string typed Literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(String theValue) {
		return super.createLiteral(theValue, createURI(XmlSchema.STRING));
	}

	/**
	 * Create an xsd:date typed literal
	 * @param theDate the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(Date theDate) {
		return super.createLiteral(BasicUtils.date(theDate), createURI(XmlSchema.DATE));
	}

	/**
	 * Create an xsd:datetime typed literal
	 * @param theDate the value of the literal
	 * @return the typed literal
	 */
	public Literal createDatetimeTypedLiteral(Date theDate) {
		return super.createLiteral(BasicUtils.datetime(theDate), createURI(XmlSchema.DATETIME));
	}

	/**
	 * Create an xsd:int typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(int theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.INT));
	}

	/**
	 * Create an xsd:boolean typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(boolean theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.BOOLEAN));
	}

	/**
	 * Create an xsd:string typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(char theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.STRING));
	}

	/**
	 * Create an xsd:long typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(long theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.LONG));
	}

	/**
	 * Create a xsd:double typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(double theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.DOUBLE));
	}

	/**
	 * Create an xsd:float typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(float theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.FLOAT));
	}

	/**
	 * Create a xsd:short typed literal
	 * @param theValue the value of the literal
	 * @return the typed literal
	 */
	public Literal createTypedLiteral(short theValue) {
		return super.createLiteral(String.valueOf(theValue), createURI(XmlSchema.SHORT));
	}
}
