/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.mda.legacy.model;

import ilarkesto.base.Str;
import ilarkesto.persistence.AEntity;

import java.util.LinkedHashSet;
import java.util.Set;

public class DatobModel extends BeanModel {

	private Set<PropertyModel> properties = new LinkedHashSet<PropertyModel>();
	private boolean searchable;
	private boolean gwtSupport;

	public DatobModel(String name, String packageName) {
		super(name, packageName);
	}

	@Override
	public boolean isValueObject() {
		return true;
	}

	public boolean isGwtSupport() {
		return gwtSupport;
	}

	public void setGwtSupport(boolean gwtSupport) {
		this.gwtSupport = gwtSupport;
	}

	public void setSearchable(boolean searchable) {
		this.searchable = searchable;
	}

	public final boolean isSearchable() {
		if (searchable) return true;
		for (PropertyModel p : getProperties()) {
			if (p.isSearchable()) return true;
		}
		return false;
	}

	public final Set<PropertyModel> getProperties() {
		return properties;
	}

	public final Set<PropertyModel> getPropertiesAndSuperbeanProperties() {
		Set<PropertyModel> ret = new LinkedHashSet<PropertyModel>();
		ret.addAll(getProperties());
		BeanModel superbean = getSuperbean();
		if (superbean != null && superbean instanceof DatobModel) {
			ret.addAll(((DatobModel) superbean).getPropertiesAndSuperbeanProperties());
		}
		return ret;
	}

	public final Set<ReferencePropertyModel> getMasterReferences() {
		Set<ReferencePropertyModel> ret = new LinkedHashSet<ReferencePropertyModel>();
		for (PropertyModel property : getProperties()) {
			if (property instanceof ReferencePropertyModel) {
				if (((ReferencePropertyModel) property).isMaster()) ret.add((ReferencePropertyModel) property);
				continue;
			}
		}
		return ret;
	}

	public final Set<PropertyModel> getSearchableProperties() {
		Set<PropertyModel> ret = new LinkedHashSet<PropertyModel>();
		for (PropertyModel property : getProperties()) {
			if (property.isSearchable()) ret.add(property);
		}
		return ret;
	}

	public StringPropertyModel addStringProperty(String name) {
		StringPropertyModel propertyModel = new StringPropertyModel(this, name);
		properties.add(propertyModel);
		return propertyModel;
	}

	public IntegerPropertyModel addIntegerProperty(String name) {
		IntegerPropertyModel propertyModel = new IntegerPropertyModel(this, name);
		properties.add(propertyModel);
		return propertyModel;
	}

	public SimplePropertyModel addProperty(String name, Class type) {
		SimplePropertyModel propertyModel = new SimplePropertyModel(this, name, false, false, type.getName());
		if (type.equals(boolean.class) || type.equals(int.class) || type.equals(long.class) || type.equals(float.class)
				|| type.equals(double.class))
			propertyModel.setMandatory(true);
		properties.add(propertyModel);
		return propertyModel;
	}

	public ListPropertyModel addListProperty(String name, Class type) {
		ListPropertyModel propertyModel = new ListPropertyModel(this, name, false, type);
		properties.add(propertyModel);
		return propertyModel;
	}

	public SetPropertyModel addSetProperty(String name, Class type) {
		SetPropertyModel propertyModel = new SetPropertyModel(this, name, false, type);
		properties.add(propertyModel);
		return propertyModel;
	}

	public SetPropertyModel addSetProperty(String name, BeanModel type) {
		boolean valueObject = type.isValueObject();
		SetPropertyModel propertyModel = new SetPropertyModel(this, name, false, valueObject,
				type.getPackageName() + "." + type.getName());
		propertyModel.setSearchable(true);
		properties.add(propertyModel);
		return propertyModel;
	}

	public ReferencePropertyModel addReference(String name, EntityModel type) {
		String className = type.getPackageName() + "." + type.getName();
		ReferencePropertyModel propertyModel = new ReferencePropertyModel(this, name, type);
		propertyModel.setAbstract(type.isAbstract());
		properties.add(propertyModel);
		if (!AEntity.class.getName().equals(className) && !type.isAbstract() && !type.equals(this))
			addDependency(type.getPackageName() + "." + type.getName() + "Dao",
				Str.lowercaseFirstLetter((type.getName())) + "Dao");
		if (!isAbstract()) propertyModel.createBackReference(Str.lowercaseFirstLetter(getName()));
		return propertyModel;
	}

	public ReferenceSetPropertyModel addSetReference(String name, EntityModel type) {
		return addSetReference(name, type, false);
	}

	public ReferenceSetPropertyModel addSetReference(String name, EntityModel type, boolean createBackReference) {
		String className = type.getPackageName() + "." + type.getName();
		ReferenceSetPropertyModel propertyModel = new ReferenceSetPropertyModel(this, name, type);
		propertyModel.setAbstract(type.isAbstract());
		properties.add(propertyModel);
		if (!AEntity.class.getName().equals(className))
			addDependency(type.getPackageName() + "." + type.getName() + "Dao",
				Str.lowercaseFirstLetter((type.getName())) + "Dao");
		if (createBackReference) propertyModel.createBackReference(Str.lowercaseFirstLetter(getName()));
		return propertyModel;
	}

	public ReferenceListPropertyModel addListReference(String name, EntityModel type, boolean duplicatesAllowed) {
		return addListReference(name, type, true, duplicatesAllowed);
	}

	public ReferenceListPropertyModel addListReference(String name, EntityModel type, boolean createBackReference,
			boolean duplicatesAllowed) {
		String className = type.getPackageName() + "." + type.getName();
		ReferenceListPropertyModel propertyModel = new ReferenceListPropertyModel(this, name, type);
		propertyModel.setAbstract(type.isAbstract());
		properties.add(propertyModel);
		if (!AEntity.class.getName().equals(className))
			addDependency(type.getPackageName() + "." + type.getName() + "Dao",
				Str.lowercaseFirstLetter((type.getName())) + "Dao");
		if (createBackReference) propertyModel.createBackReference(Str.lowercaseFirstLetter(getName()));
		propertyModel.setDuplicatesAllowed(duplicatesAllowed);
		return propertyModel;
	}

}
