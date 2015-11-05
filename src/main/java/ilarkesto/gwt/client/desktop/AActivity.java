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
package ilarkesto.gwt.client.desktop;

import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.gwt.client.AGwtApplication;
import ilarkesto.gwt.client.Updatable;
import ilarkesto.gwt.client.desktop.ActivityRuntimeStatistics.ActivityInfo;

public abstract class AActivity implements Updatable {

	protected final Log log = Log.get(getClass());

	private static AActivity currentActivity;

	protected ActivityParameters parameters = new ActivityParameters();
	private AActivity previousActivity;
	private Workspace workspace;

	protected ActivityInfo statsInfo;

	public AActivity() {}

	public final void startAsChild() {
		start(currentActivity);
	}

	public final void startAsRoot(ActivityParameters parameters) {
		this.parameters = parameters;
		start(null);
	}

	public ActivityParameters createParametersForServer() {
		return getParameters();
	}

	private final void start(AActivity returnToActivity) {
		previousActivity = returnToActivity;
		currentActivity = this;
		workspace = Widgets.workspace().setTitle("Lade...", null).setContent(Widgets.waitinfo());
		Desktop.showWorkspace(workspace);
		workspace.addToolbarAction(Widgets.selfdocAction(getSelfdocKey(), null, null));

		RuntimeTracker rt = new RuntimeTracker();
		try {
			onStart();
		} catch (Exception ex) {
			log.error(ex);
			throw new RuntimeException(getSelfdocKey() + ".onStart() failed", ex);
		}
		statsInfo = AGwtApplication.get().stats.addActivity(getSelfdocKey(), rt.getRuntime());

		onAfterStart();
	}

	protected void onAfterStart() {}

	public String getSelfdocKey() {
		return Str.getSimpleName(getClass());
	}

	protected void onStart() {};

	final void resume() {
		Desktop.showWorkspace(workspace);
		RuntimeTracker rt = new RuntimeTracker();
		onResume();
		statsInfo.setOnResumeRuntime(rt.getRuntime());
	}

	protected void onResume() {}

	public final void finish() {
		onFinish();
		workspace = null;
		if (isThisActivityCurrentlyActive()) {
			currentActivity = previousActivity;
			if (currentActivity != null) {
				currentActivity.resume();
			}
		}
	}

	protected void onUpdate() {}

	private boolean firstUpdate = true;

	@Override
	public final AActivity update() {
		if (firstUpdate) {
			try {
				onFirstUpdate();
			} catch (Exception ex) {
				throw new RuntimeException(getSelfdocKey() + ".onFirstUpdate() failed", ex);
			}
			firstUpdate = false;
		}
		try {
			onUpdate();
		} catch (Exception ex) {
			throw new RuntimeException(getSelfdocKey() + ".onUpdate() failed", ex);
		}
		return this;
	}

	protected void onFirstUpdate() {}

	protected void onDataReceivedOnClient() {
		update();
	}

	public final boolean isThisActivityCurrentlyActive() {
		return currentActivity == this;
	}

	protected void onFinish() {}

	public final Workspace getWorkspace() {
		if (workspace == null) throw new IllegalStateException("Activity already finished");
		return workspace;
	}

	@Override
	public String toString() {
		return Utl.getSimpleName(getClass());
	}

	public static AActivity getCurrent() {
		return currentActivity;
	}

	public final ActivityParameters getParameters() {
		return parameters;
	}

	public boolean executeCommand(String command) {
		return false;
	}

	public final AActivity setParameters(ActivityParameters parameters) {
		this.parameters = parameters;
		return this;
	}

}
