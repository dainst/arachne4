package de.uni_koeln.arachne.dataservice.controller


import de.uni_koeln.arachne.dataservice.domain.ArachneEntity;
import grails.converters.JSON


/**
 * 
 * Controller class handling http <code>GET</code> requests to the URLs <code>/display/entity/$id</code> and <code>/entity/$id</code>.
 * Requests to <code>/display/entity/$id</code> return preformatted response objects and requests to <code>/entity/$id</code>
 * return raw data objects.
 * <br>
 * The response objects are created by looking up the <code>$id</code> in the <code>arachneentityidentification</code> table.
 * <p>
 * The preformatted response objects are translated into the language given as the <code>lang GET</code> parameter. The default language is
 *  <code>de:DE</code>.
 * <br>
 * The raw data output format is choosen by the given <code>format GET</code> parameter. The default format is <code>application/json</code>.
 */
class ArachneEntityController {

	/**
	 * Autowired data service accessing the <code>arachneentityidentification</code> table via ORM, this means using the ArachneEntity domain
	 * class.
	 */
	def mysqlService;
	
	/**
	 * Allowed http request methods. This controller only supports <code>GET</code> requests.
	 */
    static allowedMethods = [show: "GET"];

	/**
	 * Http request handler taking a <code>lang</code> respectively a <code>format</code> parameter.
	 * @return A preformatted data object in the specified language or a raw data object in the specified format or a 404 error
	 * if no id is given in the URL. 
	 */
    def show = {
		def renderMap = [:];
		
		// debug information
		printf("params.id = " + params.id);
		
		if (params.id && params.id.contains(":")) {
			def constraintArray = params.id.split(":");
			def resultMap = mysqlService.getData(constraintArray[0], constraintArray[1]);
			renderMap = [result : resultMap];
			ArachneEntity arachneEntityInstance =  ArachneEntity.findWhere(tableName: constraintArray[0]
				, foreignKey: constraintArray[1].toInteger());
			
			if (resultMap.size() > 0) {
				if (arachneEntityInstance != null) {
					renderMap["result"]["arachneId"] = arachneEntityInstance.id + "";
				}
			} else {
				renderMap = [:];
				renderMap["error"] = "no item found";
			}
		} else if (params.id && ArachneEntity.exists(params.id)) {
			def arachneEntity = ArachneEntity.get(params.id);
			renderMap = [result : mysqlService.getData(arachneEntity.tableName, arachneEntity.foreignKey + "")];
			renderMap["result"]["arachneId"] = params.id;
		} else {
			if (params.id) {
				renderMap = ["error" : "no item found"];
			} else {
				response.sendError(404);
			}
		}
		
		String resp = renderMap as JSON;
		if (params.callback) {
			resp = params.callback + "(" + resp + ")"
		}
		render(contentType: "application/json", text: resp)
    }
}