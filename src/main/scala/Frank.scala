import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import collection.mutable.HashMap
import curated_newsfeed.Feeder


object Frank {

  def main(args: Array[String]) { 
    val conf = new SparkConf().setAppName("Frank")  
    val sc = new SparkContext(conf)
    
    val feed = Feeder
    val articles = feed.getArticles().zipWithIndex.map(_.swap)
    val all_words = articles.flatMap(line =>line._2._1.trim().split("\\s+",-1))
      .filter(word=> {
        val special_chars = "[^\\dA-Za-z ]".r
        if ((special_chars findFirstIn word) != None) false else true
        }).distinct.toSeq

    // Word count each description
    val wc_feed = sc.parallelize(articles.map (r => {
      val index = r._1
      val description = r._2._1.trim().split("\\s+",-1).toSeq
      val word_count = all_words.map(word => {
        description.filter(el => (el == word)).length.toDouble
      }).toArray
      (index.toLong, Vectors.dense(word_count))
    }))

    val id_article_map = new HashMap[Int,Tuple4[String,String,String,String]]()
    articles.map (r => {
      id_article_map += Tuple2(r._1,r._2)
    })

    val ldaModel = new LDA().setK(10).run(wc_feed)
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics = ldaModel.topicDistributions
      .map(r => {
        val doc_id = r._1
        val vec = r._2
        var topic_id = -1
        for (i <- 0 to 9) {
          if (vec(i) > 0.3) {
            topic_id = i
          }
        }
        (topic_id, List(r._1))
      })
      .filter(r => r._1 != -1)
      .reduceByKey((a,b) => (a ++ b))
  }
}

