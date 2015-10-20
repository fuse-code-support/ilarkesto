/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.mda.legacy.generator;

import ilarkesto.base.Threads;
import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.di.Context;
import ilarkesto.gwt.server.AGwtServiceImpl;
import ilarkesto.mda.legacy.model.GwtServiceModel;
import ilarkesto.mda.legacy.model.MethodModel;
import ilarkesto.mda.legacy.model.ParameterModel;

import java.util.LinkedHashSet;
import java.util.Set;

public class GwtServiceImplGenerator extends AClassGenerator {

	private GwtServiceModel service;

	public GwtServiceImplGenerator(GwtServiceModel service) {
		super();
		this.service = service;
	}

	@Override
	protected void writeContent() {
		for (MethodModel method : service.getMethods()) {
			writeMethodContent(method);
		}
	}

	private void writeMethodContent(MethodModel method) {
		ln();
		s("    protected abstract void on" + Str.uppercaseFirstLetter(method.getName())
				+ "(GwtConversation conversation");
		for (ParameterModel param : method.getParameters()) {
			s(",", param.getType(), param.getName());
		}
		ln(");");

		ln();
		annotationOverride();
		s("    public", service.getDtoClassName(), method.getName() + "(final int conversationNumber");
		for (ParameterModel param : method.getParameters()) {
			s(", final", param.getType(), param.getName());
		}
		ln(") {");
		boolean ping = method.getName().toLowerCase().equals("ping");
		if (!ping) {
			ln("        " + RuntimeTracker.class.getName(), "rt = new", RuntimeTracker.class.getName() + "();");
			ln("        log.debug(\"Handling service call: " + method.getName() + "\");");
		}
		ln("        WebSession session = (WebSession) getSession();");
		if (method.isSync()) {
			if (!ping) ln("        String threads = " + Threads.class.getName() + ".getAllThreadsInfo();");
			ln("        synchronized (getSyncObject()) {");
			if (!ping)
				ln("            if (rt.getRuntime() > 1000) log.warn(\"ServiceCall "
						+ method.getName()
						+ " waited for sync\", rt.getRuntimeFormated(), \">>> Threads before:\\n\", threads, \"Threads after:\\n\", "
						+ Threads.class.getName() + ".getAllThreadsInfo());");
		}
		ln("            final GwtConversation conversation;");
		ln("            try {");
		ln("                conversation = session.getGwtConversation(conversationNumber);");
		ln("            } catch (Throwable ex) {");
		ln("                log.info(\"Getting conversation failed:\", conversationNumber);");
		ln("                " + service.getDtoClassName() + " dto = new " + service.getDtoClassName() + "();");
		ln("                dto.addError(new ilarkesto.gwt.client.ErrorWrapper(ex));");
		ln("                return dto;");
		ln("            }");
		ln("            final", Context.class.getName(), "context = ilarkesto.di.Context.get();");
		ln("            context.setName(\"gwt-srv:" + method.getName() + "\");");
		ln("            context.bindCurrentThread();");
		ln("            try {");
		ln("                " + Persistence.class.getName() + ".runInTransaction(\"GwtService." + method.getName()
				+ "()\", new Runnable() { public void run() {");
		ln();
		s("                    on" + Str.uppercaseFirstLetter(method.getName()) + "(conversation");
		for (ParameterModel param : method.getParameters()) {
			s(",", param.getName());
		}
		ln(");");
		ln();
		ln("                }});");
		ln("                onServiceMethodExecuted(context);");
		ln("            } catch (Exception ex) {");
		ln("                handleServiceMethodException(conversationNumber, \"" + method.getName()
				+ "\", ex, context);");
		ln("            }");
		if (!ping) {
			String paramsString = "";
			for (ParameterModel param : method.getParameters()) {
				paramsString += ", " + param.getName();
			}
			if (!ping) {
				ln("            long runtime = rt.getRuntime();");
				ln("            logRuntime(conversation, \"" + method.getName() + "\" ,runtime);");
				ln("            if (runtime > getMaxServiceCallExecutionTime(\"" + method.getName() + "\")) {");
				ln("                log.warn(\"ServiceCall served in\", rt.getRuntimeFormated(),\"" + method.getName()
						+ "\"" + paramsString + ", \"Threads:\\n\", " + Threads.class.getName()
						+ ".getAllThreadsInfo());");
				ln("            } else {");
				ln("                log.info(\"ServiceCall served in\", rt.getRuntimeFormated(),\"" + method.getName()
						+ "\"" + paramsString + ");");
				ln("            }");
			}
		}
		ln("            return (" + service.getDtoClassName() + ") conversation.popNextData();");
		if (method.isSync()) {
			ln("        }");
		}
		ln("    }");
	}

	@Override
	protected String getSuperclass() {
		return AGwtServiceImpl.class.getName();
	}

	@Override
	protected Set<String> getSuperinterfaces() {
		Set<String> ret = new LinkedHashSet<String>();
		ret.addAll(super.getSuperinterfaces());
		ret.add(service.getClientPackageName() + "." + service.getName() + "Service");
		return ret;
	}

	@Override
	protected String getName() {
		return "G" + service.getName() + "ServiceImpl";
	}

	@Override
	protected String getPackage() {
		return service.getServerPackageName();
	}

	@Override
	protected boolean isInterface() {
		return false;
	}

	@Override
	protected boolean isAbstract() {
		return true;
	}

	@Override
	protected boolean isOverwrite() {
		return true;
	}

}
