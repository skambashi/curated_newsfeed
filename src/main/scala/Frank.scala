import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import curated_newsfeed.Feeder

object Frank {

	def main(args: Array[String]) { 
		val conf = new SparkConf().setAppName("Frank")	
		val sc = new SparkContext(conf)
		
		val feed = Feeder
		val sources = sc.parallelize(feed.getArticles())
		
	}
}