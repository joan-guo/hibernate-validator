/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.descriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.internal.util.Contracts;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Describes a validated bean.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class BeanDescriptorImpl<T> extends ElementDescriptorImpl implements BeanDescriptor {
	private final Map<String, PropertyDescriptor> constrainedProperties;
	private final Map<String, MethodDescriptor> methods;
	private final Set<MethodDescriptor> constrainedMethods;

	public BeanDescriptorImpl(Class<T> beanClass,
							  Set<ConstraintDescriptorImpl<?>> classLevelConstraints,
							  Map<String, PropertyDescriptor> properties,
							  Map<String, MethodDescriptor> methods,
							  boolean defaultGroupSequenceRedefined,
							  List<Class<?>> defaultGroupSequence) {
		super( beanClass, classLevelConstraints, defaultGroupSequenceRedefined, defaultGroupSequence );

		this.constrainedProperties = Collections.unmodifiableMap( properties );
		this.methods = Collections.unmodifiableMap( methods );
		this.constrainedMethods = Collections.unmodifiableSet( getConstrainedMethods( methods.values() ) );
	}

	@Override
	public final boolean isBeanConstrained() {
		return hasConstraints() || !constrainedProperties.isEmpty() || !constrainedMethods.isEmpty();
	}

	@Override
	public final PropertyDescriptor getConstraintsForProperty(String propertyName) {
		assertNotNull( propertyName, "The property name cannot be null" );
		return constrainedProperties.get( propertyName );
	}

	@Override
	public final Set<PropertyDescriptor> getConstrainedProperties() {
		return new HashSet<PropertyDescriptor>( constrainedProperties.values() );
	}

	@Override
	public ConstructorDescriptor getConstraintsForConstructor(Class<?>... parameterTypes) {
		// TODO HV-571
		throw new IllegalArgumentException( "Not yet implemented" );
	}

	@Override
	public Set<ConstructorDescriptor> getConstrainedConstructors() {
		// TODO HV-571
		throw new IllegalArgumentException( "Not yet implemented" );
	}

	@Override
	public Set<MethodDescriptor> getConstrainedMethods() {
		return constrainedMethods;
	}

	@Override
	public MethodDescriptor getConstraintsForMethod(String methodName, Class<?>... parameterTypes) {
		Contracts.assertNotNull( methodName, MESSAGES.methodNameMustNotBeNull() );
		return methods.get( methodName + Arrays.toString( parameterTypes ) );
	}

	@Override
	public Kind getKind() {
		return Kind.BEAN;
	}

	private Set<MethodDescriptor> getConstrainedMethods(Collection<MethodDescriptor> methods) {
		Set<MethodDescriptor> constrainedMethodDescriptors = newHashSet();

		for ( MethodDescriptor oneMethod : methods ) {
			if ( oneMethod.hasConstraints() ) {
				constrainedMethodDescriptors.add( oneMethod );
			}

			if ( oneMethod.getReturnValueDescriptor().isCascaded() ) {
				constrainedMethodDescriptors.add( oneMethod );
			}

			for ( ParameterDescriptor oneParameter : oneMethod.getParameterDescriptors() ) {
				if ( oneParameter.hasConstraints() || oneParameter.isCascaded() ) {
					constrainedMethodDescriptors.add( oneMethod );
				}
			}
		}

		return constrainedMethodDescriptors;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "BeanDescriptorImpl" );
		sb.append( "{class='" );
		sb.append( getElementClass().getSimpleName() );
		sb.append( "'}" );
		return sb.toString();
	}
}