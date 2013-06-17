package org.wrighting.grails.plugins.dojorest;
import grails.converters.JSON
//http://jurelillo.wordpress.com/2012/04/15/grails-convert-a-generic-projection-resulset-in-a-json-list-of-maps/
class JsHandlerService {

    /**
    Achieve a list with all the values from a Entity's field
    Luis Torres. EBD-CSIC
    */       
    def listadoColumna={field,entity->

        def lista=entity.withCriteria{
            projections{
                distinct(field)
            }
            and
            //Hay que eliminar los nulos de la lista pq falla el autocomplete de jquery
            {isNotNull(field)}
            order(field,"asc")
        }
        def listaJSON=(lista as JSON).toString()
        return listaJSON
    }

    /**
    Achieve a list of list from all values from a Entity projection 
    defined in a list of fields
    Luis Torres. EBD-CSIC
    */       
    def listadoTuplas={fields,entity->

        def lista=entity.withCriteria{
            projections{
                fields.each{field->                  
                    property(field)                               
//                    and
//                    {isNotNull(field)}
                }               
            }
            order(fields[0],"asc")
        }   
        //Eliminate the duplicate entries of resultSet
        def lista2=lista.unique()

        return listOfMaps2JSON(lista2,fields)
    }

    /**
    Function for iterate over a list of list and convert It to a list of maps
    Luis Torres. EBD-CSIC
    */   
    def listOfMaps2JSON(List lista,List fieldTitles){

        def tupla=[]
        lista.each(){row->
            //For each row, It define a map object
            def mapa=[:]
            row.eachWithIndex(){item,i->
                //For each item of the row, It add a value into the map object
                mapa.put(fieldTitles[i],item)                   
            }
            //Once is finished the items addition into th map, He is added to tupla list like as item
            tupla.add(mapa)
        }
        def listaJSON=tupla.encodeAsJSON()
        return listaJSON
    }  
}