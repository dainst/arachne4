

import groovy.sql.Sql
import javax.sql.DataSource


/**
 * Dataservice class wrapping MySQL transactions.
 */
class MysqlService {

    static transactional = true

	def dataSource
		
    def serviceMethod() {

    }
	
	def Map getData(String tableName, String objectId) {
		def sqlString = "SELECT * FROM " + tableName + " WHERE PS_" + tableName[0].toUpperCase() + tableName.substring(1) + "ID = " + objectId
		def returnMap = [:]
		returnMap["tableName"] = tableName

		if(dataSource == null) {
			return "error: dataSource is NULL"
		}
		def sql = new Sql(dataSource)
		def dateinameMarbilderbestand
		def bookCoverId
		
		sql.eachRow sqlString, { 
			row -> row.toRowResult().each { 
				k, v -> if(v) {
					if(k.contains("Kurzbeschreibung")) {
						k = "Kurzbeschreibung"
					}
					if(k == "DateinameMarbilderbestand") {
						dateinameMarbilderbestand = v
					}
					
					if(tableName == "buch" && k == "Cover") {
						bookCoverId = v
					}
					returnMap[k] = v
				}	
			}
		}
		
		if(tableName == "marbilderbestand") {
			def sql2String = "SELECT * FROM marbilder WHERE DateinameMarbilder LIKE '" + dateinameMarbilderbestand  + "'"
			
			def imageList = getDataList(sql2String)
			if(imageList.size() > 0) {
				returnMap["image"] = imageList
			}
			return returnMap	
		}
		
		if(tableName == "buch" && bookCoverId) {
			def sql3String = "SELECT * FROM marbilder WHERE FS_BuchseiteId LIKE '" + bookCoverId + "'"
			def imageList = getDataList(sql3String)
			if(imageList.size() > 0) {
				returnMap["image"] = imageList
			}
		}
		
		if(tableName == "marbilder" || tableName == "sammler" || tableName == "literatur" || tableName == "ort") {
			return returnMap	
		}
		
		def imageList = getImages(tableName, objectId)
		if(imageList.size() > 0) {
			returnMap["image"] = imageList
		}
		
		def literatureList = getLiterature(tableName, objectId)
		if(literatureList.size() > 0) {
			returnMap["literature"] = literatureList
		}
		
		if(tableName == "relief" || tableName == "realien" || tableName == "sammlungen" || tableName == "typus") {
			return returnMap
		}
		
		def placeMap = getPlaces(tableName, objectId)
		if(placeMap.size() > 0) {
			returnMap["location"] = placeMap
		}
		
		def datingList = getDating(tableName, objectId)
		if(datingList.size() > 0) {
			returnMap["dating"] = datingList
		}
		
		return returnMap
	}
	
	def private List getDataList(String sqlString) {
		def returnList = []
		def sql = new Sql(dataSource)
		
		sql.eachRow sqlString, {
			row ->
			def itemMap= [:]
			row.toRowResult().each {
				k, v -> if(v) {
					itemMap[k] = v
				}
			}
			returnList.add itemMap
		}
		
		return returnList
	}
	
	def private List getImages(String tableName, String foreignKey) {
		def sqlString = "SELECT * FROM marbilder WHERE FS_" + tableName[0].toUpperCase() + tableName.substring(1) + "ID = " + foreignKey
		
		return getDataList(sqlString)
	}
	
	def private List getDating(String tableName, String foreignKey){
		def sqlString = "SELECT * FROM datierung WHERE FS_" + tableName[0].toUpperCase() + tableName.substring(1) + "ID = " + foreignKey
		
		return getDataList(sqlString)
	}
	
	def private List getLiterature(String tableName, String foreignKey) {
		def sqlString = "SELECT * FROM literaturzitat_leftjoin_literatur WHERE FS_" + tableName[0].toUpperCase() + tableName.substring(1) + "ID = " + foreignKey
		
		return getDataList(sqlString)
	}
	
	def private List getPlaces(String tableName, String foreignKey) {
		def sqlString = "SELECT * FROM ortsbezug_leftjoin_ort WHERE FS_" + tableName[0].toUpperCase() + tableName.substring(1) + "ID = " + foreignKey
		
		return getDataList(sqlString)
	}
}