package org.agdemo;

import org.apache.commons.text.WordUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class Greeter {
    public String greet(String name) {
        if (!StringUtils.hasText(name)) {
            name = "world";
        }
        return "Hello, " + WordUtils.capitalizeFully(name) + "!";
    }
}
