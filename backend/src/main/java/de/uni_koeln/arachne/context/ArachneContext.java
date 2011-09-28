package de.uni_koeln.arachne.context;

import java.util.List;

import de.uni_koeln.arachne.response.ArachneDataset;
import de.uni_koeln.arachne.response.Link;

public class ArachneContext {
	// A Specified Context Type like Ort, Literatur, Literaturzitat.
	private String ContextName;

	//The Parent dataset
	private ArachneDataset Partent;

	//An Enumeration which Represents the State of the Context
	private enum CompletionVersionEnum {
		FULL, LIMITED, FIRST, EMPTY
	};
	
	private CompletionVersionEnum completionVersion;
		
	//A Link Point or equal maybe Static
	/*
	@Autowired
	private ContextService CS;
	*/
	
	//Depth of The Context
	private int depthLevel;

	//List of Links
	List<Link> contextEntities;

	//The Context Getter
	public List<Link> getallContexts() {
	if (completionVersion != CompletionVersionEnum.FULL)
	    doCompleteRetrival();
		return contextEntities;
	}

	public Link getFirstContext() {
	if (completionVersion != CompletionVersionEnum.EMPTY)
	    doFirstRetrival();
		return contextEntities.get(0);
	}

	public List<Link> getLimitContext(int howMany) {
	if (completionVersion != CompletionVersionEnum.LIMITED && completionVersion != CompletionVersionEnum.FULL)
	    doLimitRetrival(howMany);
	    if (howMany > contextEntities.size())
	        completionVersion = CompletionVersionEnum.FULL;
	    else
	        completionVersion = CompletionVersionEnum.LIMITED;

	    return contextEntities;
	}
	
	//The Retrival Functions all Call the General Fuction
	protected void doCompleteRetrival() {
	    doRetrival(contextEntities.size(), -1);
	    completionVersion = CompletionVersionEnum.FULL;
	}

	protected void doFirstRetrival() {
	    doRetrival(0, 1);
	    completionVersion = CompletionVersionEnum.FIRST;
	}

	protected void doLimitRetrival(int howMany) {
	    doRetrival(contextEntities.size(), howMany - contextEntities.size());
	    completionVersion = CompletionVersionEnum.LIMITED;
	}

	//Do Retrival Calls the Context Service with the needed Information
	protected void doRetrival(int offset, int Limit) {
	    //List<Link> temporary = CS.getLinks(parent, contextName, offset, limit);
	    //contextEntities.appendAll(temporary);
	}
}