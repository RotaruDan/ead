package es.eucm.ead.editor.ui.dialogue.node;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.engine.gdx.Spinner;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.WaitNode;

public class WaitNodeWidget extends SimpleNodeWidget<WaitNode> {

	private Spinner spinner;

	public WaitNodeWidget(WaitNode node, Controller controller,
			Conversation conversation) {
		super(node, controller, conversation);
	}

	@Override
	protected void build(Controller controller, Conversation conversation) {

		Skin skin = controller.getApplicationAssets().getSkin();

		add(spinner = new Spinner(skin, .5f));
		spinner.setValue(node.getTime());
	}

	
	public void setTime(float time){
		spinner.setValue(time);
	}
}
