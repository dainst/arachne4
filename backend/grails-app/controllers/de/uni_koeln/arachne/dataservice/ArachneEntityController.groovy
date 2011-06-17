package de.uni_koeln.arachne.dataservice


import de.uni_koeln.arachne.dataservice.ArachneEntity;
import grails.converters.JSON

class ArachneEntityController {

	def mysqlService
	
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [arachneEntityInstanceList: ArachneEntity.list(params), arachneEntityInstanceTotal: ArachneEntity.count()]
    }

    def create = {
		/*
        def arachneEntityInstance = new ArachneEntity()
        arachneEntityInstance.properties = params
        return [arachneEntityInstance: arachneEntityInstance]
        */
    }

    def save = {
		/*
        def arachneEntityInstance = new ArachneEntity(params)
        if (arachneEntityInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), arachneEntityInstance.id])}"
            redirect(action: "show", id: arachneEntityInstance.id)
        }
        else {
            render(view: "create", model: [arachneEntityInstance: arachneEntityInstance])
        }
        */
    }

    def show = {
		def renderMap = [:]
		
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
			renderMap = ["error" : "no item found"]
		}
		
		String resp = renderMap as JSON
		if(params.callback) {
			resp = params.callback + "(" + resp + ")"
		}
		render (contentType: "application/json", text: resp)
    }

    def edit = {
        def arachneEntityInstance = ArachneEntity.get(params.id)
        if (!arachneEntityInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [arachneEntityInstance: arachneEntityInstance]
        }
    }

    def update = {
        def arachneEntityInstance = ArachneEntity.get(params.id)
        if (arachneEntityInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (arachneEntityInstance.version > version) {
                    
                    arachneEntityInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'arachneEntity.label', default: 'ArachneEntity')] as Object[], "Another user has updated this ArachneEntity while you were editing")
                    render(view: "edit", model: [arachneEntityInstance: arachneEntityInstance])
                    return
                }
            }
            arachneEntityInstance.properties = params
            if (!arachneEntityInstance.hasErrors() && arachneEntityInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), arachneEntityInstance.id])}"
                redirect(action: "show", id: arachneEntityInstance.id)
            }
            else {
                render(view: "edit", model: [arachneEntityInstance: arachneEntityInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def arachneEntityInstance = ArachneEntity.get(params.id)
        if (arachneEntityInstance) {
            try {
                arachneEntityInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'arachneEntity.label', default: 'ArachneEntity'), params.id])}"
            redirect(action: "list")
        }
    }
}
