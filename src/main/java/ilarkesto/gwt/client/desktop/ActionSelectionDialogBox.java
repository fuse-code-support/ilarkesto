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

import ilarkesto.gwt.client.AAction;
import ilarkesto.gwt.client.Gwt;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.DialogBox;

public class ActionSelectionDialogBox {

	private DialogBox dialogBox;
	private List<AAction> actions;

	public ActionSelectionDialogBox(AAction... actions) {
		this(Arrays.asList(actions));
	}

	public ActionSelectionDialogBox(List<AAction> actions) {
		super();
		this.actions = actions;
	}

	public void setPopupPositionAndShow(final ClickEvent event) {
		BuilderPanel bp = new BuilderPanel();
		for (AAction action : actions) {
			if (action.getExecutionVeto() != null) continue;
			if (!action.isPermitted()) continue;
			ActionButton button = new ActionButton(new WrapperAction(action));
			button.getElement().getStyle().setWidth(100, Unit.PCT);
			bp.add(button);
		}

		dialogBox = Widgets.dialog(true, "Aktion wählen", Widgets.frame(bp), null, 300);
		Gwt.setPopupPositionAndShow(dialogBox, event);
	}

	private class WrapperAction extends AAction {

		private AAction payloadAction;

		public WrapperAction(AAction payloadAction) {
			super();
			this.payloadAction = payloadAction;
		}

		@Override
		public String getLabel() {
			return payloadAction.getLabel();
		}

		@Override
		protected void onExecute() {
			dialogBox.hide();
			payloadAction.execute();
		}

		@Override
		public String getExecutionVeto() {
			return payloadAction.getExecutionVeto();
		}

		@Override
		public boolean isPermitted() {
			return payloadAction.isPermitted();
		}

		@Override
		public String getId() {
			return payloadAction.getId();
		}

	}

}
