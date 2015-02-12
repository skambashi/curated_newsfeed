import scala.xml.XML
import scala.collection.mutable.ListBuffer
import java.io._


package curated_newsfeed {
	object Feeder {

		//Read XML RSS feeds
		def parseFile () : Seq[String] = {
			val urls = XML.load("/Users/shayanmasood/projects/frank/utils/sources.xml") \\ "@xmlUrl"
			var sources = new ListBuffer[String]()
			for (url <- urls) {
				sources += url.text
			}
			return sources.toSeq
		}

		//Parse feed to return [title, pubDate, link]
		def parseFeed (source : String) : Seq[Tuple3[String,String,String]] = {
			val items = XML.load(source) \\ "item"
			var articles = new ListBuffer[Tuple3[String,String,String]]
			for (item <- items) {
				val title = (item \\ "title").text.trim()
				val pubDate = (item \\ "pubDate").text.trim()
				val link = (item \\ "link").text.trim()
				articles += Tuple3(title,pubDate,link)
			}
			if (articles.length < 5) {
				println("WARNING | source is shit : " + source)
				val writer = new PrintWriter(new File("bad_sources.txt"))
				writer.write (source + "\n")
			}
			return articles.toSeq
		}

		//Get all articles	
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
}