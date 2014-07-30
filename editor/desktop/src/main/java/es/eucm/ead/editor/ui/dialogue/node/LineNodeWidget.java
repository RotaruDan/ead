package es.eucm.ead.editor.ui.dialogue.node;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.LineNode;

public class LineNodeWidget extends SimpleNodeWidget<LineNode> {

	public LineNodeWidget(LineNode node, Controller controller,
			Conversation conversation) {
		super(node, controller, conversation);
	}

	@Override
	protected void build(Controller controller, Conversation conversation) {

		Skin skin = controller.getApplicationAssets().getSkin();

		TextField speaker = new TextField(conversation.getSpeakers().get(
				node.getSpeaker()), skin);
		TextArea line = new TextArea(node.getLine(), skin);
		line.setPrefRows(2);

		add(new Label("Speaker:", skin)).left();
		row();
		add(speaker).expandX();
		row();
		add(new Label("Text:", skin)).left();
		row();
		add(line).expand().fill();
	}

}
