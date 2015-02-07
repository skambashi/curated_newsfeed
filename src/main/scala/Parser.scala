import scala.xml.XML
import scala.collection.mutable.ListBuffer

package object Feeder {

	def parseFile () : Seq[String] = {
		val urls = XML.load("/Users/skambashi/Desktop/Projects/curated_newsfeed/utils/sources.xml") \\ "@xmlUrl"
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
			val title = (item \\ "title" text)
			val desc = (item \\ "description" text)
			val link = (item \\ "link" text)
			articles += Tuple3(title,desc,link)
		}
		if (articles.length < 5) {
			println("WARNING | source is shit : " + source)
		}
		return articles.toSeq
	}

	def getArticles() : Seq[Seq[Tuple3[String,String,String]]] = {
		val sources = parseFile()
		val articles = new ListBuffer[Seq[Tuple3[String,String,String]]]
		for(source <- sources) {
			try {articles += parseFeed(source)}
			catch {case e : Exception => println("Exception while reading sources : " + e + "\nURL : " + source)}
		}
		return articles.toSeq
	}
}