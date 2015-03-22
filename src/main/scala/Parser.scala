import scala.xml.XML
import scala.collection.mutable.ListBuffer
import java.io._
import scala.io.Source

package curated_newsfeed {
  object Feeder {

    val source_uri = "/Users/shayanmasood/projects/frank/utils/sources.xml"
    
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
    def parseFeed(source: String, articles: ListBuffer[Tuple4[String, String, String,String]]) ={
      val items = XML.load(source) \\ "item"
      val pattern = "(<([^>]+)>)".r
    
      for (item <- items) {
        val title = (item \\ "title").text.trim().toString
        val pub_date = (item \\ "pubDate").text.trim().toString
        val link = (item \\ "link").text.trim().toString
        val desc = (pattern replaceAllIn ((item \\ "description" ).text.trim(),"")).trim().toString
        if (!title.isEmpty && !pub_date.isEmpty && !link.isEmpty && !desc.isEmpty) {
          articles += Tuple4(title, pub_date, link, desc)
        }
      }
      if (articles.length < 5) {
        println("WARNING | source is shit : " + source)
      }
    }

    //Load bad sources logged previously to prevent 403s/404s/401s
   

    //Get all articles	
    def getArticles(): Seq[Tuple4[String, String, String,String]] = {
      val sources = parseFile()
      val articles = new ListBuffer[Tuple4[String, String, String,String]]
      for (source <- sources) {
        try { parseFeed(source, articles) }
        catch {
          case e: Exception =>
            println("Exception while reading sources : " + e + "\nURL : " + source)
        }
      }
      return articles.toSeq
    }
  }
}