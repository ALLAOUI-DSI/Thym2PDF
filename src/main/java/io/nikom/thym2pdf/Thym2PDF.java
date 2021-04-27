package io.nikom.thym2pdf;

import com.lowagie.text.DocumentException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class Thym2PDF {
    String template;
    Map<String, Object> variables = new HashMap<>();
    String outputFileName;

    public Thym2PDF(String template, Map<String, Object> variables, String outputFileName) {
        this.template = template;
        this.variables = variables;
        this.outputFileName = outputFileName;
    }

    public Thym2PDF(String template) {
        this.template = template;
    }

    private Thym2PDF() {
    } // To disable the default constructor.

    public ResponseEntity<Resource> download() {
        String html = parseThymeleafTemplate();
        ByteArrayOutputStream out = null;
        try {
            out = generatePdfFromHtml(html);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert out != null;
        return downloadFile(new ByteArrayInputStream(out.toByteArray()));
    }

    private String parseThymeleafTemplate() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setSuffix(".html");
        templateResolver.setPrefix("templates/");
        templateResolver.setTemplateMode(TemplateMode.HTML);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();

        if (this.variables.size() > 0) {
            context.setVariables(variables);
        }

        return templateEngine.process(this.template, context);
    }

    private ByteArrayOutputStream generatePdfFromHtml(String html) throws DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);

        renderer.layout();
        renderer.createPDF(outputStream);
        return outputStream;
    }

    private ResponseEntity<Resource> downloadFile(InputStream inputStream) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + (this.outputFileName == null ? this.template : this.outputFileName) + ".pdf");
        headers.add("Pragma", "no-cache");
        headers.add("Access-Control-Expose-Headers", "Content-Disposition");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        InputStreamResource resource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .headers(headers)
                // .contentLength(resource.le)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
}
