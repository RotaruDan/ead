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
package es.eucm.ead.editor.view.builders.scene.interaction;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.model.generic.RemoveFromArray;
import es.eucm.ead.editor.control.actions.model.scene.AddComponent;
import es.eucm.ead.editor.model.Q;
import es.eucm.ead.editor.view.ModelView;
import es.eucm.ead.editor.view.SkinConstants;
import es.eucm.ead.editor.view.widgets.IconButton;
import es.eucm.ead.editor.view.widgets.WidgetBuilder;
import es.eucm.ead.editor.view.widgets.layouts.LinearLayout;
import es.eucm.ead.engine.I18N;
import es.eucm.ead.schema.components.ModelComponent;
import es.eucm.ead.schema.entities.ModelEntity;

public abstract class ComponentEditor<T extends ModelComponent> extends
		LinearLayout implements ModelView {

	protected Controller controller;

	protected I18N i18N;

	protected Skin skin;

	private String componentId;

	private String icon;

	public ComponentEditor(String icon, String label, String componentId,
			Controller cont) {
		super(false);
		this.icon = icon;
		this.controller = cont;
		this.i18N = controller.getApplicationAssets().getI18N();
		this.skin = controller.getApplicationAssets().getSkin();
		this.componentId = componentId;
		background(controller.getApplicationAssets().getSkin()
				.getDrawable(SkinConstants.DRAWABLE_PAGE_RIGHT));
		LinearLayout header = new LinearLayout(true);
		header.defaultWidgetsMargin(WidgetBuilder.dpToPixels(8),
				WidgetBuilder.dpToPixels(8), 0, 0);
		header.add(WidgetBuilder.icon(icon, SkinConstants.STYLE_GRAY));
		header.add(WidgetBuilder.label(label, SkinConstants.STYLE_EDITION));
		header.addSpace();

		IconButton delete = WidgetBuilder.icon(SkinConstants.IC_DELETE,
				SkinConstants.STYLE_EDITION);
		delete.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				ModelEntity modelEntity = (ModelEntity) controller.getModel()
						.getSelection().getSingle(Selection.SCENE_ELEMENT);
				ModelComponent component = Q.getComponentById(modelEntity,
						getComponentId());
				controller.action(RemoveFromArray.class, modelEntity,
						modelEntity.getComponents(), component);
				((InteractionContext) getParent()).closeEditor();
			}
		});

		header.add(delete).margin(0, 0, WidgetBuilder.dpToPixels(8), 0);
		add(header).expandX();
		buildContent();

		addSpace();
	}

	@Override
	public void prepare() {
		ModelEntity sceneElement = (ModelEntity) controller.getModel()
				.getSelection().getSingle(Selection.SCENE_ELEMENT);
		if (sceneElement != null) {
			T component = (T) Q.getComponentById(sceneElement, componentId);
			if (component == null) {
				component = createComponent(sceneElement);
			}
			try {
				read(component);
			} catch (Exception e) {
				Gdx.app.error(
						"ComponentEditor",
						"Component impossible to read. Replaced with a fresh one",
						e);
				sceneElement.getComponents().removeValue(component, true);
				read(createComponent(sceneElement));
			}
		}
	}

	private T createComponent(ModelEntity sceneElement) {
		T component = buildNewComponent();
		component.setId(componentId);
		controller.action(AddComponent.class, sceneElement, component);
		return component;
	}

	@Override
	public void release() {
	}

	protected abstract void buildContent();

	protected abstract void read(T component);

	protected abstract T buildNewComponent();

	@Override
	public float getPrefWidth() {
		return Math.max(super.getPrefWidth(), WidgetBuilder.dpToPixels(200));
	}

	public String getComponentId() {
		return componentId;
	}

	public String getIcon() {
		return icon;
	}

	public String getTooltip() {
		return null;
	}
}