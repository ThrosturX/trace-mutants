package org.ru.throstur;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Created by throstur on 3/7/15.
 */
public class TestGenerator {

    private static HashMap<String, String> receive;
    private static int uid = 0;

    static {
        receive = new HashMap<String, String>();
        receive.put("bus:CREATECONNECTION", "CreateConnection");
    }

    // PRIVATE VARIABLES
    ArrayDeque<String> trace;

    // CONSTRUCTOR
    public TestGenerator(ArrayDeque<String> trace) {
        this.trace = trace;
    }

    public String generateTestCase(String name) {

        if (trace == null) {
            throw new NullPointerException("trace is null!");
        }

        StringBuilder sb = new StringBuilder();

        sb.append(header());
        sb.append(buildSpec(name));

        return sb.toString();
    }

    private String buildSpec(String name) {
        String [] withs = { "ImplicitSender"
                , "FlatSpecLike"
                , "Matchers"
                , "BeforeAndAfterAll"
        };

       StringBuilder spec = new StringBuilder();

        spec.append("class ")
            .append(Character.toUpperCase(name.charAt(0)))
            .append(name.substring(1))
            .append("extends(ActorSystem(\"")
            .append(name.toLowerCase())
            .append("-spec-test\")) ")
            ;

        for (String with : withs) {
            spec.append("with ");
            spec.append(with);
        }

        spec.append("{\n\n")
            .append("\toverride def afterAll() = system.shutdown()")
            .append("\n\n");
        // spec begin

        spec.append("");

        spec.append(traceTest());

        // end of spec
        spec.append("\n}\n");
        return spec.toString();
    }

    private String traceTest() {
        int uid = generateTestUID();
        StringBuilder sb = new StringBuilder();

        sb.append("\t\"SUT\" should \"AUTO-GEN-TEST (");
        sb.append(uid);
        sb.append(")\"  in {\n");
        sb.append("\n\tval actorRef = TestActorRef[Bus]\n");

        sb.append(extractTrace(uid));

        sb.append("\n");
        sb.append("\t}");

        return sb.toString();
    }

    private String extractTrace(int id) {
        StringBuilder sb = new StringBuilder();

        for (String msg : trace) {
          sb.append(doReceive(msg, id));
        }



        return sb.toString();
    }

    private static String doReceive(String key, int uid) {
        String value = receive.get(key);

        if (value == null) {
          return "//\tnull value in key: " + key + "\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("\tactorRef.receive(Bus.");
        sb.append(receive.get(key));
        sb.append("(");
        sb.append(uid);
        sb.append("))\n");
        return sb.toString();
    }

    private static String header() {
        String [] imports = { "collection.mutable.Stack"
                , "scala.concurrent.duration._"
                , "org.scalatest._"
                , "akka.actor._"
                , "akka.testkit._"
                , "com.typesafe.config._"
                , "org.ru.throstur.bus._"
        };

        StringBuilder sb = new StringBuilder();

        sb.append("// imports\n");
        for (String line : imports) {
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }

    private synchronized static int generateTestUID() {
        return uid++;
    }

  /**
   * Unit test the TestGenerator
   * @param args no args
   */
    public static void main(String[] args) {
        ArrayDeque<String> traces = new ArrayDeque<String>();
        String _trace = "cat:INITIAL-dog:INITIAL-dog:MEOW-cat:IDLE-cat:WOOF-";
        for (String s : _trace.split("-")) {
          traces.add(s);
        }
        receive = new HashMap<String, String>();
        receive.put("cat:WOOF", "DestroyConnection");
        receive.put("dog:MEOW", "CreateConnection");

        TestGenerator tg = new TestGenerator(traces);

        String spec = tg.generateTestCase("spec1");
        System.out.println(spec);
    }
}
