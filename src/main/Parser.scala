import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import scala.xml.XML
import scala.collection.mutable.ListBuffer

object Feeder {

	def parseFile () : Seq[String] = {
		val urls = XML.load("/Users/skambashi/Desktop/Projects/curated_newsfeed/utils/sources.xml") \\ "outline" \\ "outline" \\ "@xmlUrl"
		var sources = new ListBuffer[String]()
		for (url <- urls) {
			sources += (url text)
		}
		return sources.toSeq
	}

	def parseFeed (source : String) : Seq[Tuple3[String,String,String]] = {
		val items = XML.load(source) \\ "item"
		var articles = new ListBuffer[Tuple3[String,String,String]]
		for (item <- items) {
			val title = (item \\ "title").toString
			val desc = (item \\ "description").toString
			val link = (item \\ "link").toString
			articles += Tuple3(title,desc,link)
		}
		return items.toSeq
	}

	def main(args: Array[String]) { 
		val sources = parseFile().map(source => parseFeed(source))
	}
}