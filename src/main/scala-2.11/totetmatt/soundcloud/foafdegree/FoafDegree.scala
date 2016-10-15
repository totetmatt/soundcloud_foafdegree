package totetmatt.soundcloud.foafdegree

import org.apache.spark._
import org.apache.spark.rdd.RDD

import scala.collection.immutable.SortedSet

object FoafDegree {

  def getDegreeFriends(degree: Int, input: List[(String, String)], sc: SparkContext) = {

    /* Combine methods to create the undirected graph structure*/
    def combineCreate(a: String) = {
      SortedSet[String](a)
    }
    def combine(c: SortedSet[String], a: String) = {
      c + a
    }
    def combineMerge(acc1: SortedSet[String], acc2: SortedSet[String]) = {
      acc1.++(acc2)
    }


    /**
      * Transform the input into a graph structure
      * List( (source,Set(Targets...))
      **/
    val base_graph = sc.parallelize(input)
      .flatMap(x => List(x, x.swap))
      .combineByKey(
        combineCreate,
        combine,
        combineMerge)
      .cache() // As we need to use it later on for each iteration, it's a good idea to cache the graph

    /**
      *
      * @param graph Original Graph
      * @param n     Degree to look for
      * @return List of nodes with their friends at degree *n*
      */
    def degreeFriends(graph: RDD[(String, SortedSet[String])], n: Int): RDD[(String, SortedSet[String])] = {
      if (n == 1) {
        // Our graph structure is a degree one
        graph
      } else {
        // That's the main "process" to get 1 degree forward. Notice that we use our original graph to know the "next" friends at each loop
        degreeFriends(graph, n - 1).flatMap { case (node: String, friends: SortedSet[String]) => friends.map(friend => (friend, node)) }
          .join(base_graph)
          .map { case (friend: String, (node: String, foaf: SortedSet[String])) => (node, foaf + friend - node) }
          .reduceByKey((acc, x) => acc ++ x)
      }
    }

    /**
      * Sorting by Key and print / generate file
      */

    degreeFriends(base_graph, degree)
      .sortByKey().collect()
  }
}
