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
package ilarkesto.gwt.server;

import ilarkesto.base.PermissionDeniedException;
import ilarkesto.base.Sys;
import ilarkesto.base.Utl;
import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.Entity;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.core.persistance.TransferBus;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.TimePeriod;
import ilarkesto.gwt.client.ADataTransferObject;
import ilarkesto.gwt.client.ClientDataTransporter;
import ilarkesto.gwt.client.Transportable;
import ilarkesto.webapp.AWebSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AGwtConversation<S extends AWebSession> implements ClientDataTransporter,
		Comparable<AGwtConversation> {

	private static final Log log = Log.get(AGwtConversation.class);
	private static final TimePeriod DEFAULT_TIMEOUT = TimePeriod.minutes(2);

	/**
	 * Data that will be transferred to the client at the next request.
	 */
	private ADataTransferObject nextData;
	private Object nextDataLock = new Object();
	private Map<Entity, Long> remoteEntityModificationTimes = Collections.synchronizedMap(new HashMap<Entity, Long>());

	private S session;
	private int number;
	private DateAndTime lastTouched;

	protected abstract ADataTransferObject createDataTransferObject();

	public AGwtConversation(S session, int number) {
		super();
		this.session = session;
		this.number = number;

		nextData = createDataTransferObject();
		if (nextData != null) {
			nextData.developmentMode = Sys.isDevelopmentMode();
			nextData.entityIdBase = UUID.randomUUID().toString();
			nextData.conversationNumber = number;
		}

		touch();
	}

	@Override
	public void sendToClient(Transportable transportable) {
		throw new RuntimeException("Unsupported Transportable: " + transportable.getClass().getName());
	}

	public int getNumber() {
		return number;
	}

	public final void clearRemoteEntities() {
		remoteEntityModificationTimes.clear();
	}

	public final void clearRemoteEntity(Entity entity) {
		remoteEntityModificationTimes.remove(entity);
	}

	public final void clearRemoteEntitiesByType(Class<? extends Entity> type) {
		List<Entity> toRemove = new ArrayList<Entity>();
		for (Entity entity : remoteEntityModificationTimes.keySet()) {
			if (entity.getClass().equals(type)) toRemove.add(entity);
		}
		for (Entity entity : toRemove) {
			remoteEntityModificationTimes.remove(entity);
		}
	}

	protected boolean isEntityVisible(Entity entity) {
		return true;
	}

	protected void filterEntityProperties(Entity entity, Map propertiesMap) {}

	public boolean isAvailableOnClient(Entity entity) {
		return remoteEntityModificationTimes.containsKey(entity);
	}

	@Override
	public void sendToClient(Entity entity) {
		if (entity == null) return;
		RuntimeTracker rt = new RuntimeTracker();
		sendToClientInternal(entity);
		if (rt.getRuntime() > 1000) {
			log.warn("sendToClient(Entity) took", rt.getRuntimeFormated(), "->", entity.getClass().getSimpleName(),
				entity.getId(), entity.toString());
		}
	}

	@Override
	public final void sendToClient(Collection<? extends Entity> entities) {
		if (entities == null) return;
		transferBusWarningPosted = false;
		RuntimeTracker rt = new RuntimeTracker();
		for (Entity entity : entities) {
			sendToClientInternal(entity);
		}
		if (rt.getRuntime() > 3000) {
			log.warn("sendToClient(Collection) took", rt.getRuntimeFormated(), "->", entities.size(), entities);
		}
	}

	boolean transferBusWarningPosted;

	private void sendToClientInternal(Entity entity) {
		if (entity == null) return;

		if (!Persistence.transactionManager.getCurrentTransaction().containsWithId(entity.getId())) {
			getNextData().addDeletedEntity(entity.getId());
			return;
		}

		if (!isEntityVisible(entity))
			throw new PermissionDeniedException(entity + " is not visible in " + getSession());

		TransferBus transferBus = new TransferBus();
		RuntimeTracker rt = new RuntimeTracker();
		loadTransferBus(entity, transferBus);
		if (!transferBusWarningPosted && rt.getRuntime() > 3000) {
			log.warn("Collecting passengers took", rt.getRuntimeFormated(), "->", toString(entity), "->",
				transferBus.getEntities());
			transferBusWarningPosted = true;
		}

		for (Entity e : transferBus.getEntities()) {
			addToNextData(e);
		}
	}

	protected void loadTransferBus(Entity entity, TransferBus transferBus) {
		transferBus.add(entity);
	}

	public final void sendToClientIfTracking(Collection<? extends Entity> entities) {
		if (entities == null) return;
		for (Entity entity : entities) {
			sendToClientIfTracking(entity);
		}
	}

	@Override
	public final void sendToClient(Entity... entities) {
		sendToClient(Arrays.asList(entities));
	}

	public void deleteFromClient(String entityId) {
		if (entityId == null) return;
		getNextData().addDeletedEntity(entityId);
		for (Entity entity : remoteEntityModificationTimes.keySet()) {
			if (entity.getId().equals(entityId)) {
				remoteEntityModificationTimes.remove(entity);
				break;
			}
		}
	}

	public void sendToClientIfTracking(Entity entity) {
		if (entity == null) return;
		if (!isAvailableOnClient(entity)) return;
		sendToClient(entity);
	}

	private void addToNextData(Entity entity) {
		Long timeRemote = remoteEntityModificationTimes.get(entity);
		Long timeLocal = entity.getModificationTime();

		ADataTransferObject nd = getNextData();
		if (nd.containsDeletedEntity(entity.getId())) return;

		if (timeLocal.equals(timeRemote)) {
			if (log.isDebugEnabled())
				log.debug("Remote entity already up to date:", toString(entity), "for", this, "->", timeLocal, "/",
					timeRemote);
			return;
		}

		Map<String, String> propertiesMap = entity.createPropertiesMap();
		filterEntityProperties(entity, propertiesMap);

		nd.addEntity(propertiesMap);
		remoteEntityModificationTimes.put(entity, timeLocal);
		if (log.isDebugEnabled()) log.debug("Sending", toString(entity), "to", this);
	}

	private String toString(Entity entity) {
		if (entity == null) return "<null>";
		return Str.getSimpleName(entity.getClass()) + " " + entity.getId() + " " + entity.toString();
	}

	public final ADataTransferObject popNextData() {
		if (nextData == null) return null;
		synchronized (nextDataLock) {
			ADataTransferObject ret = nextData;
			nextData = createDataTransferObject();
			return ret;
		}
	}

	public ADataTransferObject getNextData() {
		return nextData;
	}

	@Override
	public ADataTransferObject getDataTransferObject() {
		return getNextData();
	}

	public S getSession() {
		return session;
	}

	public final void touch() {
		lastTouched = DateAndTime.now();
	}

	protected TimePeriod getTimeout() {
		return DEFAULT_TIMEOUT;
	}

	public final boolean isTimeouted() {
		return lastTouched.getPeriodToNow().isGreaterThen(getTimeout());
	}

	public final DateAndTime getLastTouched() {
		return lastTouched;
	}

	public void invalidate() {}

	@Override
	public String toString() {
		return "#" + number + "@" + getSession();
	}

	@Override
	public int compareTo(AGwtConversation o) {
		return Utl.compare(o.getLastTouched(), getLastTouched());
	}
}
