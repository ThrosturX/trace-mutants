// This is a mutant program.
// Author : ysma

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import java.util.Queue;
import java.util.LinkedList;


public class JBus extends akka.actor.UntypedActor
{

    public interface BusMessage
    {
    }

    public static final class CreateConnection implements JBus.BusMessage
    {

        private final int id;

        public CreateConnection( int id )
        {
            this.id = id;
        }

    }

    public static final class DestroyConnection implements JBus.BusMessage
    {

        private final int id;

        public DestroyConnection( int id )
        {
            this.id = id;
        }

    }

    public static final class Connect implements JBus.BusMessage
    {

        private final int id;

        public Connect( int id )
        {
            this.id = id;
        }

    }

    public static final class Disconnect implements JBus.BusMessage
    {

        private final int id;

        public Disconnect( int id )
        {
            this.id = id;
        }

    }

    public static final class Subscribe implements JBus.BusMessage
    {

        private final int id;

        public Subscribe( int id )
        {
            this.id = id;
        }

    }

    public static final class SubscribeCallback implements JBus.BusMessage
    {

        private final int id;

        public SubscribeCallback( int id )
        {
            this.id = id;
        }

    }

    public static final class Unsubscribe implements JBus.BusMessage
    {

        private final int id;

        public Unsubscribe( int id )
        {
            this.id = id;
        }

    }

    public static final class UnsubscribeCallback implements JBus.BusMessage
    {

        private final int id;

        public UnsubscribeCallback( int id )
        {
            this.id = id;
        }

    }

    public static final class Publish implements JBus.BusMessage
    {

        private final int id;

        java.lang.String message;

        public Publish( int id, java.lang.String message )
        {
            this.id = id;
            this.message = message;
        }

    }

    public static final class GetMessage implements JBus.BusMessage
    {

        private final int id;

        public GetMessage( int id )
        {
            this.id = id;
        }

    }

    public static final class Success implements JBus.BusMessage
    {

        public  boolean equals( java.lang.Object o )
        {
            if (o instanceof JBus.Success) {
                return true;
            }
            return false;
        }

    }

    public static final class Error implements JBus.BusMessage
    {

        public  boolean equals( java.lang.Object o )
        {
            if (o instanceof JBus.Error) {
                return true;
            }
            return false;
        }

    }

    private boolean connectionObjectExists = false;

    private boolean connected = false;

    private boolean subscribed = false;

    private int callbacks = 0;

    private int callbackInvocations = 0;

    private java.util.Queue<String> messages = new java.util.LinkedList<String>();

    public  void onReceive( java.lang.Object message )
        throws java.lang.Exception
    {
        if (message instanceof JBus.CreateConnection) {
            if (!connectionObjectExists) {
                connectionObjectExists = true;
                getSender().tell( new JBus.Success(), getSelf() );
            } else {
                getSender().tell( new JBus.Error(), getSelf() );
            }
        } else {
            if (message instanceof JBus.DestroyConnection) {
                if (connectionObjectExists) {
                    connectionObjectExists = false;
                    connected = false;
                    subscribed = false;
                    callbacks = 0;
                    messages.clear();
                    getSender().tell( new JBus.Success(), getSelf() );
                } else {
                    getSender().tell( new JBus.Error(), getSelf() );
                }
            } else {
                if (message instanceof JBus.Connect) {
                    if (connectionObjectExists && !connected) {
                        connected = true;
                        getSender().tell( new JBus.Success(), getSelf() );
                    } else {
                        getSender().tell( new JBus.Error(), getSelf() );
                    }
                } else {
                    if (message instanceof JBus.Disconnect && connected) {
                        connected = false;
                        getSender().tell( new JBus.Success(), getSelf() );
                    } else {
                        if (message instanceof JBus.Subscribe && connected && !subscribed) {
                            subscribed = true;
                            getSender().tell( new JBus.Success(), getSelf() );
                        } else {
                            if (message instanceof JBus.SubscribeCallback && connected && callbacks < 10) {
                                callbacks += 1;
                                getSender().tell( new JBus.Success(), getSelf() );
                            } else {
                                if (message instanceof JBus.Unsubscribe && connected && (subscribed || callbacks > 0)) {
                                    subscribed = false;
                                    callbacks -= 1;
                                    getSender().tell( new JBus.Success(), getSelf() );
                                } else {
                                    if (message instanceof JBus.UnsubscribeCallback && connected && (subscribed || callbacks > 0)) {
                                        callbacks -= 1;
                                        getSender().tell( new JBus.Success(), getSelf() );
                                    } else {
                                        if (!(message instanceof JBus.Publish && connected)) {
                                            if (subscribed || callbacks > 0) {
                                                messages.add( ((JBus.Publish) message).message );
                                            }
                                            getSender().tell( new JBus.Success(), getSelf() );
                                        } else {
                                            if (message instanceof JBus.GetMessage && connected && messages.size() > 0) {
                                                java.lang.String msg = messages.poll();
                                                getSender().tell( new JBus.Success(), getSelf() );
                                            } else {
                                                getSender().tell( new JBus.Error(), getSelf() );
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
