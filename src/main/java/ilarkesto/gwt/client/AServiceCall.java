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
package ilarkesto.gwt.client;

import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.base.Str;
import ilarkesto.core.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.rpc.StatusCodeException;

public abstract class AServiceCall<D extends ADataTransferObject> {

	public static final long MAX_FAILURE_TIME = 30 * Tm.SECOND;

	private static LinkedList<AServiceCall> queue = new LinkedList<AServiceCall>();
	private static AServiceCall currentServiceCall;
	private static long lastSuccessfullServiceCallTime;
	public static Runnable listener;
	private static LinkedList<Runnable> runnablesAfterAllFinished = new LinkedList<Runnable>();

	protected final Log log = Log.get(getClass());

	private Runnable returnHandler;
	private RuntimeTracker rtCall;
	private long runtimeLastServicecall = -1;
	private long runtimeClientHandler = -1;
	private String contextInfo;

	protected abstract void onExecute(int conversationNumber, AsyncCallback<D> callback);

	protected void initializeService(Object service, String contextName) {
		ServiceDefTarget serviceDefTarget = (ServiceDefTarget) service;
		serviceDefTarget.setServiceEntryPoint(GWT.getModuleBaseURL() + contextName);
	}

	public final void execute() {
		if (AGwtApplication.get().isAborted()) {
			log.info("GWT application aborted, service call execution prevented:", this);
			return;
		}
		execute(null);
	}

	@Deprecated
	public final void execute(Runnable returnHandler) {
		if (queue.contains(this)) throw new IllegalStateException(getName() + " already executed");
		this.returnHandler = returnHandler;
		queue();
		runNext();
	}

	public final void execute(AServiceCallResultHandler resultHandler) {
		if (queue.contains(this)) throw new IllegalStateException(getName() + " already executed");
		this.returnHandler = resultHandler;
		queue();
		runNext();
	}

	private static void runNext() {
		if (currentServiceCall != null) return;
		if (queue.isEmpty()) {
			for (Runnable runnable : runnablesAfterAllFinished) {
				runnable.run();
			}
			runnablesAfterAllFinished.clear();
			return;
		}
		AServiceCall next = queue.getFirst();
		queue.remove(next);
		next.run();
	}

	public static void runAfterAllFinished(Runnable runnable) {
		runnablesAfterAllFinished.add(runnable);
	}

	public static void runAfterAllFinished(final Updatable updatable) {
		if (updatable == null) return;
		runAfterAllFinished(new Runnable() {

			@Override
			public void run() {
				updatable.update();
			}
		});
	}

	private void queue() {
		queue.add(this);
	}

	private void run() {
		if (currentServiceCall != null)
			throw new IllegalStateException("Another ServiceCall already running: " + currentServiceCall);
		currentServiceCall = this;
		rtCall = new RuntimeTracker();
		onExecute(AGwtApplication.get().getConversationNumber(), new ServiceCallback());
	}

	public static final boolean containsServiceCall(Class<? extends AServiceCall> type) {
		String name = Str.getSimpleName(type);
		for (AServiceCall call : queue) {
			String callName = Str.getSimpleName(call.getClass());
			if (callName.equals(name)) return true;
		}
		return false;
	}

	public boolean isDispensable() {
		return false;
	}

	public final long getRuntime() {
		return rtCall.getRuntime();
	}

	public final long getRuntimeLastServicecall() {
		return runtimeLastServicecall;
	}

	public final long getRuntimeClientHandler() {
		return runtimeClientHandler;
	}

	public final String getName() {
		return Str.removeSuffix(Str.getSimpleName(getClass()), "ServiceCall");
	}

	public static List<AServiceCall> getActiveServiceCalls() {
		ArrayList<AServiceCall> ret = new ArrayList<AServiceCall>();
		if (currentServiceCall != null) ret.add(currentServiceCall);
		ret.addAll(queue);
		return ret;
	}

	private void serviceCallReturned() {
		if (currentServiceCall != this) throw new IllegalStateException("currentServiceCall != this");
		currentServiceCall = null;
		rtCall.stop();
		if (!getName().equals("Ping")) log.info("serviceCallReturned()");

		if (AGwtApplication.get().isAborted()) return;

		if (listener != null) listener.run();
		runNext();
	}

	protected void onCallbackError(List<ErrorWrapper> errors) {}

	private void callbackError(List<ErrorWrapper> errors) {
		log.error("callbackError()", contextInfo, runnablesAfterAllFinished, Utl.getClassName(listener),
			Utl.getClassName(returnHandler), errors);
		onCallbackError(errors);
		long timeFromLastSuccess = Tm.getCurrentTimeMillis() - lastSuccessfullServiceCallTime;

		if (returnHandler instanceof AServiceCallResultHandler) {
			((AServiceCallResultHandler) returnHandler).onError(errors);
		}

		if (isDispensable() && timeFromLastSuccess < AServiceCall.MAX_FAILURE_TIME) {
			log.warn("Dispensable service call failed:", getName(), contextInfo, errors);
		} else {
			AGwtApplication.get().handleServiceCallError(getName(), contextInfo, errors);
		}
	}

	private void callbackSuccess(D data) {
		lastSuccessfullServiceCallTime = Tm.getCurrentTimeMillis();
		RuntimeTracker rtData = new RuntimeTracker();
		AGwtApplication.get().serverDataReceived(data);
		runtimeLastServicecall = rtData.getRuntime();

		if (returnHandler != null) {
			RuntimeTracker rtHandler = new RuntimeTracker();
			if (returnHandler instanceof AServiceCallResultHandler) {
				((AServiceCallResultHandler) returnHandler).setDto(data);
			}
			returnHandler.run();
			runtimeClientHandler = rtHandler.getRuntime();
		}
		AGwtApplication.get().onServiceCallSuccessfullyProcessed(this);
	}

	public AServiceCall<D> setContextInfo(String contextInfo) {
		this.contextInfo = contextInfo;
		return this;
	}

	protected class ServiceCallback implements AsyncCallback<D> {

		@Override
		public void onFailure(Throwable ex) {
			log.info("onFailure()", ex);
			serviceCallReturned();
			if (ex instanceof StatusCodeException) {
				StatusCodeException sce = (StatusCodeException) ex;
				if (sce.getStatusCode() == 0 || getName().toLowerCase().equals("ping")
						|| sce.getMessage().contains("503 Service Unavailable")) {
					callbackError(Utl.toList(ErrorWrapper.createServerNotAvailable()));
					return;
				}
			}
			callbackError(Utl.toList(new ErrorWrapper(ex)));
		}

		@Override
		public void onSuccess(D data) {
			serviceCallReturned();

			List<ErrorWrapper> errors = data.getErrors();
			if (errors != null && !errors.isEmpty()) {
				callbackError(errors);
				return;
			}

			callbackSuccess(data);
		}

	}

}
