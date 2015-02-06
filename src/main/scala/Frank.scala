import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object Frank {


	def extractLineItems (line: String) : String = {
		return line.trim().split("\\|", -1)(6)	
	}

	def getDid (line : String) : String = {
		return line.trim().split("\\|", -1)(0)
	}

	def getLatLong (line: String) : Tuple2[String, String] = {
		val lat = extractLat(line)
		val long = extractLong(line)
		return (lat, long)
	}

	def extractLat (line: String) : String = {
		return line.trim().split("\\|", -1)(10)
	}

	def extractLong (line: String) : String = {
		return line.trim().split("\\|", -1)(11)
	}

	def extractTileId (line: String) : String = {
		return line.trim().split("\\|", -1)(1)
	}

	def extractClick (line: String) : String = {
		return line.trim().split("\\|", -1)(8)
	}

	def matchLineItems (line : String) : Boolean = {
		val LINEITEMS = Array (	"61367650","62267290","61368490","62266330", "61369090","62268130", "66350290","62268370","61370530", "62268490","61370650", "62268610", "61433770", "62268730", "61370770")
		return LINEITEMS contains line
	}

	def getClickCount (line : String) : Int = {
		if (extractClick(line).toInt > 0) 
			return 1
		else 
			return 0
	}

	def extractPolygonToWKT (line : String) : Tuple2[String, String] = {
		val polygonId = line.trim().split("\\|",-1)(0).split(":",-1)(1)
		val wktString = line.trim().split("\\|",-1)(1)
		return (polygonId, wktString)
	}

	def getUDID (line: String) : Array[Tuple2 [String, Int]] = {

		val deviceId1 = line.trim().split("\\|", -1)(14)
		val deviceId2 = line.trim().split("\\|", -1)(16)
		val deviceId3 = line.trim().split("\\|", -1)(17)
		val deviceId4 = line.trim().split("\\|", -1)(18)
		val deviceId5 = line.trim().split("\\|", -1)(19)
		val clickCount = getClickCount(line)
		val nonClickCount = 1 - clickCount

		return Array ((deviceId1, clickCount),(deviceId2, clickCount),
			(deviceId3, clickCount),(deviceId4, clickCount),(deviceId5, clickCount))
	}


	def main(args: Array[String]) { 

		
		val conf = new SparkConf().setAppName("Chrysler Data")
		.set("spark.executor.memory", "13g")
		.set("spark.storage.memoryFraction", "0.3")
		.set("spark.shuffle.memoryFraction", "0.7")
		.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
		.set("spark.eventLog.enabled", "true")
		.set("spark.eventLog.dir", "/home/hadoop/spark/logs/")
		.set("spark.core.containsxection.ack.wait.timeout", "12000") /* 20 minutes */
		.set("spark.shuffle.manager", "sort")
		val sc = new SparkContext(conf)
		val bcPolygons = sc.broadcast(sc.textFile(chryslerWKT).map (line => extractPolygonToWKT(line)).collect.toSet)


		val campLogs = sc.textFile(campaignLog)
			.filter(line => matchLineItems(extractLineItems(line)))
			.flatMap (line => getUDID(line))
			.reduceByKey(_+_,240)

		val bcChrysler =  sc.broadcast (sc.textFile(chryslerTiles)
			.map(line => line.split(":")(1).split(",")(1)).collect.toSet)


		val joinOnUDID = sc.textFile(logrecordjoin)
			.filter (line => bcChrysler.value.contains(extractTileId(line)))
			.map (line => (getDid(line), getLatLong(line))).join(campLogs).distinct()
			.coalesce (5, true)


		def findPointIntersections (tup: Tuple2[String, Tuple2[Tuple2[String,String], Int]]) : Boolean ={
			val long = tup._2._1._1.toDouble
			val lat = tup._2._1._2.toDouble
			val geometryFactory = new GeometryFactory()
			val reader = new WKTReader(geometryFactory)
			val point = geometryFactory.createPoint(new Coordinate(lat, long))
			for (elem <- bcPolygons.value) {
					if (reader.read(elem._2).contains(point)) {
						return true
					}
			}
			return false
		}		

		val filterByLatLong = joinOnUDID.filter (line => findPointIntersections(line))


		filterByLatLong.saveAsTextFile("/user/hadoop/output/" )				
		
	}
}