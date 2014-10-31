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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import es.eucm.ead.editor.view.widgets.PositionedHiddenPanel.Position;
import es.eucm.ead.editor.view.widgets.ScalePanel;

/**
 * A {@link IconWithPositionedPanel} that has a scale in/out animation and a
 * FadeIn for each children when showing.
 */
public abstract class BaseIconWithScalePanel extends IconWithPositionedPanel {

	private final Runnable showCells = new Runnable() {

		public void run() {
			Array<Cell> cells = panel.getCells();
			for (int i = 0; i < cells.size; ++i) {
				Actor actor = cells.get(i).getActor();
				if (actor != null) {
					actor.addAction(Actions.delay(i * .05f,
							Actions.fadeIn(.2f, Interpolation.fade)));
				}
			}
		}
	};

	public BaseIconWithScalePanel(String icon, float separation, Skin skin,
			Position position, int paneCol, String styleName) {
		super(icon, separation, skin, position, paneCol, styleName);
		panel.setTransform(true);
	}

	@Override
	protected Action getShowAction() {
		float xDuration, yDuration;
		float w = panel.getPrefWidth(), h = panel.getPrefHeight();
		if (w > h) {
			yDuration = IN_DURATION * h / w;
			xDuration = IN_DURATION;
		} else {
			xDuration = IN_DURATION * w / h;
			yDuration = IN_DURATION;
		}

		panel.setScale(0f);
		for (Cell<Actor> cell : panel.getCells()) {
			Actor actor = cell.getActor();
			if (actor != null) {
				actor.getColor().a = 0f;
			}
		}
		return ScalePanel.showAction(xDuration, yDuration, showCells);
	}

	@Override
	protected Action getHideAction() {
		return ScalePanel.hideAction(OUT_DURATION);
	}

}
