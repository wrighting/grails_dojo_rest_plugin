package org.wrighting.grails.plugins.dojorest

import grails.converters.*
import grails.gorm.DetachedCriteria;
import grails.orm.PagedResultList
import groovy.util.logging.Log;

import org.codehaus.groovy.grails.commons.GrailsClassUtils
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap;
import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.springframework.web.servlet.support.RequestContextUtils as RCU

@Log
class DojoRestApiController {
	def list = {
		
		def end = parsePagingOptions(request, params)

		def result = [ success: true ]
		def entity = grailsApplication.getClassForName(params.entity)
		
		parseSortOptions(entity,params)
		
		if (entity) {
			def api = getCustomApi(entity)

			if (api?.list instanceof Closure)
				result.data = api.list(params)
			else
			//result.data = entity.list(params)
			result.data = getFilteredList(entity, params)

			Integer numResults;

			if (result.data instanceof PagedResultList) {
				numResults = result.data.list.size()
				result.count = result.data.getTotalCount()
			} else {
				numResults = result.data.size();
				if (api?.count instanceof Closure)
					result.count = api.count(params)
				else
					result.count = entity.count()
			}
		} else {
			result.success = false
			result.message = "Entity ${params.entity} not found"
		}

		setPagingHeader(end, result, params, response);

		render text: result.data as JSON, contentType: 'application/json', status: result.success ? 200 : 500
	}

	private setPagingHeader(Number end, Map result, Map params, javax.servlet.http.HttpServletResponse response) {
		if (result.count < end) {
			end = result.count;
		}
		def resultsHeader = "items "+params.offset+"-"+end + "/" + result.count;

		response.setHeader("Content-Range", resultsHeader)
	}

	private Number parsePagingOptions(javax.servlet.http.HttpServletRequest request, Map params) {
		def range = request.getHeader("Range")
		def start = 0
		def end = 200
		/*
		 * Range: items=0-
		 * Range: items=0-9
		 */
		if (range != null && range.length() > 0) {
			def r = range.split(/(=| |-)/)
			start = new Integer(r[1])
			if (r.length > 2) {
				end = new Integer(r[2])
			}
		}
		//offset:10, max:20
		params.max = end - start
		params.offset = start
		return end
	}

	private parseSortOptions(entity, Map params) {
		def sortKey = null;
		def sortOrder;

		for ( p in params ) {
			if (p.key.startsWith("sort")) {
				def s = p.key.split (/\(|\)/)
				if (s.size() > 1) {
					sortKey = s[1].substring(1);
//Have to check for - because + is translated into ' '
					if (s[1].startsWith("-")) {
						sortOrder = "desc";
					} else {
						sortOrder = "asc";
					}
				}
			}
		}
		
		if (sortKey != null) {
			def api = getCustomApi(entity)
			
			if (api?.sort instanceof Closure) {
				sortKey = api.sort(sortKey)
			}
			
			params.sort = sortKey;
			params.order = sortOrder;
		}
	}

	def show = {
		def data = retrieveRecord()
		render text: data.result.data as JSON, contentType: 'application/json', status: data.status
	}

	def create = {
		def result = [ success: true ]
		def status = 200
		def entity = grailsApplication.getClassForName(params.entity)
		if (entity) {
			def obj = entity.newInstance()
			obj.properties = request.JSON
			obj.validate()
			if (obj.hasErrors()) {
				status = 500
				result.message = extractErrors(obj).join(";")
				result.success = false
			} else {
				result.data = obj.save(flush: true)
			}
		} else {
			result.success = false
			result.message = "Entity ${params.entity} not found"
			status = 500
		}
		render text: result.data as JSON, contentType: 'application/json', status: status
	}

	def update = {
		def data = retrieveRecord()
		if (data.result.success) {
			data.result.data.properties = request.JSON
			data.result.data.validate()
			if (data.result.data.hasErrors()) {
				data.status = 500
				data.result.message = extractErrors(data.result.data).join(";")
				data.result.success = false
			} else {
				data.result.data = data.result.data.save(flush: true)
			}
		}
		render text: data.result.data as JSON, contentType: 'application/json', status: data.status
	}

	def delete = {
		def data = retrieveRecord()
		try {
			if (data.result.success) {
				data.result.data.delete(flush: true)
			}
		} catch (Exception e) {
			data.result.success = false
			data.result.message = e.message
			data.result.status = 500
		}
		render text: data.result.data as JSON, contentType: 'application/json', status: data.status
	}

	private getCustomApi(clazz) {
		clazz.declaredFields.name.contains('dojo_rest_api') ? clazz.dojo_rest_api : null
	}

	private retrieveRecord() {
		def result = [ success: true ]
		def status = 200
		def entity = grailsApplication.getClassForName(params.entity)
		if (entity) {
			def obj = entity.get(params.id)
			if (obj) {
				result.data = obj
			} else {
				result.success = false
				result.message = "Object with id=${params.id} not found"
				status = 404
			}
		} else {
			result.success = false
			result.message = "Entity ${params.entity} not found"
			status = 500
		}

		[ result: result, status: status ]
	}

	def messageSource

	private extractErrors(model) {
		def locale = RCU.getLocale(request)
		model.errors.fieldErrors.collect { error ->
			messageSource.getMessage(error, locale)
		}
	}

	def range = {
		def entity = grailsApplication.getClassForName(params.entity)
		def c = entity.createCriteria()
		def result = [ success: true ]
		result.data = c {
			and {
				for (p in params) {
					//Only add the filter if there's a field it matches
					entity.metaClass.properties.each {
						if (it.name.equals(p.key)) {
							//Ignore empty
							if (p.value.length() > 0) {
								eq (p.key, p.value.asType(it.type))
							}
						}
					}
				}
			}
			projections {
				min(params.range)
				max(params.range)
			}
		}

		render text: result.data as JSON, contentType: 'application/json', status: result.success ? 200 : 500
	}

	def distinct = {

		def end = parsePagingOptions(request, params)

		def entity = grailsApplication.getClassForName(params.entity)
		parseSortOptions(entity, params)
		def c = entity.createCriteria()
		def result = [ success: true ]
		result.data = c.list(max: params.max, offset: params.offset) {
			and {
				for (p in params) {
					//Only add the filter if there's a field it matches
					entity.metaClass.properties.each {
						if (it.name.startsWith(p.key)) {
							//Ignore empty
							def value = p.value;
							if (value.length() > 0) {
								if (!value.equals("*")) {
									if (value.findIndexOf{ it == '*' } > 0) {
										like (it.name, value.replace('*','%'))
									} else {
										if (it.name.length() == p.key.length()) {
											eq (it.name, p.value.asType(it.type))
										} else if (p.key.length() == it.name.length() + 3 && p.key.endsWith("Min")) {
											gt (it.name, p.value.asType(it.type))
										} else if (p.key.length() == it.name.length() + 3 && p.key.endsWith("Max")) {
											lt (it.name, p.value.asType(it.type))
										}
									}
								}
							}
						}
					}
				}}
			projections { distinct(params.distinct) }
			if (params.sort != null && params.sort.length() > 0) {
				params.sort.split (/,/).each {
					order(it,params.order)
				}
			}
		}
		setPagingHeader(end, result, params, response);
		render text: result.data as JSON, contentType: 'application/json', status: result.success ? 200 : 500
	}

	private addQueryParams(Map params, Class entity) {
		return new DetachedCriteria(entity).build {
			
		}
	}

	private getFilteredList(entity, params) {

		def results;



		def c = entity.createCriteria();
		results = c.list(max: params.max, offset: params.offset) {
			and {
				for (p in params) {
					//Only add the filter if there's a field it matches
					entity.metaClass.properties.each {
						def key = p.key
						
						if (key.equals(it.name)) {
						
							//Ignore empty
							def value = p.value;
							if ((!(value instanceof String)) || value.length() > 0) {
								if ((!(value instanceof String)) || !value.equals("*")) {
									if (value instanceof String && value.findIndexOf{ it == '*' } > 0) {
										like (it.name, value.replace('*','%'))
									} else {
										if (value instanceof String) {
											if (value.startsWith(">")) {
												gt (it.name, value.substring(1).asType(it.type))
											} else if (value.startsWith("<")) {
												lt (it.name, value.substring(1).asType(it.type))
											} else {
												eq (it.name, value.asType(it.type))
											}
										} else {
											eq (it.name, p.value.asType(it.type))
										}
									}
								}
							}
						}
					}
				}
			}
			if (params.sort != null && params.sort.length() > 0) {
				params.sort.split (/,/).each {
					order(it,params.order)
				}
			}
		}

		/*
		 } else {
		 results = entity.list(max:params.max, offset:params.offset)
		 }
		 */
		return results
	}
}
