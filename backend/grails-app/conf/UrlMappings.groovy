class UrlMappings {

	static mappings = {
		"/item/$id?" {
			controller = "arachneEntity"
			action = [GET:"show", PUT:"update", DELETE:"delete", POST:"save"]
		}
		
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
