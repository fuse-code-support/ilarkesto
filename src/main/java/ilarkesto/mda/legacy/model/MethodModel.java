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

import java.util.ArrayList;
import java.util.List;

public class MethodModel extends AModel {

	private List<ParameterModel> parameters = new ArrayList<ParameterModel>();
	private String packageName = "base";
	private boolean sync = false;
	private String returnType;
	private List<String> exceptions = new ArrayList<String>();
	private boolean dispensable;

	public MethodModel(String name) {
		super(name);
	}

	public MethodModel addException(Class<? extends Throwable> type) {
		return addException(type.getName());
	}

	public MethodModel addException(String type) {
		exceptions.add(type);
		return this;
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public MethodModel setReturnType(Class type) {
		setReturnType(type == null ? null : type.getName());
		return this;
	}

	public MethodModel setReturnType(String type) {
		this.returnType = type == null ? "void" : type;
		return this;
	}

	public String getReturnType() {
		return returnType;
	}

	public MethodModel addParameter(String name, String type) {
		ParameterModel parameter = new ParameterModel(name, type);
		parameters.add(parameter);
		return this;
	}

	public List<ParameterModel> getParameters() {
		return parameters;
	}

	public MethodModel setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	public String getPackageName() {
		return packageName;
	}

	public MethodModel setSync(boolean sync) {
		this.sync = sync;
		return this;
	}

	public boolean isSync() {
		return sync;
	}

	public boolean isDispensable() {
		return dispensable;
	}

	public MethodModel setDispensable(boolean dispensable) {
		this.dispensable = dispensable;
		return this;
	}

	// --- helper ---

	public MethodModel addParameter(String name) {
		return addParameter(name, String.class);
	}

	public MethodModel addParameter(String name, EntityModel type) {
		return addParameter(name, type.getPackageName() + "." + type.getName());
	}

	public MethodModel addParameter(String name, Class type) {
		return addParameter(name, type.getName());
	}

}
