/**
 * eAdventure is a research project of the
 *    e-UCM research group.
 *
 *    Copyright 2005-2014 e-UCM research group.
 *
 *    You can access a list of all the contributors to eAdventure at:
 *          http://e-adventure.e-ucm.es/contributors
 *
 *    e-UCM is a research group of the Department of Software Engineering
 *          and Artificial Intelligence at the Complutense University of Madrid
 *          (School of Computer Science).
 *
 *          CL Profesor Jose Garcia Santesmases 9,
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
package es.eucm.ead.editor.editorui.widgets;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;

import es.eucm.ead.editor.assets.EditorGameAssets;
import es.eucm.ead.editor.editorui.MockupUITest;
import es.eucm.ead.editor.view.widgets.DropDown;
import es.eucm.ead.engine.I18N;
import es.eucm.ead.engine.assets.Assets.AssetLoadedCallback;

public class DropDownTest extends MockupUITest {

	@Override
	protected Actor buildUI(Skin skin, I18N i18n) {

		Container<Actor> container = new Container<Actor>().top();
		EditorGameAssets gameAssets = controller.getEditorGameAssets();

		// Prepare some images
		gameAssets.setLoadingPath("dropDown", true);

		Table table = new Table(skin);
		table.pad(20f);

		DropDown dropDown = new DropDown(skin);
		dropDown.getList().pad(20, 40, 60, 80);
		dropDown.pad(20, 40, 60, 80);
		dropDown.setItems(new Label("123", skin), new Label("456", skin),
				new Label("789", skin));

		SelectBox<String> selectBox = new SelectBox<String>(skin);
		selectBox.setItems("123", "456", "789");

		Array<Actor> images = new Array<Actor>();
		for (int i = 0; i < 4; ++i) {
			final Image image = new Image();
			image.setColor(Color.BLACK);
			gameAssets.get(i + ".png", Texture.class,
					new AssetLoadedCallback<Texture>() {

						@Override
						public void loaded(String fileName, Texture asset) {
							image.setDrawable(new TextureRegionDrawable(
									new TextureRegion(asset)));
						}

					}, true);
			images.add(image);
		}
		DropDown dropDown2 = new DropDown(skin, false);
		dropDown2.setItems(images);
		dropDown2.getList().pad(80, 60, 40, 20);
		dropDown2.pad(80, 60, 40, 20);

		table.add("Drop Down  ");
		table.add("  Select Box");
		table.row();
		table.add(dropDown, selectBox);
		table.row();
		table.add(dropDown2);

		container.setActor(table);
		container.setFillParent(true);

		return container;
	}

	public static void main(String[] args) {
		new LwjglApplication(new DropDownTest(), "Drop Down test", 700, 700);
	}
}
