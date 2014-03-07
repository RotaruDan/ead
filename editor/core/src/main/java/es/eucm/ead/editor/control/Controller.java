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
package es.eucm.ead.editor.control;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import es.eucm.ead.editor.assets.EditorAssets;
import es.eucm.ead.editor.assets.ProjectAssets;
import es.eucm.ead.editor.control.actions.EditorActionException;
import es.eucm.ead.editor.control.actions.UpdateRecents;
import es.eucm.ead.editor.control.commands.Command;
import es.eucm.ead.editor.control.pastelisteners.SceneElementPasteListener;
import es.eucm.ead.editor.control.pastelisteners.ScenePasteListener;
import es.eucm.ead.editor.model.Model;
import es.eucm.ead.editor.platform.Platform;
import es.eucm.ead.schema.actors.Scene;
import es.eucm.ead.schema.actors.SceneElement;

/**
 * Mediator and main controller of the editor's functionality
 * 
 */
public class Controller {

	/**
	 * Default name for the editor's preferences.
	 */
	private static final String DEFAULT_PREFERENCES_FILE = "preferences.json";

	/**
	 * Game model managed by the editor.
	 */
	private Model model;

	/**
	 * Platform dependent functionality
	 */
	private Platform platform;

	/**
	 * Asset manager used for internal's editor assets.
	 */
	private EditorAssets editorAssets;

	/**
	 * Asset manager for the current openend game's project.
	 */
	private ProjectAssets projectAssets;

	protected Views views;

	private Actions actions;

	/**
	 * Manages editor preferences
	 */
	private Preferences preferences;

	/**
	 * Manage editor's command history.
	 */
	private Commands commands;

	private EditorIO editorIO;

	/**
	 * Manage keyboard mappings to editor's functionality
	 */
	private KeyMap keyMap;

	private Clipboard clipboard;

	public Controller(Platform platform, Files files, Group rootView) {
		this.platform = platform;
		this.editorAssets = new EditorAssets(files);
		editorAssets.finishLoading();
		this.projectAssets = new ProjectAssets(files, editorAssets);
		this.model = new Model();
		this.commands = new Commands(model);
		this.views = createViews(rootComponent);
		this.editorIO = new EditorIO(this);
		this.actions = new Actions(this);
		this.preferences = new Preferences(
				editorAssets.resolve(DEFAULT_PREFERENCES_FILE));
		this.keyMap = new KeyMap(actions);
		setClipboard();
		// Shortcuts listener
		rootComponent.addListener(new InputListener() {
			private boolean ctrl = false;
			private boolean alt = false;
			private boolean shift = false;

			@Override
			public boolean keyDown(InputEvent event, int keycode) {
				switch (keycode) {
				case Keys.CONTROL_LEFT:
				case Keys.CONTROL_RIGHT:
					ctrl = true;
					return true;
				case Keys.ALT_LEFT:
				case Keys.ALT_RIGHT:
					alt = true;
					return true;
				case Keys.SHIFT_LEFT:
				case Keys.SHIFT_RIGHT:
					shift = true;
					return true;
				default:
					String shortcut = "";
					if (ctrl) {
						shortcut += "ctrl+";
					}
					if (alt) {
						shortcut += "alt+";
					}
					if (shift) {
						shortcut += "shift+";
					}

					shortcut += Keys.toString(event.getKeyCode()).toLowerCase();
					return keyMap.shortcut(shortcut);
				}
			}

			@Override
			public boolean keyUp(InputEvent event, int keycode) {
				switch (keycode) {
				case Keys.CONTROL_LEFT:
				case Keys.CONTROL_RIGHT:
					ctrl = false;
					return true;
				case Keys.ALT_LEFT:
				case Keys.ALT_RIGHT:
					alt = false;
					return true;
				case Keys.SHIFT_LEFT:
				case Keys.SHIFT_RIGHT:
					shift = false;
					return true;
				default:
					return false;
				}
			}
		});
		loadPreferences();
	}

	protected Views createViews(Group rootView) {
		return new Views(this, rootView);
	}

	private void setClipboard() {
		this.clipboard = new Clipboard(Gdx.app.getClipboard(), views,
				editorAssets);
		clipboard.registerPasteListener(Scene.class, new ScenePasteListener(
				this));
		clipboard.registerPasteListener(SceneElement.class,
				new SceneElementPasteListener(this));
	}

	/**
	 * Process preferences concerning the controller
	 */
	private void loadPreferences() {
		getEditorAssets().getI18N().setLang(
				preferences.getString(Preferences.EDITOR_LANGUAGE));
	}

	public Model getModel() {
		return model;
	}

	public ProjectAssets getProjectAssets() {
		return projectAssets;
	}

	public EditorAssets getEditorAssets() {
		return editorAssets;
	}

	public Platform getPlatform() {
		return platform;
	}

	public Preferences getPreferences() {
		return preferences;
	}

	public Commands getCommands() {
		return commands;
	}

	public Actions getActions() {
		return actions;
	}

	public Views getViews() {
		return views;
	}

	public void view(String viewName) {
		views.setView(viewName);
	}

	public KeyMap getKeyMap() {
		return keyMap;
	}

	public Clipboard getClipboard() {
		return clipboard;
	}

	/**
	 * Executes an editor action with the given name and arguments
	 * 
	 * @param actionClass
	 *            the action class
	 * @param args
	 *            the arguments for the action
	 */
	public void action(Class actionClass, Object... args) {
		try {
			Gdx.app.debug("Controller", "Executing action " + actionClass
					+ " with args" + prettyPrintArgs(args));
			actions.perform(actionClass, args);
		} catch (ClassCastException e) {
			throw new EditorActionException(
					"Something went wrong when executing action "
							+ actionClass
							+ " with arguments "
							+ prettyPrintArgs(args)
							+ ". Perhaps the number of arguments is not correct or these are not valid",
					e);
		} catch (NullPointerException e) {
			throw new EditorActionException(
					"Something went wrong when executing action "
							+ actionClass
							+ " with arguments "
							+ prettyPrintArgs(args)
							+ ". Perhaps the number of arguments is not correct or these are not valid",
					e);
		}
	}

	/**
	 * Just formats an array of objects for console printing. For debugging only
	 */
	private String prettyPrintArgs(Object... args) {
		String str = "[";
		for (Object arg : args) {
			str += (arg instanceof String ? "\"" : "")
					+ (arg == null ? "null" : arg.toString())
					+ (arg instanceof String ? "\"" : "") + " , ";
		}
		if (args.length > 0) {
			str = str.substring(0, str.length() - 3);
		}
		str += "]";
		return str;
	}

	/**
	 * Executes a command, an takes care of notifying to all model listeners all
	 * the changes performed by it
	 * 
	 * @param command
	 *            the command
	 */
	public void command(Command command) {
		commands.command(command);
	}

	public String getLoadingPath() {
		return projectAssets.getLoadingPath();
	}

	public void loadGame(String gamePath, boolean internal) {
		editorIO.load(gamePath, internal);
		actions.perform(UpdateRecents.class, getLoadingPath());
	}

	public void saveAll() {
		editorIO.saveAll(model);
	}

	public EditorIO getEditorIO() {
		return editorIO;
	}

	public void setLanguage(String language) {
		getEditorAssets().getI18N().setLang(language);
		views.clearCache();
		views.reloadCurrentView();
		preferences.putString(Preferences.EDITOR_LANGUAGE, language);
	}

	public static interface BackListener {
		/**
		 * Called when the Back key was pressed in Android.
		 */
		void onBackPressed();
	}
}
