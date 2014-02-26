/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2013 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          C Profesor Jose Garcia Santesmases sn,
 *          28040 Madrid (Madrid), Spain.
 *
 *          For more info please visit:  <http://e-adventure.e-ucm.es> or
 *          <http://www.e-ucm.es>
 *
 * ****************************************************************************
 *
 *  This file is part of eAdventure
 *
 *      eAdventure is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      eAdventure is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with eAdventure.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.eucm.ead.editor.view.builders.mockup.gallery;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.actions.ChangeView;
import es.eucm.ead.editor.view.builders.mockup.camera.Picture;
import es.eucm.ead.editor.view.builders.mockup.camera.Video;
import es.eucm.ead.editor.view.widgets.GridLayout;
import es.eucm.ead.editor.view.widgets.mockup.ToolBar;
import es.eucm.ead.editor.view.widgets.mockup.buttons.BottomProjectMenuButton;
import es.eucm.ead.engine.I18N;

public class Gallery extends BaseGallery {

	public static final String NAME = "mockup_gallery";

	private static final String IC_PHOTOCAMERA = "ic_photocamera",
			IC_VIDEOCAMERA = "ic_videocamera";

	private static final float PREF_BOTTOM_BUTTON_WIDTH = .25F;
	private static final float PREF_BOTTOM_BUTTON_HEIGHT = .2F;

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected WidgetGroup centerWidget(Vector2 viewport, I18N i18n, Skin skin, Controller controller) {
		
		GridLayout galleryTable = new GridLayout();
		galleryTable.pad(2);
		galleryTable.setFillParent(true);

		Table tIn = new Table().debug();
		tIn.pad(2);
		tIn.setFillParent(true);

		// FIXME (Testing GridLayout)
		for (int i = 10; i < 40; i++) {
			galleryTable.addActor(new TextButton("proyecto" + i, skin));
			tIn.add(new TextButton("proyecto " + i, skin));
			tIn.row();
		}
		// END FIXME

		Table window = new Table();
		window.setFillParent(true);

		ScrollPane sp = new ScrollPane(galleryTable);
		sp.setScrollingDisabled(true, false);
		sp.layout();
		
		return sp;
	}

	@Override
	protected WidgetGroup bottomWidget(Vector2 viewport, I18N i18n, Skin skin, Controller controller) {
		ToolBar botBar = new ToolBar(viewport, skin);

		BottomProjectMenuButton pictureButton = new BottomProjectMenuButton(
				viewport, i18n.m("general.mockup.photo"), skin, IC_PHOTOCAMERA,
				PREF_BOTTOM_BUTTON_WIDTH, PREF_BOTTOM_BUTTON_HEIGHT,
				controller, ChangeView.NAME, Picture.NAME);
		BottomProjectMenuButton videoButton = new BottomProjectMenuButton(
				viewport, i18n.m("general.mockup.video"), skin, IC_VIDEOCAMERA,
				PREF_BOTTOM_BUTTON_WIDTH, PREF_BOTTOM_BUTTON_HEIGHT,
				controller, ChangeView.NAME, Video.NAME);

		botBar.add(pictureButton).left();
		botBar.add("").expandX();
		botBar.add(videoButton).right();

		return botBar;
	}
}
