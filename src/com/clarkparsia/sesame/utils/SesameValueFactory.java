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
		return super.createLiteral(BasicUtils.date(theDate), createURI(XmlSchema.DATE));
	}

	public Literal createDatetimeTypedLiteral(Date theDate) {
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
