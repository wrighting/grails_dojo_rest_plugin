import org.wrighting.grails.plugins.dojorest.JSONApiRegistry

class DojoRestApiUrlMappings {

	static mappings = {
		applicationContext ->
		def config = applicationContext.grailsApplication.config.dojoRest
		def root = config?.contextRoot ? config.contextRoot : '/api'
		
		"${root}/$domain" (controller: 'dojoRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'list', POST: 'create' ]
		}

		"${root}/$domain/$id" (controller: 'dojoRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'show', PUT: 'update', DELETE: 'delete' ]
		}
		
		"${root}/$domain/range/$range" (controller: 'dojoRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'range' ]
		}
		
		"${root}/$domain/distinct/$distinct" (controller: 'dojoRestApi') {
			entity = { JSONApiRegistry.registry[params.domain] }
			action = [ GET: 'distinct' ]
		}
	}

}
