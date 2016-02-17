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
package ilarkesto.tools.picturemanager.swing;

import ilarkesto.swing.Swing;
import ilarkesto.tools.picturemanager.Picture;
import ilarkesto.tools.picturemanager.PicturesProvider;
import ilarkesto.tools.picturemanager.Repository;

import java.awt.Color;

import javax.swing.JPanel;

public class PicturesPanel extends JPanel {

	public static void main(String[] args) {
		Swing.showInJFrame(new PicturesPanel(Repository.createTestDummy().folders().next()));
	}

	private PicturesProvider picturesProvider;

	public PicturesPanel(PicturesProvider picturesProvider) {
		super();
		this.picturesProvider = picturesProvider;

		setBackground(Color.black);

		for (Picture picture : picturesProvider.pictures().list()) {
			add(new PicturePanel(picture));
		}
	}

}
