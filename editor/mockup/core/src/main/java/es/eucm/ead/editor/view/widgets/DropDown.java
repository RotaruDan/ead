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

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import es.eucm.ead.editor.view.widgets.PanelOverActor.PanelOverActorStyle;

/**
 * A drop down allows a user to choose one of a number of values from a list.
 * When inactive, the selected value is displayed. When activated, it shows the
 * list of values that may be selected.
 * <p>
 * {@link ChangeEvent} is fired when the drop down selection changes.
 * <p>
 * The preferred size of the select box is determined by the maximum bounds of
 * the items and the size of the {@link DropDownStyle#background}.
 */
public class DropDown extends Container<Actor> implements Disableable {
	private static final float FADE = .25f;

	private static final Vector2 tmpCoords = new Vector2();

	private ClickListener clickListener;
	private DropDownStyle style;
	private ListScroll scroll;
	private boolean disabled;
	private Actor selection;
	private boolean changeIcon;
	private float prefWidth, prefHeight;

	public DropDown(Skin skin) {
		this(skin, skin.get(DropDownStyle.class), true);
	}

	public DropDown(Skin skin, boolean changeIcon) {
		this(skin, skin.get(DropDownStyle.class), changeIcon);
	}

	public DropDown(Skin skin, String styleName, boolean changeIcon) {
		this(skin, skin.get(styleName, DropDownStyle.class), changeIcon);
	}

	public DropDown(Skin skin, String styleName) {
		this(skin, skin.get(styleName, DropDownStyle.class), true);
	}

	private DropDown(Skin skin, DropDownStyle style, boolean changeIcon) {
		setStyle(style);
		setBackground(style.background);
		this.changeIcon = changeIcon;
		scroll = new ListScroll(skin, style.panelOverActorStyle);

		addListener(clickListener = new ClickListener() {
			public boolean touchDown(InputEvent event, float x, float y,
					int pointer, int button) {
				if (pointer == 0 && button != 0)
					return false;
				if (disabled)
					return false;
				showList();
				return true;
			}
		});
	}

	@Override
	public float getPrefWidth() {
		if (getActor() != null) {
			prefWidth = super.getPrefWidth();
		}
		return prefWidth;
	}

	@Override
	public float getPrefHeight() {
		if (getActor() != null) {
			prefHeight = super.getPrefHeight();
		}
		return prefHeight;
	}

	public void setStyle(DropDownStyle style) {
		if (style == null)
			throw new IllegalArgumentException("style cannot be null.");
		this.style = style;
		invalidateHierarchy();
	}

	/**
	 * Returns the drop down's style. Modifying the returned style may not have
	 * an effect until {@link #setStyle(DropDowntyle)} is called.
	 */
	public DropDownStyle getStyle() {
		return style;
	}

	public void setItems(Actor... newItems) {
		if (newItems == null)
			throw new IllegalArgumentException("newItems cannot be null.");

		resetScroll();
		for (int i = 1; i < newItems.length; ++i) {
			addToList(newItems[i]);
		}

		if (newItems.length > 0) {
			setSelected(newItems[0]);
		}

	}

	public void setItems(Array<Actor> newItems) {
		if (newItems == null)
			throw new IllegalArgumentException("newItems cannot be null.");

		resetScroll();
		for (int i = 1; i < newItems.size; ++i) {
			addToList(newItems.get(i));
		}

		if (newItems.size > 0) {
			setSelected(newItems.first());
		}

	}

	private void resetScroll() {
		scroll.clearChildren();
		scroll.first = scroll.add((Actor) null);
		scroll.row();
	}

	public Array<Actor> getItems() {
		return scroll.getChildren();
	}

	@Override
	protected void drawBackground(Batch batch, float parentAlpha, float x,
			float y) {
		Drawable background;
		if (disabled && style.backgroundDisabled != null) {
			background = style.backgroundDisabled;
		} else if (clickListener.isOver() && style.backgroundOver != null) {
			background = style.backgroundOver;
		} else {
			background = style.background;
		}
		if (background != null) {
			setBackground(background);
		}
		super.drawBackground(batch, parentAlpha, x, y);
	}

	/** Returns the selected actor, or null. */
	public Actor getSelected() {
		return selection;
	}

	/**
	 * Sets the selection
	 */
	public void setSelected(Actor item) {
		if (getActor() == item) {
			return;
		}
		Cell<Actor> cell = scroll.getCell(item);
		if (cell != null) {
			cell.setActor(selection);
		}
		selection = item;
		setActor(item);
		scroll.setReferenceActor(selection);
		scroll.setReferenceChild(selection);
		invalidateHierarchy();
	}

	private void addToList(Actor actor) {
		scroll.add(actor);
		scroll.row();
	}

	public void setDisabled(boolean disabled) {
		if (disabled && !this.disabled)
			hideList();
		this.disabled = disabled;
	}

	public void showList() {
		scroll.show(getStage());
	}

	public void hideList() {
		scroll.hide();
	}

	/** Returns the list shown when the select box is open. */
	public Table getList() {
		return scroll;
	}

	private class ListScroll extends PanelOverActor {

		private Cell<Actor> first;

		private final Runnable addSelection = new Runnable() {

			@Override
			public void run() {
				selection.setPosition(0f, 0f);
				first.setActor(selection);
				selection = null;
			}
		};

		private final Runnable setSelection = new Runnable() {

			@Override
			public void run() {
				setSelected(selection);
				selection.setTouchable(Touchable.enabled);
			}
		};

		public ListScroll(Skin skin, PanelOverActorStyle panelOverActorStyle) {
			super(skin, panelOverActorStyle);
			defaults().uniform();
			setHideChild(false);
			setUpdatePositionOnChildrenChanged(false);
			addListener(new InputListener() {
				public boolean touchDown(InputEvent event, float x, float y,
						int pointer, int button) {
					if (changeIcon) {
						Actor target = event.getTarget();
						if (target != null) {
							Cell<Actor> cell = getCell(target);
							if (cell != null && cell != first) {
								setSelected(target);
								cell.setActor(first.getActor());
								ChangeEvent changeEvent = Pools
										.obtain(ChangeEvent.class);
								changeEvent.setListenerActor(DropDown.this);
								DropDown.this.fire(changeEvent);
								Pools.free(changeEvent);
							}
						}
					}
					hideList();
					return false;
				}
			});
		}

		@Override
		public void pack() {
			first.setActor(selection);
			super.pack();
		}

		@Override
		public void positionPanel() {
			selection.localToStageCoordinates(tmpCoords.set(0f, 0f));
			super.positionPanel();
			selection.setPosition(tmpCoords.x, tmpCoords.y);
			first.setActor(null);
		}

		@Override
		public void show(Stage stage) {
			super.show(stage);
			stage.addActor(selection);
		}

		@Override
		protected Action getShowAction() {
			return Actions.sequence(super.getShowAction(),
					Actions.run(addSelection));
		}

		@Override
		public void hide() {
			if (selection == null || getActions().size > 0) {
				clearActions();
				if (selection == null) {
					selection = first.getActor();
				}
				selection.localToStageCoordinates(tmpCoords.set(0f, 0f));
				int selectionX = MathUtils.round(tmpCoords.x);
				int selectionY = MathUtils.round(tmpCoords.y);
				selection.setPosition(selectionX, selectionY);
				getStage().addActor(selection);
				hide(Actions.sequence(getHideAction(),
						Actions.run(setSelection)));
			} else {
				hide(getHideAction());
			}

		}
	}

	/**
	 * The style for a drop down, see {@link DropDown}.
	 * 
	 */
	static public class DropDownStyle {

		/** Optional. */
		public Drawable background;

		public PanelOverActorStyle panelOverActorStyle;

		/** Optional. */
		public Drawable backgroundOver, backgroundDisabled;

		public DropDownStyle() {
		}

		public DropDownStyle(Drawable background,
				PanelOverActorStyle panelOverActorStyle) {
			this.background = background;
			this.panelOverActorStyle = panelOverActorStyle;
		}

		public DropDownStyle(DropDownStyle style) {
			this.background = style.background;
			this.backgroundOver = style.backgroundOver;
			this.panelOverActorStyle = style.panelOverActorStyle;
			this.backgroundDisabled = style.backgroundDisabled;
		}
	}

	public static class DropdownChangeListener extends ChangeListener {

		private Actor currentSelected;

		@Override
		public void changed(ChangeEvent changeEvent, Actor actor) {
			DropDown listenerActor = (DropDown) changeEvent.getListenerActor();
			Actor selected = listenerActor.getSelected();
			if (currentSelected != selected) {
				currentSelected = selected;
				changed(selected, listenerActor);
			}
		}

		public void changed(Actor selected, DropDown listenerActor) {

		}
	}
}
