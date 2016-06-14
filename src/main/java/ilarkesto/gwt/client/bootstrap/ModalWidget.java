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
package ilarkesto.gwt.client.bootstrap;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ModalWidget implements IsWidget {

	private static int lastId = 0;

	private String modalId = "modal_" + (++lastId);
	private String labelId = modalId + "_label";
	private SimplePanel modal;
	private FlowPanel modalDialog;
	private FlowPanel modalContent;

	public ModalWidget() {
		modal = new SimplePanel();
		Element modalElement = modal.getElement();
		modalElement.setId(modalId);
		modalElement.setTabIndex(-1);
		modalElement.setAttribute("role", "dialog");
		modalElement.setAttribute("aria-labelledby", labelId);
		modalElement.setAttribute("aria-hidden", "true");
		modal.addStyleName("modal");
		modal.addStyleName("fade");

		modalDialog = new FlowPanel();
		modalDialog.addStyleName("modal-dialog");
		modalDialog.getElement().setAttribute("role", "document");
		modal.add(modalDialog);

		modalContent = new FlowPanel();
		modalContent.addStyleName("modal-content");
		modalDialog.add(modalContent);
	}

	@Override
	public Widget asWidget() {
		return modal;
	}

	public FlowPanel getModalContent() {
		return modalContent;
	}

	public void show() {
		show(modalId);
	}

	private static native void show(String id)
	/*-{
	$wnd.$('#'+id).modal('show');
	}-*/;

	public void hide() {
		hide(modalId);
	}

	private static native void hide(String id)
	/*-{
	$wnd.$('#'+id).modal('hide');
	}-*/;

}
