package totetmatt.soundcloud.foafdegree

import org.apache.spark.graphx._
import org.apache.spark.rdd.RDD
import org.apache.spark._

import scala.collection.immutable.SortedSet

object FoafDegreeGraphx{
  type Nodedata = (String,SortedSet[String])

  def getDegreeFriends(degree:Int,input:List[(String,String)],sc:SparkContext) ={
    /* Combine methods to create the undirected graph structure*/
    def combineCreate  (a:String) = { SortedSet[String](a) }
    def combine (c:SortedSet[String], a:String) = { c + a  }
    def combineMerge (acc1:SortedSet[String],acc2:SortedSet[String]) = { acc1.++(acc2) }


    val init = sc.parallelize(input)
    /* Create nodes */
    val nodes: RDD[(VertexId,(String,SortedSet[String]))] =
      init
        .flatMap(x=>scala.collection.immutable.List(x._1,x._2))
        .distinct
        .map(x => ( x.hashCode.asInstanceOf[VertexId],(x,SortedSet[String]())  )  )
    /* Create Edges */
    val edges: RDD[Edge[Int]] = init
      .flatMap(x=>List(x,x.swap)).map(x => Edge(x._1.hashCode(),x._2.hashCode(),1))

    /* Graph structore from Graphx */
    val graph = Graph(nodes, edges)

    /* Methods used for the pregel function*/

    // When a node receive a message
    def vprog(id:VertexId,friends:Nodedata,newFriends:SortedSet[String]) : Nodedata = {
      (friends._1,newFriends ++friends._2)
    }

    //Create message to send for the next loop
    def sendMessage(triplet:EdgeTriplet[Nodedata, Int]): Iterator[(VertexId, SortedSet[String])] = {
      Iterator((triplet.dstId, triplet.srcAttr._2 + triplet.srcAttr._1))
    }

    // Create message from all incoming messages
    def mergeMessage(a:SortedSet[String],b:SortedSet[String]):SortedSet[String] = {
      a ++ b
    }

    // Pregel method
    graph.pregel(SortedSet.empty[String],degree,EdgeDirection.Both)(
      vprog,
      sendMessage,
      mergeMessage
    ).vertices.map{case (vid,(stringid,friends))=> (stringid,friends - stringid)}.sortByKey().collect() // Formating as List('id',Set('id'...))
  }
}