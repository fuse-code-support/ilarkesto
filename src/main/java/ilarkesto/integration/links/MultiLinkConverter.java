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
package ilarkesto.integration.links;

import ilarkesto.core.base.LinkConverter;

import java.util.ArrayList;
import java.util.List;

public class MultiLinkConverter implements LinkConverter {

	public static MultiLinkConverter ALL = new MultiLinkConverter().add(new ImageLinkConverter(),
		new YoutubeLinkConverter(), new VimeoLinkConverter());

	private List<LinkConverter> converters = new ArrayList<LinkConverter>();

	public MultiLinkConverter add(LinkConverter... converters) {
		for (LinkConverter converter : converters) {
			this.converters.add(converter);
		}
		return this;
	}

	@Override
	public String convert(String href, int maxWidth) {
		if (href == null) return null;
		for (LinkConverter converter : converters) {
			String s = converter.convert(href, maxWidth);
			if (!s.equals(href)) return s;
		}
		return href;
	}

}
