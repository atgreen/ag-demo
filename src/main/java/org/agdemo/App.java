package org.agdemo;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.SpringVersion;

@Configuration
@ComponentScan
public class App {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx =
                 new AnnotationConfigApplicationContext(App.class)) {
            Greeter greeter = ctx.getBean(Greeter.class);
            String name = args.length > 0 ? String.join(" ", args) : "dependabot demo";
            System.out.println(greeter.greet(name));
            System.out.println("spring-core: " + SpringVersion.getVersion());
        }
    }
}
