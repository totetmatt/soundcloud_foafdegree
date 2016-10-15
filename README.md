## Graph database
We have a Graph problem, therefore why not using a Graph database ? 
Example with Neo4J (but similar apporach works with other graph db)

```
CREATE (davidbowie:User {name:"davidbowie"}),
(omid:User {name:"omid"}),
(kim:User {name:"kim"}),
(torsten:User {name:"torsten"}),
(brendan:User {name:"brendan"}),
(ziggy:User {name:"ziggy"}),
(mick:User {name:"mick"}),

(davidbowie)-[:FRIEND]->(omid),
(davidbowie)-[:FRIEND]->(kim),
(kim)-[:FRIEND]->(torsten),
(torsten)-[:FRIEND]->(omid),
(brendan)-[:FRIEND]->(torsten),
(ziggy)-[:FRIEND]->(davidbowie),
(mick)-[:FRIEND]->(ziggy)


START n=node(*) 
MATCH p = (n)-[*1..2]- (y) 
WHERE y.name <> n.name
WITH n.name as name, collect(distinct y.name) as d
RETURN name,d
ORDER BY name
```
The solution is partially done, as the list of friends is not sorted, but as you will run this querry in an application, you can still add a sort after the query exectuion

## Simple Application

We can have an elegant way of writing a recursive function to traverse the graph until Nth degree.

```
import scala.collection.immutable.SortedSet
val l = List[(String,String)](("davidbowie","omid"),
  ("davidbowie","kim"),
  ("kim","torsten"),
  ("torsten","omid"),
  ("brendan","torsten"),
  ("ziggy","davidbowie"),
  ("mick","ziggy"))

val db = l.flatMap(x =>  List( x ,x.swap))
  .foldLeft(Map.empty[String, SortedSet[String]]) { case (acc, (k, v)) =>
    acc.updated(k, acc.getOrElse(k, SortedSet.empty[String]) ++ SortedSet(v))
  }

def friends (start:String,nb:Int) : SortedSet[String] =  {
     def friendsacc(start:String,visited:SortedSet[String],next: SortedSet[String],nb:Int) : SortedSet[String] = {
       if(next.isEmpty || nb==0) {
         visited - start
       } else {
         friendsacc(start,visited ++ next, next.foldLeft(SortedSet[String]())((x,y)=>{x ++ db(y)}) -- visited -- next ,nb-1)
       }
     }
     friendsacc(start,SortedSet(),db(start),nb)
}
db.map(x => (x._1,friends(x._1,1)))
  .toSeq
  .sortBy(_._1)
  .foreach(x=> println(x._1+" "+x._2.mkString(" ")))
```

One of the important point is to avoid to revisit nodes we already visits.
It will works fine for small graph but we can question on large graph if the recurisve part won't create trouble in term of space.

## MapReduce based on Spark
See source code 

Usage : 
* `sbt test` : Launch the test defined in `src/test`
* `sbt run`  : Launch the app 
* `sbt "run --help"` : Get the help
* `sbt "run -i anotherinput.txt -o anotheroutput.txt -d 3"` : If you have other file and requierment
* `sbt "run -g true` : Use the GraphX based solution
```
[info] Running totetmatt.soundcloud.foafdegree.FoafDegreeCLI --help
Friend of a Friend Degree CLI by @Totetmatt 1.0
Usage: Foaf Degree [options]

  -d, --degree <value>     Degree of Separation
  -i, --input <value>      Input File
  -o, --output <value>     Output File
  -s, --sparkmaster <value>
                           Spark Master Connection
  -g, --graphx <value>     Use GraphX framework from Spark
  --help                   prints this usage text
```
## Comments
### Complexity and Implementation
For N =Nodes and Edges = E : Complexity should be `N Log(E)` Time and Space (for Worst case).

An improvement would be to keep the Degree Friends separated to only visit last Degree when expanding the degree range.
   
### Spark instead of Hadoop Streaming  
Spark is basically a Map Reduce framework that could be replaced by Hadoop Streaming. It gives several advantages :

* The context is kept open during all the job execution, so for multiple Map Reduce job here, only 1 Context initialisation time is necessary
* Spark cares about partionning and distributing the data for map reduce operation (RDD Partition)
* Some operational process like fault tolerance are included and tools exists to schedule jobs and monitor jobs

Another advantage is that Spark comes directly with GraphX, which is a spark framework that handles graph over Map Reduce which on this case would help.
The second solution use GraphX using the Pregel method, which intend to be for "large-scale graph processing". 
On the demo example, the time to compute the correct result takes more time with GraphX, but it might overcome the normal Map Reduce method for large graph.
### File loading
The method to read and write data is the basic scala one, but it would be recommanded to use all defaults Spark method to read and write files from HDFS for example. 

The current implementation has the advantage to reduce the dependency to make it easy to use in the context of the challenge.

### Graph for what ?
To fine tune the algorithm, we need to understand more what kind of graph we have.
As it sounds a social network, we knows that :
* Extreme graph won't appears (hairball, graph without edges etc...)
* There will be multiple clusters (communities) and differents "known" types of nodes (Authority, Betweeness Centrality, Bot ...)
* The small world effect will tend the number of relation between any user to 6.

Based on that, we could tune the program to get it faster or refine it to offer better insight on the problem we want to solve.