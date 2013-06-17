package org.wrighting.grails.plugins.dojorest;

import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

import org.codehaus.groovy.grails.commons.GrailsApplication

public class DojoRestApiPropertyEditorRegistrar implements PropertyEditorRegistrar {
    private final GrailsApplication application

    public DojoRestApiPropertyEditorRegistrar(GrailsApplication application) {
        this.application = application
    }

    public void registerCustomEditors(PropertyEditorRegistry reg) {
        JSONApiRegistry.registry.each { name, className -> 
            Class clazz = application.getClassForName(className)
            reg.registerCustomEditor(clazz, new NumberToDomainInstanceEditor(clazz));
        }
    }
}
