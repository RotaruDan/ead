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
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import es.eucm.ead.editor.control.MockupController.Dpi;
import es.eucm.ead.editor.editorui.MockupUITest;
import es.eucm.ead.editor.view.widgets.IconButton;
import es.eucm.ead.editor.view.widgets.PanelOverActor;
import es.eucm.ead.editor.view.widgets.layouts.LinearLayout;
import es.eucm.ead.engine.I18N;

public class PanelOverActorTest extends MockupUITest {

	@Override
	protected Actor buildUI(Skin skin, I18N i18n) {

		Table table = new Table().top();
		table.setFillParent(true);

		IconButton leftMenu = new IconButton("menu", skin);
		IconButton leftPanelMenu = new IconButton("menu", skin);
		PanelOverActor leftPanel = new PanelOverActor(skin);
		leftPanel.setReferenceActor(leftMenu);
		leftPanel.setReferenceChild(leftPanelMenu);

		LinearLayout layout = new LinearLayout(true);
		layout.add(leftPanelMenu);
		layout.add(new Label("just a text", skin));
		leftPanel.add(layout);
		for (int i = 0; i < 4; ++i) {
			leftPanel.row();
			leftPanel.add("another text " + i);
		}

		IconButton rightMenu = new IconButton("menu", skin);
		IconButton rightPanelMenu = new IconButton("menu", skin);
		PanelOverActor rightPanel = new PanelOverActor(skin);
		rightPanel.setReferenceActor(rightMenu);
		rightPanel.setReferenceChild(rightPanelMenu);
		rightPanel.setHideChild(false);

		layout = new LinearLayout(true);
		layout.add(new Label("just a text", skin));
		layout.add(rightPanelMenu);
		rightPanel.add(layout);
		for (int i = 0; i < 4; ++i) {
			rightPanel.row();
			rightPanel.add("another text " + i);
		}

		table.add(leftMenu);
		table.add().expandX();
		table.add(rightMenu);

		final PanelOverActor[] panels = new PanelOverActor[] { leftPanel,
				rightPanel };

		table.addAction(Actions.forever(Actions.sequence(Actions.delay(2f,
				Actions.run(new Runnable() {
					@Override
					public void run() {
						for (PanelOverActor widget : panels) {
							if (widget.hasParent()) {
								widget.hide();
							} else {
								widget.show();
							}
						}
					}
				})))));

		return table;
	}

	public static void main(String[] args) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 500;
		config.height = 300;
		config.overrideDensity = MathUtils.round(Dpi.HDPI.getMaxDpi());
		PanelOverActorTest test = new PanelOverActorTest();
		config.title = test.getClass().getSimpleName();
		new LwjglApplication(test, config);
	}

}