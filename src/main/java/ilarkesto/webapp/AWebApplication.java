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
package ilarkesto.webapp;

import ilarkesto.base.Str;
import ilarkesto.base.Sys;
import ilarkesto.core.persistance.TransferableEntity;
import ilarkesto.di.app.AApplication;
import ilarkesto.gwt.server.AGwtConversation;
import ilarkesto.logging.DefaultLogRecordHandler;
import ilarkesto.webapp.jsonapi.JsonApiFactory;
import ilarkesto.webapp.jsonapi.ReflectionJsonApiFactory;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class AWebApplication extends AApplication {

	protected abstract void onStartWebApplication();

	protected abstract void onShutdownWebApplication();

	protected abstract AWebSession createWebSession(HttpServletRequest httpRequest);

	private Set<AWebSession> webSessions = new HashSet<AWebSession>();

	private String contextPath;

	private JsonApiFactory restApiFactory;
	private GwtSuperDevMode gwtSuperDevMode;

	@Override
	protected void onPreStart() {
		if (!isDevelopmentMode()) Sys.setHeadless(true);
		super.onPreStart();

		if (isDevelopmentMode() && !isUnitTestMode()) {
			gwtSuperDevMode = createGwtSuperDevMode();
			if (gwtSuperDevMode != null) gwtSuperDevMode.startCodeServerInSeparateProcessWithJarsFromIlarkesto();
		}
	}

	@Override
	protected void onStart() {
		DefaultLogRecordHandler.setLogFile(new File(getApplicationDataDir() + "/error.log"));
		log.info("Initializing web application");
		onStartWebApplication();
	}

	@Override
	protected void onShutdown() {
		if (gwtSuperDevMode != null) gwtSuperDevMode.stopCodeServer();
		onShutdownWebApplication();
	}

	// --- ---

	protected GwtSuperDevMode createGwtSuperDevMode() {
		return null;
	}

	public final AWebApplication getWebApplication() {
		return this;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String getApplicationName() {
		if (contextPath != null) return contextPath;
		String name = super.getApplicationName();
		name = Str.removeSuffix(name, "Web");
		return name;
	}

	public static final String WEB_SESSION_SESSION_ATTRIBUTE = "_webSession";

	public AWebSession getWebSession(HttpServletRequest httpRequest) {
		if (isShuttingDown()) return null;
		HttpSession httpSession = httpRequest.getSession();
		AWebSession webSession = (AWebSession) httpSession.getAttribute(WEB_SESSION_SESSION_ATTRIBUTE);
		if (webSession != null && webSession.isSessionInvalidated()) webSession = null;
		if (webSession == null) {
			webSession = createWebSession(httpRequest);
			httpSession.setAttribute(WEB_SESSION_SESSION_ATTRIBUTE, webSession);
			synchronized (webSessions) {
				webSessions.add(webSession);
			}
		} else {
			webSession.touch();
		}
		return webSession;
	}

	public final void destroyTimeoutedSessions() {
		for (AWebSession session : getWebSessions()) {
			if (session.isTimeouted() || session.isSessionInvalidated()) {
				log.info("Destroying invalid/timeouted session:", session);
				destroyWebSession(session, null);
			}
		}
	}

	public final void destroyTimeoutedGwtConversations() {
		for (AGwtConversation conversation : getGwtConversations()) {
			if (conversation.isTimeouted()) {
				AWebSession session = conversation.getSession();
				log.info("Destroying invalid/timeouted GwtConversation:", conversation);
				session.destroyGwtConversation(conversation);
			}
		}
	}

	public final void destroyWebSession(AWebSession webSession, HttpSession httpSession) {
		synchronized (webSessions) {
			webSessions.remove(webSession);
			webSession.destroy();
			if (httpSession != null) {
				try {
					httpSession.removeAttribute(WEB_SESSION_SESSION_ATTRIBUTE);
				} catch (Throwable t) {}
				try {
					httpSession.invalidate();
				} catch (Throwable t) {}
			}
		}
	}

	public final Set<AWebSession> getWebSessions() {
		synchronized (webSessions) {
			return new HashSet<AWebSession>(webSessions);
		}
	}

	public Set<AGwtConversation> getGwtConversations() {
		Set<AGwtConversation> ret = new HashSet<AGwtConversation>();
		for (AWebSession session : getWebSessions()) {
			ret.addAll(session.getGwtConversations());
		}
		return ret;
	}

	public JsonApiFactory getRestApiFactory() {
		if (restApiFactory == null) restApiFactory = createRestApiFactory();
		return restApiFactory;
	}

	protected ReflectionJsonApiFactory createRestApiFactory() {
		return new ReflectionJsonApiFactory(this);
	}

	@Override
	protected String getProductionModeApplicationDataDir() {
		return Sys.getUsersHomePath() + "/" + getApplicationName() + "-data";
	}

	public void sendToAll(TransferableEntity... entities) {
		for (AGwtConversation conversation : getGwtConversations()) {
			conversation.sendToClient(entities);
		}
	}

	public void sendToAll(Collection<TransferableEntity> entities) {
		for (AGwtConversation conversation : getGwtConversations()) {
			conversation.sendToClient(entities);
		}
	}

	public void sendToAllIfTracking(Collection<TransferableEntity> entities) {
		for (AGwtConversation conversation : getGwtConversations()) {
			conversation.sendToClientIfTracking(entities);
		}
	}

	public void deleteFromClients(Collection<String> entityIds) {
		if (entityIds == null || entityIds.isEmpty()) return;
		for (AGwtConversation conversation : getGwtConversations()) {
			for (String id : entityIds) {
				conversation.deleteFromClient(id);
			}
		}
	}

	public static AWebApplication get() {
		return (AWebApplication) AApplication.get();
	}

}
