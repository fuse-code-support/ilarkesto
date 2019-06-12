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
package ilarkesto.io;

import ilarkesto.core.base.Filename;
import ilarkesto.core.logging.Log;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageThumbFactory {

	private Log log = Log.get(ImageThumbFactory.class);

	private File thumbDir;
	private ThumbCreator thumbCreator;
	private long thumbMaxAgeMillis = -1;

	public ImageThumbFactory(File thumbDir, ThumbCreator thumbCreator) {
		super();
		this.thumbDir = thumbDir;
		this.thumbCreator = thumbCreator;
	}

	public File getThumb(File imageFile, String folder, String size) {
		return getThumb(imageFile, folder, Integer.parseInt(size));
	}

	public File getThumb(File imageFile, String folder, int size) {
		return getThumb(imageFile, folder, imageFile.getName(), size);
	}

	public File getThumb(File imageFile, String folder, String id, int size) {
		if (!imageFile.exists()) return null;
		if (new Filename(id).getSuffix() == null) {
			String suffix = new Filename(imageFile.getName()).getSuffix();
			if (suffix == null) suffix = "jpg";
			id += "." + suffix;
		}
		File thumbFile = new File(thumbDir.getPath() + "/" + folder + "/" + size + "/" + id);
		if (isThumbFileUpToDate(thumbFile, imageFile)) return thumbFile;
		log.info("Creating thumb:", imageFile, "->", thumbFile);
		IO.createDirectory(thumbFile.getParentFile());
		thumbCreator.createThumb(imageFile, thumbFile, size);
		return thumbFile;
	}

	protected boolean isThumbFileUpToDate(File thumbFile, File imageFile) {
		if (!thumbFile.exists()) return false;
		if (thumbMaxAgeMillis > 0 && thumbFile.lastModified() + thumbMaxAgeMillis < System.currentTimeMillis())
			return false;
		return thumbFile.lastModified() > imageFile.lastModified();
	}

	public static interface ThumbCreator {

		void createThumb(File imageFile, File thumbFile, int size);
	}

	public ImageThumbFactory setThumbMaxAgeMillis(long thumbMaxAgeMillis) {
		this.thumbMaxAgeMillis = thumbMaxAgeMillis;
		return this;
	}

	public static class AwtQuadratizeAndLimitSizeThumbCreator implements ThumbCreator {

		@Override
		public void createThumb(File imageFile, File thumbFile, int size) {
			BufferedImage image = Awt.loadImage(imageFile);
			Image thumbImage = Awt.quadratizeAndLimitSize(image, size);
			Awt.writeImage(thumbImage, "JPG", thumbFile);
			thumbFile.setLastModified(imageFile.lastModified());
		}

	}

}
