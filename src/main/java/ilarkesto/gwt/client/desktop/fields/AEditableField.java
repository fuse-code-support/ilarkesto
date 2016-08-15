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
package ilarkesto.gwt.client.desktop.fields;

import ilarkesto.core.base.Callback;
import ilarkesto.core.base.MultilineBuilder;
import ilarkesto.core.base.Str;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.ADataTransferObject;
import ilarkesto.gwt.client.Gwt;
import ilarkesto.gwt.client.Updatable;
import ilarkesto.gwt.client.desktop.ActionSelectionDialogBox;
import ilarkesto.gwt.client.desktop.DataForClientLoader;
import ilarkesto.gwt.client.desktop.DataForClientLoaderHelper;
import ilarkesto.gwt.client.desktop.Widgets;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AEditableField extends AField {

	private AEditableMultiFieldField parent;
	private Updatable updatable;
	private String editVetoMessage;
	private boolean clickHandlerAdded;
	private Callback callback;

	protected FieldEditorDialogBox fieldEditorDialogBox;
	private boolean editorAsync;

	public abstract boolean isValueSet();

	protected abstract IsWidget createEditorWidget();

	protected abstract void trySubmit() throws Exception;

	@Override
	protected boolean isLabelMandatoryMarkerActive() {
		return isMandatory() && !isValueSet();
	}

	protected final Widget createEditorWidgetForUse() {
		if (isEditorAsync()) {
			AsyncEditorWrapper asyncEditorWrapper = new AsyncEditorWrapper();
			onEditorRequested(asyncEditorWrapper);
			return asyncEditorWrapper;
		}
		return createEditorWidget().asWidget();
	}

	protected void onEditorRequested(Callback callback) {
		DataForClientLoader loader = getDataForClientLoader();
		if (loader != null) {
			this.callback = callback;
			DataForClientLoaderHelper.load(loader);
			return;
		}
		throw new IllegalStateException(getClass().getName() + ".onEditorRequested() not implemented");
	}

	protected DataForClientLoader getDataForClientLoader() {
		if (this instanceof DataForClientLoader) return (DataForClientLoader) this;
		return null;
	}

	public void dataReceivedOnClient(ADataTransferObject result) {
		callback.success(result);
	}

	protected boolean isEditorAsync() {
		if (getDataForClientLoader() == null) return false;
		return editorAsync;
	}

	public AEditableField setEditorAsync() {
		return setEditorAsync(true);
	}

	public AEditableField setEditorAsync(boolean editorAsync) {
		this.editorAsync = editorAsync;
		return this;
	}

	protected List<? extends IsWidget> getAdditionalDialogButtons(FieldEditorDialogBox dialogBox) {
		return null;
	}

	public final void submit() {
		Persistence.runInTransaction(Str.getSimpleName(getClass()) + ".submit()", new Runnable() {

			@Override
			public void run() {
				try {
					trySubmit();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				onSubmitted();
			}

		});
		onAfterSubmitted();
	}

	public final void submitOrParentSubmit() {
		AEditableMultiFieldField parent = getParent();
		if (parent == null) {
			getFieldEditorDialogBox().submit();
		} else {
			parent.getFieldEditorDialogBox().submit();
		}
	}

	/**
	 * Called after <code>trySubmit()</code> <strong>inside of</strong> transaction.
	 */
	protected void onSubmitted() {}

	/**
	 * Called after <code>trySubmit()</code> <strong>after</strong> transaction.
	 */
	protected void onAfterSubmitted() {}

	protected String getEditVetoMessage() {
		return editVetoMessage;
	}

	@Override
	protected void initFocusPanel(FocusPanel focusPanel) {
		super.initFocusPanel(focusPanel);
		String vetoMessage = getEditVetoMessage();
		if (vetoMessage == null || isSelfdocEnabled()) {
			if (!clickHandlerAdded) {
				focusPanel.addClickHandler(new ValueClickHandler());
				clickHandlerAdded = true;
			}
			focusPanel.addStyleName("clickable");
			focusPanel.setTitle(getTooltip());
		} else {
			focusPanel.setTitle(vetoMessage);
		}
	}

	@Override
	protected String getDisplayValueColor() {
		if (getEditVetoMessage() != null) return super.getDisplayValueColor();
		return "#444444";
	}

	protected List<AAction> getEditActions() {
		List<AAction> ret = new ArrayList<AAction>();
		if (getEditVetoMessage() == null) {
			ret.add(new EditAction());
		} else if (isSelfdocEnabled()) {
			String selfdocKey = getSelfdocKey();
			ret.add(Widgets.selfdocAction(selfdocKey, getLabel(), getTooltip()));
		}

		return ret;
	}

	public String getSelfdocKey() {
		return Str.getSimpleName(getClass());
	}

	private void onValueClicked(final ClickEvent event) {
		activate(event);
	}

	public final void activate(final ClickEvent event) {
		List<AAction> actions = getEditActions();
		log.debug(getClass().getSimpleName() + ".activate():", actions);
		if (actions.isEmpty()) return;
		if (actions.size() == 1) {
			AAction aAction = actions.get(0);
			if (aAction.getExecutionVeto() == null) {
				aAction.execute();
			}
			return;
		}
		// new ActionSelectionDialogBox(actions).showRelativeTo(getWidget());
		new ActionSelectionDialogBox(actions).setPopupPositionAndShow(event);
	}

	public void showEditor() {
		getFieldEditorDialogBox().show();
	}

	public void cancelEditor() {
		if (fieldEditorDialogBox == null) return;
		fieldEditorDialogBox.cancel();
	}

	public FieldEditorDialogBox getFieldEditorDialogBox() {
		if (fieldEditorDialogBox == null) fieldEditorDialogBox = new FieldEditorDialogBox(this);
		return fieldEditorDialogBox;
	}

	public boolean isMandatory() {
		return false;
	}

	public boolean isSubmittingEmptyMandatoryFieldAllowed() {
		return false;
	}

	public AEditableField setUpdatable(Updatable updateOnSubmit) {
		this.updatable = updateOnSubmit;
		return this;
	}

	public Updatable getUpdatable() {
		if (updatable == null) return this;
		return updatable;
	}

	protected IsWidget createEditorHeaderWidget() {
		return null;
	}

	public AEditableMultiFieldField getParent() {
		return parent;
	}

	public void setParent(AEditableMultiFieldField parent) {
		this.parent = parent;
	}

	public AEditableField setEditVetoMessage(String editVetoMessage) {
		this.editVetoMessage = editVetoMessage;
		return this;
	}

	public AEditableField setReadOnly() {
		return setReadOnly(true);
	}

	public AEditableField setReadOnly(boolean readonly) {
		return setEditVetoMessage(readonly ? "Nicht änderbar" : null);
	}

	public String getApplyButtonLabel() {
		return "Übernehmen";
	}

	public boolean isSelfdocEnabled() {
		return true;
	}

	protected boolean isIgnoreClicksOnImageElements() {
		return true;
	}

	private class ValueClickHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {
			event.stopPropagation();

			String targetString = Gwt.getTargetString(event);
			log.debug(AEditableField.this.getClass().getSimpleName() + ".onClick():", targetString);

			if (targetString != null) {
				if (isIgnoreClicksOnImageElements() && targetString.contains("[object HTMLImageElement]")) return;
				if (targetString.contains("goon-AnchorButton")) return;
			}

			onValueClicked(event);
		}
	}

	public class EditAction extends AAction {

		@Override
		public String getLabel() {
			return "Bearbeiten";
		}

		@Override
		protected void onExecute() {
			showEditor();
		}

	}

	class AsyncEditorWrapper extends SimplePanel implements Callback {

		public AsyncEditorWrapper() {
			add(Widgets.textSecondary("Lade..."));
		}

		@Override
		public void success(Object result) {
			setWidget(createEditorWidget());
			if (fieldEditorDialogBox != null) fieldEditorDialogBox.center();
		}

		@Override
		public void failure(String message, Exception ex) {
			log.warn("AsyncEditorWrapper callback failure:", message, ex);
			clear();
			add(Widgets.textError(new MultilineBuilder().ln(message, Str.formatException(ex, " -> "))));
			if (fieldEditorDialogBox != null) fieldEditorDialogBox.center();
		}

	}

	public boolean isEditorDialogAutohide() {
		return true;
	}

}
