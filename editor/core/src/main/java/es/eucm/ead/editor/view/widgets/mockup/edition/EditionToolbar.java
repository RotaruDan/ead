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
package es.eucm.ead.editor.view.widgets.mockup.edition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.actions.Action.ActionListener;
import es.eucm.ead.editor.control.actions.editor.Redo;
import es.eucm.ead.editor.control.actions.editor.Undo;
import es.eucm.ead.editor.control.background.BackgroundExecutor;
import es.eucm.ead.editor.control.background.BackgroundExecutor.BackgroundTaskListener;
import es.eucm.ead.editor.control.background.BackgroundTask;
import es.eucm.ead.editor.view.builders.mockup.edition.EditionWindow;
import es.eucm.ead.editor.view.listeners.ActionOnClickListener;
import es.eucm.ead.editor.view.widgets.mockup.ToolBar;
import es.eucm.ead.editor.view.widgets.mockup.buttons.ToolbarButton;
import es.eucm.ead.editor.view.widgets.mockup.edition.draw.BrushStrokes;
import es.eucm.ead.editor.view.widgets.mockup.edition.draw.PaintComponent;
import es.eucm.ead.editor.view.widgets.mockup.engine.MockupEngineView;
import es.eucm.ead.engine.I18N;

public class EditionToolbar extends ToolBar {

	private static final String LOGTAG = "AddElementComponent";
	private static final String IC_GO_BACK = "ic_goback", IC_UNDO = "ic_undo",
			IC_SAVE = "ic_save";

	private final BrushStrokes brushStrokes;
	private final EraserComponent eraser;
	private final PaintComponent paint;
	private EditionWindow parent;

	public EditionToolbar(final EditionWindow parent,
			final Controller controller, I18N i18n, Skin skin,
			Vector2 viewport, Table center, MockupEngineView scaledView) {
		super(viewport, skin);

		this.brushStrokes = new BrushStrokes(scaledView.getSceneview(),
				controller);
		scaledView.setBrushStrokes(brushStrokes);

		eraser = new EraserComponent(parent, controller, skin);
		paint = new PaintComponent(parent, controller, skin);
		this.parent = parent;
		center.addActor(eraser);
		center.addActor(paint);
		eraser.setBrushStrokes(brushStrokes);
		paint.setBrushStrokes(brushStrokes);

		this.setVisible(false);
		final Button backButton = new ToolbarButton(viewport, IC_GO_BACK,
				i18n.m("general.cancel"), false, skin);
		backButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				setVisible(false);
			}
		});

		final Button saveButton = new ToolbarButton(viewport, IC_SAVE,
				i18n.m("general.save"), false, skin);

		saveButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				controller.getBackgroundExecutor().submit(saveTask,
						saveListener);
			}

			private final BackgroundTaskListener<Boolean> saveListener = new BackgroundTaskListener<Boolean>() {

				@Override
				public void completionPercentage(float percentage) {
				}

				@Override
				public void done(BackgroundExecutor backgroundExecutor,
						Boolean result) {
					Gdx.app.log(LOGTAG, "done saving, result is: " + result);
					if (result) {
						EditionToolbar.this.brushStrokes.createSceneElement();
					}
					setVisible(false);
				}

				@Override
				public void error(Throwable e) {
					Gdx.app.error(LOGTAG, "error saving", e);
				}
			};

			private final BackgroundTask<Boolean> saveTask = new BackgroundTask<Boolean>() {
				@Override
				public Boolean call() throws Exception {

					boolean saved = EditionToolbar.this.brushStrokes.save();
					setCompletionPercentage(.5f);
					EditionToolbar.this.brushStrokes.release();
					setCompletionPercentage(1f);

					return saved;
				}
			};
		});

		/* Undo & Redo buttons */
		final Button undo = new ToolbarButton(viewport,
				skin.getDrawable(IC_UNDO), i18n.m("general.undo"), skin);
		undo.addListener(new ActionOnClickListener(controller, Undo.class));

		final TextureRegion redoRegion = new TextureRegion(
				skin.getRegion(IC_UNDO));
		redoRegion.flip(true, true);
		final TextureRegionDrawable redoDrawable = new TextureRegionDrawable(
				redoRegion);
		final Button redo = new ToolbarButton(viewport, redoDrawable,
				i18n.m("general.redo"), skin);
		redo.addListener(new ActionOnClickListener(controller, Redo.class));

		undo.setVisible(false);
		redo.setVisible(false);
		controller.getActions().addActionListener(Undo.class,
				new ActionListener() {
					@Override
					public void enableChanged(Class actionClass, boolean enable) {
						undo.setVisible(enable);
					}
				});
		controller.getActions().addActionListener(Redo.class,
				new ActionListener() {
					@Override
					public void enableChanged(Class actionClass, boolean enable) {
						redo.setVisible(enable);
					}
				});

		this.add(backButton).left().expandX();
		this.add(saveButton).left().expandX();
		this.add(undo, redo, paint.getButton(), eraser.getButton());

		new ButtonGroup(undo, redo);
		new ButtonGroup(paint.getButton(), eraser.getButton(), saveButton,
				backButton);
	}

	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			paint.show();
			parent.getTop().setVisible(false);
		} else {
			eraser.hide();
			paint.hide();
			brushStrokes.release();
			brushStrokes.clearMesh();
			parent.getTop().setVisible(true);
		}
		brushStrokes.setVisible(visible);
		super.setVisible(visible);
	}
}
