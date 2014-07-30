package es.eucm.ead.editor.ui.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

import es.eucm.ead.editor.control.Controller;
import es.eucm.ead.editor.control.Selection;
import es.eucm.ead.editor.control.actions.model.SetSelection;
import es.eucm.ead.editor.model.Model.SelectionListener;
import es.eucm.ead.editor.model.events.SelectionEvent;
import es.eucm.ead.editor.view.widgets.AbstractWidget;
import es.eucm.ead.editor.view.widgets.dragndrop.DraggableLinearLayout.DropListEvent;
import es.eucm.ead.editor.view.widgets.dragndrop.DraggableScrollPane;
import es.eucm.ead.schema.components.conversation.Conversation;
import es.eucm.ead.schema.components.conversation.ForkNode;
import es.eucm.ead.schema.components.conversation.SimpleNode;

public class ConversationsGraph extends DraggableScrollPane {

	private static final Vector2 TEMP = new Vector2();

	private Array<Node> rootNodes;
	private AbstractWidget group;

	private Target target;
	private Controller controller;
	private ClickListener nodeClicked;
	private ShapeRenderer shapeRenderer;

	public ConversationsGraph(Controller control) {
		super(null);
		this.controller = control;
		shapeRenderer = controller.getShapeRenderer();
		group = new AbstractWidget() {

			@Override
			public float getPrefWidth() {
				float prefW = 0f;
				for (Actor actor : getChildren()) {
					prefW = Math.max(prefW, actor.getWidth() + actor.getX());
				}
				return prefW;
			}

			@Override
			public float getPrefHeight() {
				float prefH = 0f;
				for (Actor actor : getChildren()) {
					prefH = Math.max(prefH, actor.getHeight() + actor.getY());
				}
				return prefH;
			}

			@Override
			protected void drawChildren(Batch batch, float parentAlpha) {
				super.drawChildren(batch, parentAlpha);
				if (rootNodes.size != 0) {
					batch.end();
					shapeRenderer.setProjectionMatrix(batch
							.getProjectionMatrix());
					shapeRenderer
							.setTransformMatrix(batch.getTransformMatrix());
					shapeRenderer.begin(ShapeType.Line);
					shapeRenderer.setColor(Color.BLACK);
					for (Node node : rootNodes) {
						drawLines(node, shapeRenderer);
					}
					shapeRenderer.end();
					batch.begin();
				}
			}

			private void drawLines(Node rootNode, ShapeRenderer renderer) {
				Actor actor = rootNode.getActor();
				float x = 0;
				float y = 0;
				if (actor != null) {
					x = actor.getX();
					y = actor.getY();
				}
				for (Node node : rootNode.getChildren()) {
					Actor childActor = node.getActor();
					if (childActor != null && actor != null) {

						float x1, y1;
						x1 = childActor.getX();
						y1 = childActor.getY();
						float xAux = x1;
						float yAux = y1;
						TEMP.set(x, y).sub(xAux, yAux).nor()
								.set(-TEMP.y, TEMP.x).scl(10f);

						renderer.triangle(x1, y1, xAux + TEMP.x, yAux + TEMP.y,
								xAux - TEMP.x, yAux - TEMP.y);
						renderer.line(x, y, x1, y1);
					}
					drawLines(node, renderer);
				}
			}
		};
		setWidget(group);
		rootNodes = new Array<Node>();
		addTarget(target = newTarget());
		controller.getModel().addSelectionListener(new SelectionListener() {
			private Array<Integer> visited = new Array<Integer>();

			@Override
			public void modelChanged(SelectionEvent event) {
				if (event.getType() == SelectionEvent.Type.FOCUSED) {
					Object object = event.getSelection()[0];
					if (object instanceof Conversation) {
						Conversation conversation = (Conversation) object;
						visited.clear();
						clear();
						prepareGraph(conversation, rootNodes, conversation
								.getNodes().first(), visited);
					}
				}
			}

			@Override
			public boolean listenToContext(String contextId) {
				return Selection.CONVERSATION.equals(contextId);
			}
		});

		nodeClicked = new ClickListener() {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				controller.action(SetSelection.class, Selection.CONVERSATION,
						Selection.NODE,
						((GraphNodeWidget) event.getListenerActor()).node);
			}
		};
	}

	private void prepareGraph(final Conversation conversation,
			Array<Node> array,
			final es.eucm.ead.schema.components.conversation.Node node,
			Array<Integer> visited) {
		Skin skin = controller.getApplicationAssets().getSkin();

		GraphNodeWidget nodeButton = new GraphNodeWidget(node, skin);
		nodeButton.addListener(nodeClicked);

		Node nodeWidget = new Node(nodeButton);
		array.add(nodeWidget);
		group.addActor(nodeButton);
		addSource(newSource(nodeButton));
		if (node instanceof SimpleNode) {
			Integer nextId = ((SimpleNode) node).getNextNodeId();
			if (!visited.contains(nextId, true)) {
				visited.add(nextId);
				prepareGraph(conversation, nodeWidget.getChildren(),
						conversation.getNodes().get(nextId), visited);
			}
		} else if (node instanceof ForkNode) {
			ForkNode forkNode = (ForkNode) node;

			for (Integer id : forkNode.getNextNodeIds()) {
				if (!visited.contains(id, true)) {
					visited.add(id);
					prepareGraph(conversation, nodeWidget.getChildren(),
							conversation.getNodes().get(id), visited);
				}
			}

		}
	}

	@Override
	public void clearChildren() {
		super.clearChildren();
		addTarget(target);
		group.clear();
		clearNodes();
	}

	public Array<Node> getRootNodes() {
		return rootNodes;
	}

	private Node getNodeFromActor(Actor actor) {
		return getNodeFromActor(actor, rootNodes);
	}

	private Node getNodeFromActor(Actor actor, Array<Node> nodes) {
		for (Node node : nodes) {
			if (node.actor == actor) {
				return node;
			}
			Node ret = getNodeFromActor(actor, node.getChildren());
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

	private void clearNodes() {
		clearNodes(rootNodes);
	}

	private void clearNodes(Array<Node> nodes) {
		for (Node node : nodes) {
			clearNodes(node.getChildren());
		}
		nodes.clear();
	}

	private static class Node {
		private Actor actor;
		private final Array<Node> children = new Array<Node>(0);

		public Node(Actor actor) {
			if (actor == null)
				throw new IllegalArgumentException("actor cannot be null.");
			this.actor = actor;
		}

		public Actor getActor() {
			return actor;
		}

		public Array<Node> getChildren() {
			return children;
		}
	}

	public Source newSource(Actor widget) {
		return new Source(widget) {

			@Override
			public Payload dragStart(InputEvent event, float x, float y,
					int pointer) {
				// Necessary to be able to drag and drop
				setCancelTouchFocus(false);

				// Necessary to stop the scroll from moving while dragging
				cancel();

				// Set the actor displayed while dragging
				Actor actor = getActor();
				Payload payload = new Payload();
				payload.setDragActor(actor);
				Node node = getNodeFromActor(actor);
				payload.setObject(node);
				node.actor = null;
				return payload;
			}

			@Override
			public void dragStop(InputEvent event, float x, float y,
					int pointer, Payload payload, Target target) {
				// Return the ScrollPane to its original state
				setCancelTouchFocus(true);

				if (target == null) {
					// The pay load was not dropped over a target, thus put it
					// back to where it came from.
					Actor actor = getActor();
					((Node) payload.getObject()).actor = actor;
					group.addActor(actor);
				}
			}
		};
	}

	private Target newTarget() {
		return new Target(group) {

			@Override
			public boolean drag(Source source, Payload payload, float x,
					float y, int pointer) {
				return true;
			}

			@Override
			public void drop(Source source, Payload payload, float x, float y,
					int pointer) {

				Actor dropActor = payload.getDragActor();
				dropActor.localToStageCoordinates(TEMP.set(dropActor.getX(),
						dropActor.getY()));
				group.stageToLocalCoordinates(TEMP);
				group.setPosition(dropActor, TEMP.x, TEMP.y);
				((Node) payload.getObject()).actor = dropActor;
				group.addActor(dropActor);
			}

			/**
			 * Fires that some actor has been dropped
			 */
			private void fireDrop(Actor actor) {
				DropListEvent dropEvent = Pools.obtain(DropListEvent.class);

				fire(dropEvent);
				Pools.free(dropEvent);
			}
		};
	}

	private static class GraphNodeWidget extends TextButton {

		private es.eucm.ead.schema.components.conversation.Node node;

		public GraphNodeWidget(
				es.eucm.ead.schema.components.conversation.Node node, Skin skin) {
			super(node.getId() + "", skin);
			this.node = node;
		}

	}
}
