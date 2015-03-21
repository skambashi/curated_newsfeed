import scala.xml.XML
import scala.collection.mutable.ListBuffer
import java.io._
import scala.io.Source

package curated_newsfeed {
  object Feeder {

    val source_uri = "utils/sources.xml"
    
    //Read XML RSS feeds
    def parseFile(): Seq[String] = {
      val urls = XML.load(source_uri) \\ "@xmlUrl"
      var sources = new ListBuffer[String]()
      for (url <- urls) {
        sources += url.text
      }
      return sources.toSeq
    }

    //Parse feed to return [title, pubDate, link]
    def parseFeed(source: String): Seq[Tuple3[String, String, String]] = {
      val items = XML.load(source) \\ "item"
      var articles = new ListBuffer[Tuple3[String, String, String]]
      for (item <- items) {
        val title = (item \\ "title").text.trim()
        val pubDate = (item \\ "pubDate").text.trim()
        val link = (item \\ "link").text.trim()
        articles += Tuple3(title, pubDate, link)
      }
      if (articles.length < 5) {
        println("WARNING | source is shit : " + source)
      }
      return articles.toSeq
    }

    //Load bad sources logged previously to prevent 403s/404s/401s
   

    //Get all articles	
    def getArticles(): Seq[Seq[Tuple3[String, String, String]]] = {
      val sources = parseFile()
      val articles = new ListBuffer[Seq[Tuple3[String, String, String]]]
      for (source <- sources) {
        try { articles += parseFeed(source) }
        catch {
          case e: Exception =>
            println("Exception while reading sources : " + e + "\nURL : " + source)
        }
      }
      return articles.toSeq
    }
  }
}