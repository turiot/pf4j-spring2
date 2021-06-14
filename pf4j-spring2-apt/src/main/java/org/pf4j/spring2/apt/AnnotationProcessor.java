package org.pf4j.spring2.apt;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import org.pf4j.spring2.SpringPluginManager;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

@SupportedAnnotationTypes({
    "org.springframework.stereotype.Component",
    "org.springframework.stereotype.Controller",
    "org.springframework.stereotype.Repository",
    "org.springframework.stereotype.Service"})
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) return false;

        var services = new ArrayList<String>(10);

        for (var e : roundEnv.getElementsAnnotatedWithAny(Set.of(Component.class, Controller.class, Repository.class, Service.class))) {
            if (!e.getKind().isClass() && !e.getKind().isInterface()) continue;
            var type = (TypeElement) e;
            processingEnv.getMessager().printMessage(Kind.WARNING, "found " + type.getSimpleName());
            services.add(type.getQualifiedName().toString());
        }
        if (services.isEmpty()) return false;

        processingEnv.getMessager().printMessage(Kind.WARNING, "Writing " + SpringPluginManager.SPRING_IDX);
        var filer = processingEnv.getFiler();
        try (
                var pw = new PrintWriter(new OutputStreamWriter(filer.createResource(StandardLocation.CLASS_OUTPUT, "", SpringPluginManager.SPRING_IDX).openOutputStream(), StandardCharsets.UTF_8))
            ) {
                services.forEach(pw::println);
        }
        catch (IOException x) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to write service definition files: " + x);
        }

        return false;
    }

}
