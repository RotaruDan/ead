package es.eucm.ead.editor.ui.dialogue.node;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.EffectsNode;
import es.eucm.ead.schema.effects.Effect;

public class EffectsNodeWidget extends SimpleNodeWidget<EffectsNode> {

	public EffectsNodeWidget(EffectsNode node, Controller controller,
			Conversation conversation) {
		super(node, controller, conversation);
	}

	@Override
	protected void build(Controller controller, Conversation conversation) {
		Skin skin = controller.getApplicationAssets().getSkin();

		for (Effect effect : node.getEffects()) {
			add(effect.toString());
			add("|");
		}
	}

}
