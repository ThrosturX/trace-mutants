package org.ru.throstur;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by throstur on 3/7/15.
 */
public class TestGenerator {

  private static HashMap<String, String> receive;
  private static HashMap<String, String> expect;
  private static int uid = 0;

  static {
    receive = new HashMap<String, String>();
    receive.put("bus:CREATECONNECTION", "CreateConnection");
    receive.put("bus:DESTROYCONNECTION", "DestroyConnection");
    receive.put("bus:CONNECT", "Connect");
    receive.put("bus:DISCONNECT", "Disconnect");
    receive.put("bus:PUBLISH", "PublishSSS");
    receive.put("bus:SUBSCRIBE", "Subscribe");
    receive.put("bus:SUBSCRIBECALLBACK", "SubscribeCallback");
    receive.put("bus:UNSUBSCRIBE", "Unsubscribe");
    receive.put("bus:UNSUBSCRIBECALLBACK", "UnsubscribeCallback");
    receive.put("bus:GETMESSAGE", "GetMessage");

    expect = new HashMap<String, String>();
    expect.put("att:ACK", "Success");
    expect.put("att:FAIL", "Error");
  }

  // PRIVATE VARIABLES
  List<ArrayDeque<String>> traces;

  // CONSTRUCTOR
  public TestGenerator(ArrayDeque<String> trace) {
    traces = new ArrayList<ArrayDeque<String>>();
    traces.add(trace);
  }

  public TestGenerator(ArrayList<ArrayDeque<String>> traces) {
    this.traces = traces;
  }


  public boolean addTrace(ArrayDeque<String> trace) {
    if (traces.contains(trace)) return false;
    traces.add(trace);
    return true;
  }

  public String generateTestCase(String name) {

    if (traces.isEmpty()) {
      throw new RuntimeException("no traces found!");
    }

    StringBuilder sb = new StringBuilder();

    sb.append(header());
    sb.append(buildSpec(name));

    return sb.toString();
  }

  private String buildSpec(String name) {
    String[] withs = {"ImplicitSender"
        , "FlatSpecLike"
        , "Matchers"
        , "BeforeAndAfterAll"
    };

    StringBuilder spec = new StringBuilder();

    spec.append("class ")
        .append(Character.toUpperCase(name.charAt(0)))
        .append(name.substring(1))
        .append(" extends TestKit(ActorSystem(\"")
        .append(name.toLowerCase())
        .append("-spec-test\")) ")
    ;

    for (String with : withs) {
      spec.append("with ");
      spec.append(with);
      spec.append(' ');
    }

    spec.append("{\n\n")
        .append("\toverride def afterAll() = system.shutdown()")
        .append("\n\n");
    // spec begin

    spec.append("");

    spec.append(allTraceTest());

    // end of spec
    spec.append("\n}\n");
    return spec.toString();
  }

  private String allTraceTest() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < traces.size(); ++i) {
      sb.append(traceTest(i));
    }

    return sb.toString();
  }

  private String traceTest(int uid) {
//  int uid = generateTestUID();
    StringBuilder sb = new StringBuilder();

    sb.append("\n\t\"The System-under-test (");
    sb.append(uid);
    sb.append(")\" should \"AUTO-GEN-TEST (");
    sb.append(uid);
    sb.append(")\" in {\n");
    sb.append("\t\tval actorRef = TestActorRef[Bus]\n\n");

    sb.append(extractTrace(uid));

    sb.append("\n\t}\n");

    return sb.toString();
  }

  private String extractTrace(int id) {
    StringBuilder sb = new StringBuilder();
    boolean started = false;

    ArrayDeque<String> outgoing = new ArrayDeque<String>();
    ArrayDeque<String> incoming = new ArrayDeque<String>();

    for (String msg : traces.get(id)) {
      String sausage = process(msg, id);
      if (!started && sausage.contains("!"))
        started = true;
      else if (!started && sausage.contains("expect"))
        continue;

      sb.append(sausage);

      if (sausage.contains("!")) {
        outgoing.add(sausage);
      } else if (sausage.contains("expect")) {
        incoming.add(sausage);
      }
    }

/// Strict input pair matching
//  sb = new StringBuilder();

//  for (int i=0; i<outgoing.size(); ++i) {
//    if (i >= incoming.size()) {
//      System.err.println("WARNING 1: outgoing/incoming mismatch " + outgoing.size() + " - " + incoming.size());
//      break;
//    }
//    sb.append(outgoing.poll());
//    sb.append(incoming.poll());
//  }

    return sb.toString();
  }

  private static String process(String message, int uid) {
    String value = receive.get(message);
    if (value != null) {
      return doReceive(value, uid);
    }

    value = expect.get(message);
    if (value != null) {
      return "\t\texpectMsg(Bus." + value + ")\n";
    }

    return "";
//      return "//\t process(): Nothing found in {receive, expect} about \"" + message +"\"\n";
  }

  private static String doReceive(String message, int param) {
    StringBuilder sb = new StringBuilder();
    sb.append("\t\tactorRef ! (Bus.");
    sb.append(message.replace("SSS", ""));
    sb.append("(");
    sb.append(param);
    if (message.endsWith("SSS")) {
      sb.append(", \"foo\"");
    }
    sb.append("))\n");
    return sb.toString();
  }

  private static String header() {
    String[] imports = {
        "scala.concurrent.duration._"
        , "org.scalatest._"
        , "akka.actor._"
        , "akka.testkit._"
        , "com.typesafe.config._"
        , "org.ru.throstur.bus._"
    };

    StringBuilder sb = new StringBuilder();

    sb.append("// imports\n");
    for (String line : imports) {
      sb.append("import ");
      sb.append(line);
      sb.append('\n');
    }
    sb.append('\n');
    return sb.toString();
  }

  private synchronized static int generateTestUID() {
    return uid++;
  }

  /**
   * Unit test the TestGenerator
   *
   * @param args input trace file
   */
  public static void main(String[] args) {
    ArrayList<ArrayDeque<String>> traces = new ArrayList<ArrayDeque<String>>();
    if (args.length == 0) {
      String [] _traces = {
          "cat:INITIAL-dog:INITIAL-dog:MEOW-cat:IDLE-cat:WOOF-"
          , "cat:INITIAL-cat:IDLE-cat:WOOF-dog:MEOW-"
      };

      for (String _trace : _traces) {
        ArrayDeque<String> t = new ArrayDeque<String>();
        for (String s : _trace.split("-")) {
          t.add(s);
        }
        traces.add(t);
      }
      receive = new HashMap<String, String>();
      receive.put("cat:WOOF", "DestroyConnection");
      receive.put("dog:MEOW", "CreateConnection");
    } else {
      FileInputStream fstream;
      try {
        boolean found = false;
        fstream = new FileInputStream(args[0]);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
        String line = "";
        line = br.readLine();
        while (line != null)
        {
          ArrayDeque<String> trace = new ArrayDeque<String>();
          for (String msg : line.split("-")) {
            if (msg.contains("bus")) {
              found = true;
            }
            if (found)
              trace.add(msg);
          }
          line = br.readLine();
          traces.add(trace);
        }
      } catch (Exception ex) {
        System.err.println(ex.getMessage());
        ex.printStackTrace();
        return;
      }
    }
    TestGenerator tg = new TestGenerator(traces);

    String spec = tg.generateTestCase("spec1");
    System.out.println(spec);
  }
}
