import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.xml.XML

object Feeder {

	def parseFile (uri : String) : Map[String] = 
		val source = scala.io.Source.fromFile(uri)
		val lines = source.mkString
		source.close()
	}

	def main(args: Array[String]) { 

				
	}
}