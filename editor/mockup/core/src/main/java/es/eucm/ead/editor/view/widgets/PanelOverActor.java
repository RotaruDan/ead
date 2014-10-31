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
package es.eucm.ead.editor.view.widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * A panel with a scale animation that is positioned on top of the
 * {@link #referenceActor} in a way that the {@link #referenceChild}'s position
 * will coincide with the {@link #referenceActor}'s.
 * 
 */
public class PanelOverActor extends ScalePanel {

	private static final Vector2 tmpCoords = new Vector2();

	private Actor referenceActor;
	private Actor referenceChild;

	private boolean hideChild;
	private float childX, childY;
	private Color childColor = new Color();

	public PanelOverActor(Skin skin) {
		this(skin, skin.get(PanelOverActorStyle.class));
	}

	public PanelOverActor(Skin skin, PanelOverActorStyle style) {
		super(skin, style.background);
		hideChild = true;
	}

	/**
	 * 
	 * @param referenceActor
	 *            used to position the panel.
	 */
	public void setReferenceActor(Actor referenceActor) {
		this.referenceActor = referenceActor;
	}

	/**
	 * @param referenceChild
	 *            will be positioned right on top of {@link #referenceActor}
	 *            when the panel is opened. Must be a child of this panel (not
	 *            necessarily a direct child).
	 */
	public void setReferenceChild(Actor referenceChild) {
		this.referenceChild = referenceChild;
	}

	/**
	 * 
	 * @param hideChild
	 *            if true {@link #referenceChild} will get drawn on top of the
	 *            {@link #referenceActor} while opening/hiding. Default is true.
	 */
	public void setHideChild(boolean hideChild) {
		this.hideChild = hideChild;
	}

	public void show() {
		show(referenceActor.getStage());
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		super.draw(batch, parentAlpha);
		if (hideChild && referenceChild.getColor().a == 0) {
			float childX = referenceChild.getX();
			float childY = referenceChild.getY();
			referenceChild.setPosition(this.childX + getX(), this.childY
					+ getY());
			referenceChild.setColor(childColor);
			referenceChild.draw(batch, parentAlpha);
			referenceChild.getColor().a = 0f;
			referenceChild.setPosition(childX, childY);

		}
	}

	@Override
	public void hide() {
		if (hideChild) {
			referenceChild.getColor().a = 0f;
		}
		super.hide();
	}

	@Override
	protected Action getShowAction() {
		return hideChild ? Actions.parallel(
				super.getShowAction(),
				Actions.delay(getChildren().size * CHILD_DELAY_OFFSET
						+ CHILD_FADE + FADE, Actions.run(hideChildColor)))
				: super.getShowAction();
	}

	private Runnable hideChildColor = new Runnable() {
		@Override
		public void run() {
			referenceChild.clearActions();
			referenceChild.setColor(childColor);
		}
	};

	@Override
	public void positionPanel() {
		referenceActor.localToStageCoordinates(tmpCoords.set(0f, 0f));
		float stageX = tmpCoords.x;
		float stageY = tmpCoords.y;

		pack();
		layout();

		referenceChild.localToAscendantCoordinates(this, tmpCoords.set(0f, 0f));
		childX = tmpCoords.x;
		childY = tmpCoords.y;
		if (hideChild) {
			if (childColor.a != 0f) {
				referenceChild.setColor(childColor);
			}
			childColor.set(referenceChild.getColor());
			referenceChild.getColor().a = 0f;
		}

		float prefWidth = getPrefWidth();
		float prefHeight = getPrefHeight();

		setBounds(MathUtils.round(stageX - childX),
				MathUtils.round(stageY - childY), MathUtils.round(prefWidth),
				MathUtils.round(prefHeight));

		if (referenceChild.getX() > (prefWidth - referenceChild.getWidth()) * .5f) {
			setOriginX(prefWidth);
		} else {
			setOriginX(0);
		}
		setOriginY(prefHeight);
	}

	/**
	 * The style for a panel, see {@link PanelOverActor}.
	 * 
	 */
	static public class PanelOverActorStyle {

		/** Optional. */
		public Drawable background;

		public PanelOverActorStyle() {
		}

		public PanelOverActorStyle(Drawable background) {
			this.background = background;
		}

		public PanelOverActorStyle(PanelOverActorStyle style) {
			this.background = style.background;
		}
	}
}
