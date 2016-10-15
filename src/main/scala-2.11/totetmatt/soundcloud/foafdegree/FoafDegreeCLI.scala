package totetmatt.soundcloud.foafdegree

import java.io.{File, PrintWriter}

import org.apache.spark.{SparkConf, SparkContext}

import scala.io.Source

case class Config(degree: Int = 2,
                  inputfile: String = "./input.txt",
                  outputfile: String = "./output.txt",
                  sparkmaster: String = "local[*]",
                  graphx: Boolean = false
                 )

object FoafDegreeCLI {
  def main(args: Array[String]) {
    val parser = new scopt.OptionParser[Config]("Foaf Degree") {
      head("Friend of a Friend Degree CLI by @Totetmatt", "1.0")
      opt[Int]('d', "degree").optional() action { (x, c) =>
        c.copy(degree = x)
      } text "Degree of Separation"
      opt[String]('i', "input").optional() action { (x, c) =>
        c.copy(inputfile = x)
      } text "Input File"

      opt[String]('o', "output").optional() action { (x, c) =>
        c.copy(outputfile = x)
      } text "Output File"

      opt[String]('s', "sparkmaster").optional() action { (x, c) =>
        c.copy(sparkmaster = x)
      } text "Spark Master Connection"

      opt[Boolean]('g', "graphx").optional() action { (x, c) =>
        c.copy(graphx = x)
      } text "Use GraphX framework from Spark"

      help("help").text("Prints this usage text")
    }
    val config = parser.parse(args, Config())
    val data = Source.fromFile(config.get.inputfile).getLines().map(line => {
      val x = line.split("\t"); (x(0), x(1))
    }).toList
    // Should use >> sc.textFile or better the CSV class for Spark , but it adds dependecies if you don't have hadoop (especially problematics for Windows)

    // Init Spark Context
    val conf = new SparkConf().setMaster(config.get.sparkmaster).setAppName("Foaf Degree")
    val sc = new SparkContext(conf)
    val result = if (config.get.graphx) {
      FoafDegreeGraphx.getDegreeFriends(config.get.degree, data, sc)
    } else {
      FoafDegree.getDegreeFriends(config.get.degree, data, sc)
    }

    val pw = new PrintWriter(new File(config.get.outputfile))
    // Spark has a lot of saveAS that should be used (like .saveAsHadoopDataset())
    result.foreach { case (node: String, friends: Set[String]) => pw.write(node + "\t" + friends.mkString("\t") + "\n") }
    pw.close

  }
}