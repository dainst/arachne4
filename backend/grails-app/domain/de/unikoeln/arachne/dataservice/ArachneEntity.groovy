package de.unikoeln.arachne.dataservice

class ArachneEntity {
	
	int id
	String tableName
	int foreignKey
	int isDeleted

	
	static mapping = {
		table 'arachneentityidentification'
		id column: 'ArachneEntityID'
		foreignKey column: 'ForeignKey'
		isDeleted column: 'isDeleted'
		tableName column: 'TableName'
		version false
	}
	
	
    static constraints = {
    }
}
