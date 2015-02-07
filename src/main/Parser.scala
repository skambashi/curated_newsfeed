import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.xml.XML

object Feeder {

	def parseFile (uri : String) : Array[String] = {
		val feed = XML.load("/Users/shayanmasood/projects/frank/utils/sources.xml") \\ "outline" \\ "outline" \\ "@xmlUrl"
		return feed.toArray
	}

	def main(args: Array[String]) { 
		
				
	}
}