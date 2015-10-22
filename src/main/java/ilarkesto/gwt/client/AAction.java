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
package ilarkesto.gwt.client;

import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.gwt.client.desktop.Widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;

public abstract class AAction implements Command, ClickHandler {

	protected final Log log = Log.get(getClass());
	private Updatable updatable = RootWidgetUpdatable.INSTANCE;

	private ClickEvent clickEvent;

	public abstract String getLabel();

	protected abstract void onExecute();

	@Override
	public final void execute() {
		String executionVeto = getExecutionVeto();
		if (executionVeto != null) throw new VetoException(executionVeto);
		if (!isPermitted()) throw new PermissionDeniedException();

		try {
			Persistence.runInTransaction(Str.getSimpleName(getClass()), new Runnable() {

				@Override
				public void run() {
					onExecute();
				}

			}, updatable != null && isUpdateAfterExecute() ? new Updater() : null);
		} catch (Exception ex) {
			try {
				handleException(ex);
			} catch (Exception ex1) {
				throw new RuntimeException(Str.getSimpleName(getClass()) + ".onExecute() failed", ex1);
			}
		}

	}

	protected boolean isUpdateAfterExecute() {
		return true;
	}

	protected void handleException(Exception ex) throws Exception {
		throw ex;
	}

	public String getTargetHistoryToken() {
		return null;
	}

	@Override
	public void onClick(ClickEvent event) {
		this.clickEvent = event;
		event.stopPropagation();
		try {
			execute();
		} catch (VetoException ex) {
			Widgets.showDialog("Action aborted", ex.getMessage());
		} catch (PermissionDeniedException ex) {
			Widgets.showDialog("Acton aborted", "Berechtigung fehlt");
		}
	}

	protected String getIconName() {
		return null;
	}

	public Image getIcon() {
		String iconName = getIconName();
		if (iconName == null) return null;
		return Widgets.icon("action_" + iconName);
	}

	/**
	 * @deprecated {@link #getExecutionVeto()}
	 */
	@Deprecated
	protected boolean isExecutable() {
		return true;
	}

	public String getExecutionVeto() {
		if (!isExecutable()) return "Not executable";
		return null;
	}

	public boolean isPermitted() {
		return true;
	}

	public String getTooltip() {
		return null;
	}

	public String getTooltipAsHtml() {
		return Str.toHtml(getTooltip());
	}

	public String getId() {
		return Str.getSimpleName(getClass()).replace('$', '_');
	}

	@Override
	public String toString() {
		return Str.getSimpleName(getClass());
	}

	public AAction setUpdatable(Updatable updatable) {
		this.updatable = updatable;
		return this;
	}

	protected Updatable getUpdatable() {
		return updatable;
	}

	protected boolean isUpdatableSet() {
		return updatable != null;
	}

	protected ClickEvent getClickEvent() {
		return clickEvent;
	}

	protected boolean isClickEventSet() {
		return clickEvent != null;
	}

	private class Updater implements Runnable {

		@Override
		public void run() {
			if (updatable == null) return;
			Timer t = new Timer() {

				@Override
				public void run() {
					updatable.update();
				}
			};
			t.schedule(500);

		}
	}

	class VetoException extends RuntimeException {

		public VetoException(String veto) {
			super(veto);
		}
	}

	class PermissionDeniedException extends RuntimeException {

	}

}
