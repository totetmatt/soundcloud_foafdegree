package totetmatt.soundcloud.foafdegree.test

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import totetmatt.soundcloud.foafdegree._

import scala.collection.immutable.SortedSet

@RunWith(classOf[JUnitRunner])
class FoafDegreeTest extends FunSuite {
  val test_input = List(
    ("davidbowie", "omid"),
    ("davidbowie", "kim"),
    ("kim", "torsten"),
    ("torsten", "omid"),
    ("brendan", "torsten"),
    ("ziggy", "davidbowie"),
    ("mick", "ziggy")
  )
  val expect = List(
    ("brendan", SortedSet("kim", "omid", "torsten")),
    ("davidbowie", SortedSet("kim", "mick", "omid", "torsten", "ziggy")),
    ("kim", SortedSet("brendan", "davidbowie", "omid", "torsten", "ziggy")),
    ("mick", SortedSet("davidbowie", "ziggy")),
    ("omid", SortedSet("brendan", "davidbowie", "kim", "torsten", "ziggy")),
    ("torsten", SortedSet("brendan", "davidbowie", "kim", "omid")),
    ("ziggy", SortedSet("davidbowie", "kim", "mick", "omid"))
  )
  val conf = new SparkConf().setMaster("local[*]").setAppName("Foaf Degree Test")
  val sc = new SparkContext(conf)

  test("Spark Native : Test from example") {
    val result = FoafDegree.getDegreeFriends(2, test_input, sc)
    assert(result === expect)
  }
  test("Spark GraphX : Test from example ") {
    val result = FoafDegreeGraphx.getDegreeFriends(2, test_input, sc)
    assert(result === expect)
  }
}
