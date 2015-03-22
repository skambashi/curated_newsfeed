import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.linalg.Vectors
import curated_newsfeed.Feeder


object Frank {

  def main(args: Array[String]) { 
    val conf = new SparkConf().setAppName("Frank")  
    val sc = new SparkContext(conf)
    
    val feed = Feeder
    val articles = sc.parallelize(feed.getArticles().zipWithIndex.map(_.swap))
    val all_words = articles.flatMap(line =>line._2._4.trim().split("\\s+",-1)).distinct()

    // Word count each description
    val wc_feed = articles.map (r => {
      val index = r._1
      val description = r._2._4.trim().split("\\s+",-1).toSeq
      val word_count = all_words.map(word => {
        description.filter(el => (el == word)).length.toDouble
      })
      word_count
      //description
      // (index, Vectors.dense(word_count))
    })

    val ldaModel = new LDA().setK(10).run(wc_feed)
    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics = ldaModel.topicDistributions
    
  }
}

