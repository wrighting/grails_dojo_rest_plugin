grails_dojo_rest_plugin
=======================

This is heavily based on http://grails.org/plugin/json-rest-api

To some extent this is an exercise in creating a grails plugin although I believe that it does work.

It may be possible to use another plugin or approach e.g. https://github.com/krasserm/grails-jaxrs however I haven't experimented.

I would expect the main limitation of a standard REST/JSON service to be the use of HTTP Headers for paging that Dojo JsonRest uses

Configuration
=============

See the json-rest-api docs

Instead of:
static expose = 'person'
use
static dojo_rest_expose = 'person'

Instead of:
static api = []
use
static dojo_rest_api = []

In the application Config.groovy change the root like this:
dojoRest {
	contextRoot = "/api"
}

Differences
===========
Dojo JsonRest is supported - this includes:
Revised response JSON format
Support for parameters for filtering and sorting
Support for pagination by means of setting response headers
