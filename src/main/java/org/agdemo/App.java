package org.agdemo;

import org.apache.commons.text.WordUtils;
import org.springframework.core.SpringVersion;
import org.springframework.util.StringUtils;

public class App {
    public static void main(String[] args) {
        String name = args.length > 0 ? String.join(" ", args) : "dependabot demo";
        if (!StringUtils.hasText(name)) {
            name = "dependabot demo";
        }
        System.out.println(WordUtils.capitalizeFully(name));
        System.out.println("spring-core: " + SpringVersion.getVersion());
    }
}
