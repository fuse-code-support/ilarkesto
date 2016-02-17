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

import ilarkesto.base.Sys;
import ilarkesto.io.ImageThumbFactory;
import ilarkesto.io.ImageThumbFactory.AwtQuadratizeAndLimitSizeThumbCreator;
import ilarkesto.swing.Swing;
import ilarkesto.tools.picturemanager.Picture;
import ilarkesto.tools.picturemanager.Repository;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PicturePanel extends JPanel {

	private static final ImageThumbFactory thumbFactory = new ImageThumbFactory(
			new File(Sys.getUsersHomePath() + "/inbox/tmp"), new AwtQuadratizeAndLimitSizeThumbCreator());
	private static final int size = 256;

	public static void main(String[] args) {
		Swing.showInJFrame(new PicturePanel(Repository.createTestDummy().folders().next().pictures().next()));
	}

	private Picture picture;
	private boolean selected;

	public PicturePanel(Picture picture) {
		super();
		this.picture = picture;

		// setBackground(Color.black);

		File file = picture.getFile();
		file = thumbFactory.getThumb(file, "picture", size);
		System.out.println(file.exists());
		ImageIcon imageIcon = new ImageIcon(file.getPath());
		JLabel label = new JLabel(imageIcon);
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				toggleSelected();
			}
		});
		add(label);
	}

	public void toggleSelected() {
		setSelected(!selected);
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		setBackground(selected ? Color.yellow : null);
	}

	public boolean isSelected() {
		return selected;
	}
}
