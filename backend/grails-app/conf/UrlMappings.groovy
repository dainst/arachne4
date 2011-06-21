class UrlMappings {

	static mappings = {
		// preformatted 
		"/entity/$id?" {
			controller = "arachneEntity"
			action = [GET:"show"]
		}
		
		// raw data
		"/display/entity/$id?" {
			controller = "arachneEntity"
			action = [GET:"show"]
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
