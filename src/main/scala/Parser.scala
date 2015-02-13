import scala.xml.XML
import scala.collection.mutable.ListBuffer
import java.io._
import scala.io.Source

package curated_newsfeed {
  object Feeder {

    var bad_sources = new ListBuffer[String]
    val source_uri= "/Users/shayanmasood/projects/frank/utils/sources.xml"
    val bad_source_uri = "/Users/shayanmasood/projects/frank/utils/bad_sources.txt"

    def using[A <: { def close(): Unit }, B](param: A)(f: A => B): B =
      try { f(param) } finally { param.close() }

    def appendToFile(fileName: String, textData: String) = {
      using(new FileWriter(fileName, true)) {
        fileWriter =>
          using(new PrintWriter(fileWriter)) {
            printWriter => printWriter.println(textData)
          }
      }
    }

    //Read XML RSS feeds
    def parseFile(): Seq[String] = {
      val urls = XML.load(source_uri) \\ "@xmlUrl"
      var sources = new ListBuffer[String]()
      for (url <- urls) {
        if (!bad_sources.contains(url)) {
         	 sources += url.text
        }
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
        appendToFile(bad_source_uri, articles(0)._3 )
      }
      return articles.toSeq
    }

    //Load bad sources logged previously to prevent 403s/404s/401s
    def getBadSources() {
			for (line <- Source.fromFile(bad_source_uri).getLines()) {
			  bad_sources += line.mkString
			}
    }

    //Get all articles	
    def getArticles(): Seq[Seq[Tuple3[String, String, String]]] = {
    	getBadSources()
      val sources = parseFile()
      val articles = new ListBuffer[Seq[Tuple3[String, String, String]]]
      for (source <- sources) {
        try { articles += parseFeed(source) }
        catch {
         case e: Exception => 
         			println("Exception while reading sources : " + e + "\nURL : " + source)
        			bad_sources += source
			        appendToFile(bad_source_uri, source)
        }
     }
      return articles.toSeq
    }
  }
}