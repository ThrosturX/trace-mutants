import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Queue;
import java.util.LinkedList;

public class JBus extends UntypedActor {

	public interface BusMessage {
		// intentionally left blank
	}

	public final static class CreateConnection implements BusMessage {
		private final int id;

		public CreateConnection(int id) {
			this.id = id;
		}
	}

	public final static class DestroyConnection implements BusMessage {
		private final int id;

		public DestroyConnection(int id) {
			this.id = id;
		}
	}

	public final static class Connect implements BusMessage {
		private final int id;

		public Connect(int id) {
			this.id = id;
		}
	}

	public final static class Disconnect implements BusMessage {
		private final int id;

		public Disconnect(int id) {
			this.id = id;
		}
	}

	public final static class Subscribe implements BusMessage {
		private final int id;

		public Subscribe(int id) {
			this.id = id;
		}
	}

	public final static class SubscribeCallback implements BusMessage {
		private final int id;

		public SubscribeCallback(int id) {
			this.id = id;
		}
	}

	public final static class Unsubscribe implements BusMessage {
		private final int id;

		public Unsubscribe(int id) {
			this.id = id;
		}
	}

	public final static class UnsubscribeCallback implements BusMessage {
		private final int id;

		public UnsubscribeCallback(int id) {
			this.id = id;
		}
	}

	public final static class Publish implements BusMessage {
		private final int id;
		String message;

		public Publish(int id, String message) {
			this.id = id;
			this.message = message;
		}
	}

	public final static class GetMessage implements BusMessage {
		private final int id;

		public GetMessage(int id) {
			this.id = id;
		}
	}

	public final static class Success implements BusMessage {
		@Override
		public boolean equals(Object o) {
			if (o instanceof Success) {
				return true;
			}
			return false;
		}
	}

	public final static class Error implements BusMessage {
		@Override
		public boolean equals(Object o) {
			if (o instanceof Error) {
				return true;
			}
			return false;
		}
	}

//	LoggingAdapter log = Logging.getLogger(getContext().system(), this);

	private boolean connectionObjectExists = false;
	private boolean connected = false;
	private boolean subscribed = false;
	private int     callbacks = 0;
	private int     callbackInvocations = 0; // internal counter
	private Queue<String> messages = new LinkedList<>();

	public void onReceive(Object message) throws Exception {
		if (message instanceof CreateConnection) {
			if (!connectionObjectExists) {
				connectionObjectExists = true;
				getSender().tell(new Success(), getSelf());
			} else {
				getSender().tell(new Error(), getSelf());
			}
		} else if (message instanceof DestroyConnection) {
			if (connectionObjectExists) {
				connectionObjectExists = false;
				connected = false;
				subscribed = false;
				callbacks = 0;
				messages.clear();
				getSender().tell(new Success(), getSelf());
			} else {
				getSender().tell(new Error(), getSelf());
			}
		} else if (message instanceof Connect) {
			if (connectionObjectExists && !connected) {
				connected = true;
				getSender().tell(new Success(), getSelf());
			} else {
				getSender().tell(new Error(), getSelf());
			}
		} else if (message instanceof Disconnect
				&& connected) {
			connected = false;
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof Subscribe
				&& connected && !subscribed) {
			subscribed = true;
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof SubscribeCallback
				&& connected && callbacks< 10) {
			callbacks += 1;
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof Unsubscribe
				&& connected && (subscribed || callbacks > 0)) {
			subscribed = false;
			callbacks -= 1;
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof UnsubscribeCallback
				&& connected && (subscribed || callbacks > 0)) {
			callbacks -=1 ;
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof Publish
				&& connected) {
			/// BEWARE POSSIBLE BUG!
			if (subscribed || callbacks > 0) {
				messages.add(((Publish) message).message);
			}
			getSender().tell(new Success(), getSelf());
		} else if (message instanceof GetMessage
				&& connected && messages.size() > 0) {
			String msg = messages.poll();
			getSender().tell(new Success(), getSelf());
		} else {
//			unhandled(message);
			getSender().tell(new Error(), getSelf());
		}
	}
}
