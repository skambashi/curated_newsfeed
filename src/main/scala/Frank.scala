import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object Frank {

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

		filterByLatLong.saveAsTextFile("/user/hadoop/output/" )
		
	}
}