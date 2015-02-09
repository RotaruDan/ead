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
package es.eucm.ead.editor.control.engine;

import com.badlogic.ashley.core.Component;

import com.badlogic.gdx.utils.ObjectMap;

import es.eucm.ead.editor.assets.EditorGameAssets;
import es.eucm.ead.editor.control.engine.converters.BlinkAnimationConverter;
import es.eucm.ead.editor.control.engine.converters.ComponentConverter;
import es.eucm.ead.editor.control.engine.converters.MoveAnimationConverter;
import es.eucm.ead.engine.ComponentLoader;
import es.eucm.ead.engine.variables.VariablesManager;
import es.eucm.ead.schema.components.ModelComponent;
import es.eucm.ead.schema.editor.components.animations.BlinkAnimation;
import es.eucm.ead.schema.editor.components.animations.MoveAnimation;

public class EditorComponentLoader extends ComponentLoader {

	private ObjectMap<Class, ComponentConverter> converters;

	public EditorComponentLoader(EditorGameAssets gameAssets,
			VariablesManager variablesManager) {
		super(gameAssets, variablesManager);
		converters = new ObjectMap<Class, ComponentConverter>();
		converters.put(MoveAnimation.class, new MoveAnimationConverter());
		converters.put(BlinkAnimation.class, new BlinkAnimationConverter());
	}

	@Override
	public Component toEngineComponent(ModelComponent component) {
		ComponentConverter converter = converters.get(component.getClass());
		if (converter != null) {
			component = converter.convert(component);
		}
		return super.toEngineComponent(component);
	}
}
