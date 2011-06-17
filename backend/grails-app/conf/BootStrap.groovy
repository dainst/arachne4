import de.uni_koeln.arachne.dataservice.ArachneEntity;

import grails.util.Environment;

class BootStrap {

    def init = { servletContext ->
		if(Environment.current == Environment.DEVELOPMENT) {
			/*
			def item1 = new ArachneEntity(tableName: "bauwerk", foreignKey: 12398, isDeleted: 0)
			item1.save()
			
			def item2 = new ArachneEntity(tableName: "bauwerk", foreignKey: 12332, isDeleted: 0)
			item2.save()
			
			def item3 = new ArachneEntity(tableName: "bauwerk", foreignKey: 98876, isDeleted: 0)
			item3.save()
			*/
		}
    }
    def destroy = {
    }
}
