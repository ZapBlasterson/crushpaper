/*
Copyright 2015 CrushPaper.com.

This file is part of CrushPaper.

CrushPaper is free software: you can redistribute it and/or modify
it under the terms of version 3 of the GNU Affero General Public
License as published by the Free Software Foundation.

CrushPaper is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with CrushPaper.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.crushpaper;

import java.util.Map;

import org.pegdown.LinkRenderer;
import org.pegdown.ToHtmlSerializer;
import org.pegdown.VerbatimSerializer;
import org.pegdown.ast.InlineHtmlNode;

/** A link renderer that renders links with just text and no html anchors. */
public class ToHtmlNoLinkSerializer extends ToHtmlSerializer {

	public ToHtmlNoLinkSerializer(LinkRenderer linkRenderer,
			Map<String, VerbatimSerializer> verbatimSerializerMap) {
		super(linkRenderer, verbatimSerializerMap);
	}

	@Override
	protected void printLink(LinkRenderer.Rendering rendering) {
		printer.print(rendering.text);
	}

	@Override
	public void visit(InlineHtmlNode node) {
		String text = node.getText();
		if (text != null
				&& (text.startsWith("<a ") || text.startsWith("<A ")
						|| text.startsWith("</a") || text.startsWith("</A")))
			return;

		printer.print(text);
	}

}
