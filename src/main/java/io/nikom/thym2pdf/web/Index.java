package io.nikom.thym2pdf.web;

import io.nikom.thym2pdf.Thym2PDF;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;


@Controller
public class Index {
    @GetMapping("/generate")
    ResponseEntity<Resource> generate() {
        Thym2PDF generator = new Thym2PDF("receipt", Map.of("header", "Thym2PDF"), "RCPT-002");
//        Thym2PDF generator = new Thym2PDF("receipt");

        return generator.download();
    }

    @GetMapping("/")
    String home(){
        return "receipt";
    }
}
