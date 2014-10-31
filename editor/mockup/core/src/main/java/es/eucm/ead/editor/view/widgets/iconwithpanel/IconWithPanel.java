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
package es.eucm.ead.editor.view.widgets.iconwithpanel;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import es.eucm.ead.editor.view.widgets.HiddenPanel;
import es.eucm.ead.editor.view.widgets.IconButton;

/**
 * An {@link IconButton} with a {@link HiddenPanel} as attribute, when clicked
 * the panel is automatically added to the {@link Stage} and removed from it
 * when clicked again. The panel is drawn in {@link Stage} coordinates.
 * 
 */
public abstract class IconWithPanel<T extends HiddenPanel> extends IconButton {

	private static final ChangeListener showOrHide = new ChangeListener() {

		public void changed(ChangeEvent event,
				com.badlogic.gdx.scenes.scene2d.Actor actor) {
			IconWithPanel<?> icon = (IconWithPanel<?>) event.getListenerActor();
			if (!icon.panel.hasParent()) {
				icon.showPanel();
			}
		};
	};

	protected T panel;

	public IconWithPanel(String icon, Skin skin) {
		this(icon, skin, -1);
	}

	public IconWithPanel(String icon, Skin skin, int paneCol) {
		this(icon, skin, paneCol, "default");
	}

	public IconWithPanel(String icon, Skin skin, int paneCol, String styleName) {
		super(icon, 0f, skin, styleName);
		panel.setColumns(paneCol);
	}

	@Override
	protected void init(Drawable icon, float padding, Skin skin) {
		super.init(icon, padding, skin);
		panel = createPanel(skin);
		addListener(showOrHide);
	}

	protected abstract T createPanel(Skin skin);

	public void showPanel() {
		panel.show(getStage());
	}

	public void hidePanel() {
		panel.hide();
	}

	public HiddenPanel getPanel() {
		return panel;
	}
}
