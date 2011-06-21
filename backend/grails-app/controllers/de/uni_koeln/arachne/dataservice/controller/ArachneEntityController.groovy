package de.uni_koeln.arachne.dataservice.controller


import de.uni_koeln.arachne.dataservice.domain.ArachneEntity;
import grails.converters.JSON

class ArachneEntityController {

	def mysqlService
	
    static allowedMethods = [show: "GET"]

    def show = {
		def renderMap = [:]
		
		printf("params.id = " + params.id);
		
		if(params.id && params.id.contains(":")) {
			def constraintArray = params.id.split(":")
			def resultMap = mysqlService.getData(constraintArray[0], constraintArray[1])
			renderMap = [result : resultMap]
			ArachneEntity arachneEntityInstance =  ArachneEntity.findWhere(tableName: constraintArray[0], foreignKey: constraintArray[1].toInteger())
			
			if(resultMap.size() > 0) {
				if(arachneEntityInstance != null) {
					renderMap["result"]["arachneId"] = arachneEntityInstance.id + ""
				}
			} else {
				renderMap = [:]
				renderMap["error"] = "no item found"
			}
		} else if(params.id && ArachneEntity.exists(params.id)) {
			def arachneEntity = ArachneEntity.get(params.id)
			renderMap = [result : mysqlService.getData(arachneEntity.tableName, arachneEntity.foreignKey+"")]
			renderMap["result"]["arachneId"] = params.id
		} else {
			if(params.id) {
				renderMap = ["error" : "no item found"]
			} else {
				response.sendError(404);
			}
		}
		
		String resp = renderMap as JSON
		if(params.callback) {
			resp = params.callback + "(" + resp + ")"
		}
		render (contentType: "application/json", text: resp)
    }
}
