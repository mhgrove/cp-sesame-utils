package com.clarkparsia.sesame.annotation;

import org.openrdf.model.Value;
import org.openrdf.model.URI;
import org.openrdf.model.Resource;

import java.util.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import java.lang.reflect.Field;

import com.clarkparsia.sesame.utils.GraphBuilder;
import com.clarkparsia.sesame.utils.ResourceBuilder;
import com.clarkparsia.sesame.utils.SesameValueFactory;
import com.clarkparsia.sesame.utils.ExtendedGraph;

import com.clarkparsia.utils.io.Encoder;

import com.clarkparsia.utils.BasicUtils;
import com.clarkparsia.utils.Function;

import com.clarkparsia.utils.collections.CollectionUtil;

/**
 * Title: <br/>
 * Description: <br/>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com> <br/>
 * Created: Nov 21, 2009 3:11:21 PM <br/>
 *
 * @author Michael Grove <mike@clarkparsia.com>
 */
public class RdfGenerator {

	public static ExtendedGraph asRdf(Object theObj) throws InvalidRdfException {
		if (theObj.getClass().getAnnotation(RdfClass.class) == null) {
			throw new InvalidRdfException("Specified value is not an RdfClass object");
		}

		RdfClass aClass = theObj.getClass().getAnnotation(RdfClass.class);

		GraphBuilder aBuilder = new GraphBuilder();
		ResourceBuilder aRes = null;

		Field aIdField = null;

		Collection<Field> aProps = new HashSet<Field>();

		for (Field aField : theObj.getClass().getDeclaredFields()) {
			if (aField.getAnnotation(RdfId.class) != null) {
				if (aIdField != null) {
					throw new InvalidRdfException("Cannot have multiple id properties");
				}
				else {
					aIdField = aField;
				}
			}

			if (aField.getAnnotation(RdfProperty.class) != null) {
				aProps.add(aField);
			}
		}

		try {
			if (aIdField == null || aIdField.getAnnotation(RdfId.class).namespace().equals("")) {
				aRes = aBuilder.instance(aBuilder.getSesameValueFactory().createURI(aClass.value()));
			}
			else {
				boolean aOldAccess = aIdField.isAccessible();
				aIdField.setAccessible(true);

				aRes = aBuilder.instance(aBuilder.getSesameValueFactory().createURI(aClass.value()),
										 aIdField.getAnnotation(RdfId.class).namespace() + hash(aIdField.get(theObj)));

				aIdField.setAccessible(aOldAccess);
			}

			AsValueFunction aFunc = new AsValueFunction();
			for (Field aProp : aProps) {
				RdfProperty aPropertyAnnotation = aProp.getAnnotation(RdfProperty.class);
				URI aProperty = aBuilder.getSesameValueFactory().createURI(aPropertyAnnotation.value());

				boolean aOldAccess = aProp.isAccessible();
				aProp.setAccessible(true);

				Object aValue = aProp.get(theObj);

				aProp.setAccessible(aOldAccess);

				if (Collection.class.isAssignableFrom(aValue.getClass())) {
					@SuppressWarnings("unchecked")
					List<Value> aValueList = asList((Collection<Object>) Collection.class.cast(aValue));

					if (aPropertyAnnotation.isList()) {
						aRes.addProperty(aProperty, aValueList);
					}
					else {
						for (Value aVal : aValueList) {
							aRes.addProperty(aProperty, aVal);
						}
					}
				}
				else {
					aRes.addProperty(aProperty, aFunc.apply(aValue));
				}
			}
		}
		catch (IllegalAccessException e) {
			throw new InvalidRdfException(e);
		}
		catch (RuntimeException e) {
			throw new InvalidRdfException(e.getMessage());
		}

		return aBuilder.graph();
	}

	private static Resource asResource(Object theObj) throws InvalidRdfException {
		Field aIdField = null;

		for (Field aField : theObj.getClass().getDeclaredFields()) {
			if (aField.getAnnotation(RdfId.class) != null) {
				if (aIdField != null) {
					throw new InvalidRdfException("Cannot have multiple id properties");
				}
				else {
					aIdField = aField;
				}
			}

		}

		if (aIdField == null) {
			throw new InvalidRdfException("No id field specified");
		}

		boolean aOldAccess = aIdField.isAccessible();
		aIdField.setAccessible(true);

		URI aURI = null;

		try {
			aURI = SesameValueFactory.instance().createURI(aIdField.getAnnotation(RdfId.class).namespace() +
														   hash(aIdField.get(theObj)));
		}
		catch (IllegalAccessException e) {
			// I dont think this can happen, set override any private-ness of the field
		}

		aIdField.setAccessible(aOldAccess);

		return aURI;
	}

	private static List<Value> asList(Collection<Object> theCollection) throws InvalidRdfException {
		try {
			return CollectionUtil.list(CollectionUtil.transform(theCollection, new AsValueFunction()));
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			throw new InvalidRdfException(e.getMessage());
		}
	}

	private static String hash(Object theObj) {
		return Encoder.base64Encode(BasicUtils.md5(theObj.toString()));
	}

	private static class AsValueFunction implements Function<Object, Value> {
		public Value apply(final Object theIn) {
			if (Boolean.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Boolean.class.cast(theIn));
			}
			else if (Integer.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Integer.class.cast(theIn));
			}
			else if (Long.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Long.class.cast(theIn));
			}
			else if (Short.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Short.class.cast(theIn));
			}
			else if (Double.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Double.class.cast(theIn));
			}
			else if (Float.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Float.class.cast(theIn));
			}
			else if (Date.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Date.class.cast(theIn));
			}
			else if (String.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(String.class.cast(theIn));
			}
			else if (Character.class.isInstance(theIn)) {
				return SesameValueFactory.instance().createTypedLiteral(Character.class.cast(theIn));
			}
			else if (Value.class.isAssignableFrom(theIn.getClass())) {
				return Value.class.cast(theIn);
			}
			else if (theIn.getClass().getAnnotation(RdfClass.class) != null) {
				try {
					return asResource(theIn);
				}
				catch (InvalidRdfException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				throw new RuntimeException("Unknown type conversion: " + theIn.getClass() + " " + theIn);
			}
		}
	}
}
