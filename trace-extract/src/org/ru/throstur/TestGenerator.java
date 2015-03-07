package org.ru.throstur;

import java.util.ArrayDeque;
import java.util.HashMap;

/**
 * Created by throstur on 3/7/15.
 */
public class TestGenerator {

    private static HashMap<String, String> receive;

    static {
        receive = new HashMap<String, String>();
        receive.put("bus:CREATECONNECTION", "CreateConnection");
    }

    ArrayDeque<String> trace;

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
            .append("override def afterAll() = system.shutdown()")
            .append("\n\n");
        // spec begin

        spec.append("");

        spec.append(traceTest());

        // end of spec
        spec.append("}\n");
        return spec.toString();
    }

    private String traceTest() {

        // TODO: Implement this
        throw new UnsupportedOperationException("Method not implemented.");
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

        for (String line : imports) {
            sb.append("// imports\n");
            sb.append(line);
            sb.append('\n');
        }
        return sb.toString();
    }
}
