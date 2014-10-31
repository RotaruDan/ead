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

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import es.eucm.ead.editor.view.widgets.IconButton;
import es.eucm.ead.editor.view.widgets.PositionedHiddenPanel;
import es.eucm.ead.editor.view.widgets.PositionedHiddenPanel.Position;

/**
 * An {@link IconButton} with a {@link PositionedHiddenPanel} as attribute, when
 * clicked the panel is added to the {@link Stage} after an animation defined
 * via {@link #getShowAction()}/{@link #getHideAction()} and removed from it
 * when clicked again. The panel is drawn in {@link Stage} coordinates.
 * 
 */
public abstract class IconWithPositionedPanel extends
		IconWithPanel<PositionedHiddenPanel> {

	protected static final float IN_DURATION = .3F;
	protected static final float OUT_DURATION = .25F;

	public IconWithPositionedPanel(String icon, float separation, Skin skin,
			Position position) {
		this(icon, separation, skin, position, -1, "default");
	}

	public IconWithPositionedPanel(String icon, float separation, Skin skin,
			Position position, int paneCol) {
		this(icon, separation, skin, position, paneCol, "default");
	}

	public IconWithPositionedPanel(String icon, float separation, Skin skin,
			Position position, String styleName) {
		this(icon, separation, skin, position, -1, styleName);
	}

	public IconWithPositionedPanel(String icon, float separation, Skin skin,
			Position position, int paneCol, String styleName) {
		super(icon, skin, paneCol, styleName);
		panel.setPosition(position);
		panel.setSpace(separation);
		panel.setReference(this);
	}

	protected PositionedHiddenPanel createPanel(Skin skin) {
		return new Panel(skin);
	}

	/**
	 * Invoked when the panel is going to be displayed, this method should
	 * return the {@link Action} used to display the {@link #panel}.
	 */
	protected abstract Action getShowAction();

	/**
	 * Invoked when the panel is going to be hidden, this method should return
	 * the {@link Action} used to hide the {@link #panel}.
	 */
	protected abstract Action getHideAction();

	public void showPanel() {
		panel.show(getShowAction());
	}

	public void hidePanel() {
		panel.hide(getHideAction());
	}

	public PositionedHiddenPanel getPanel() {
		return panel;
	}

	protected class Panel extends PositionedHiddenPanel {

		public Panel(Skin skin) {
			super(skin);

		}

		@Override
		public void hide() {
			hidePanel();
		}
	}
}
