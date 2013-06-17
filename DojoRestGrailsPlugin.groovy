import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.wrighting.grails.plugins.dojorest.JSONApiRegistry
import org.wrighting.grails.plugins.dojorest.DojoRestApiPropertyEditorRegistrar
import org.wrighting.grails.plugins.dojorest.JsonDateEditorRegistrar

class DojoRestGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Ian Wright"
    def authorEmail = "tech@wrighting.org"
    def title = "Dojo JSON RESTful API for GORM"
    def description = '''\\
Heavily based on http://grails.org/plugin/json-rest-api
'''

    // URL to the plugin's documentation
    //def documentation = "http://grails.org/plugin/json-rest-api"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
        dojoRestApiPropertyEditorRegistrar(DojoRestApiPropertyEditorRegistrar, ref("grailsApplication"))
		customPropertyEditorRegistrar(JsonDateEditorRegistrar)
    }

    def doWithDynamicMethods = { ctx ->
        application.domainClasses.each { domainClass ->
            def resource = domainClass.getStaticPropertyValue('dojo_rest_expose', String)
            if (resource) {
                JSONApiRegistry.registry[resource] = domainClass.fullName
            }
        }
    }

    def doWithApplicationContext = { applicationContext ->
        grails.converters.JSON.registerObjectMarshaller(new org.wrighting.grails.plugins.dojorest.JSONDomainMarshaller(application))
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
