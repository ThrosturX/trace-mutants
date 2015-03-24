/* 
 * Message Bus implementation (Simple)
 * Supports only a single application (component)
 * 
 */
package org.ru.throstur.bus

import akka.actor._

import scala.collection.mutable

object Main extends App {
	val system = ActorSystem("some-system")
	val actor = system.actorOf(Props[Bus])

	actor ! Bus.CreateConnection(1)
	Thread.sleep(500)
	actor ! Bus.SubscribeCallback(1)
	Thread.sleep(500)
	actor ! Bus.Connect(1)
	Thread.sleep(500)
	actor ! Bus.Subscribe(1)
	Thread.sleep(500)
	actor ! Bus.Publish(1, "Hello, World!")
	Thread.sleep(500)
	actor ! Bus.GetMessage(1)
	Thread.sleep(500)
	actor ! Bus.GetMessage(1)
	Thread.sleep(500)
	actor ! Bus.DestroyConnection(1)
	system.shutdown()
}

object Bus {
	sealed trait BusMessage
	case class CreateConnection(id: Int)     extends BusMessage
	case class DestroyConnection(id: Int)    extends BusMessage
	case class Connect(id: Int)              extends BusMessage
	case class Disconnect(id: Int)           extends BusMessage
	case class Subscribe(id: Int)            extends BusMessage
	case class SubscribeCallback(id: Int)    extends BusMessage
	case class Unsubscribe(id: Int)          extends BusMessage
	case class UnsubscribeCallback(id: Int)  extends BusMessage
	case class Publish(id: Int, msg: String) extends BusMessage
	case class GetMessage(id: Int)           extends BusMessage
	case class Success()         			 extends BusMessage
	case class Error()	         			 extends BusMessage
}

class Bus extends Actor { 
	import Bus._

	var connectionObjectExists = false
	var connected = false
	var subscribed = false
	var callbacks = 0
	val messages = mutable.Queue.empty[String]
	var callbackInvocations = 0 // internal counter

	// TODO: fix callback bug
	// NOTE: actually decide if it needs to be fixed later...
	// INFO: the bug is that currently SubscribeCallback ; UnsubscribeCallback
	//       will give the state `subscribed == true' (incorrect behavior)
	def receive = {
		case CreateConnection(id) =>
			if (!connectionObjectExists) { 
				connectionObjectExists = true
				println("Connection object created by %d".format(id))
				sender() ! Success
			} else {
				println("Failure: Connection object exists!!")
				sender() ! Error
			}
		case DestroyConnection(id) =>
			if (connectionObjectExists) {
				connectionObjectExists = false
				connected = false;
				// side effects: subscriptions/messages are deleted
				subscribed = false
				callbacks = 0
				messages.clear()
				
				println("Connection destroyed")
				sender() ! Success
			} else {
				println("Failure: No connection object!")
				sender() ! Error
			} 
		case Connect(id) =>
			if (connectionObjectExists && !connected) {
				connected = true
				println("Connection established")
				sender() ! Success
			} else {
				var error = {
					if (connected) "Already connected"
					else "No connection object"
				}
				println("Failure: %s".format(error))
				sender() ! Error
			} 
		case Disconnect(id) =>
			if (connected) {
				connected = false
				println("Connection broken")
				sender() ! Success
			} else {
				println("Failure: not connected!")
				sender() ! Error
			}
		case Subscribe(id) =>
			if (connected && !subscribed) {
				subscribed = true
				println("Subscription made")
				sender() ! Success
			} else {
				var error = {
					if (subscribed) "already subscribed"
					else "not connected"
				}
				println("Failure: %s".format(error))
				sender() ! Error
			}
		case SubscribeCallback(id) =>
			if (connected && callbacks < 10) {
//				if (!subscribed) subscribed = true // bug!
				callbacks += 1
				println("Callback subscription made")
				sender() ! Success
			} else {
				var error = {
					if (callbacks == 10) { "Maximum callbacks reached!" }
					else if (!connected) { "Not connected!" }
					else { "LOLCATZ (You shouldn't get this)!" } 
				}
				println("Failure: %s".format(error))
				sender() ! Error
			}
		case Unsubscribe(id) =>
			if (connected && (subscribed || callbacks > 0)) {
				subscribed = false
				callbacks = 0
				println("Subscriptions removed")
				sender() ! Success
			} else {
				println("Failure: Wasn't subscribed.")
				sender() ! Error
			}
		case UnsubscribeCallback(id) =>
			if (connected && (subscribed || callbacks > 0)) {
				callbacks -= 1
				println("Callback subscription removed")
				sender() ! Success
			} else {
				sender() ! Error
				println("Failure: No callbacks found! (or not subscribed)")
			}
		case Publish(id, msg) =>
			if (connected) {
				if (subscribed || callbacks > 0) {
					messages += msg // message only reaches queue if subscribed
				}
				println("Message published to subject %d: %s".format(id, msg))
				sender() ! Success
			} else {
				println("Couldn't publish message %s to %d: Not connected!".format(msg, id))
				sender() ! Error
			}
		case GetMessage(id) =>
			if (connected && messages.length > 0) {
				val msg = messages.dequeue()
				println("Message retrieved: %s [%d]".format(msg, id))
				sender() ! Success
			} else { 
				println("No message to retrieve from subject %d.".format(id))
				sender() ! Error
			}
	}
}
