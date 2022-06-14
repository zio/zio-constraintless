// A simple representation of an input
// query that produce a value of type
import executor._

sealed trait Query[+A] 

object Query {

    

    implicit def canExecuteQuery[A]: executor.CanExecute[Query, A] = 
        new executor.CanExecute[Query, A] {
          override def execute(f: Query[A]): executor.IO[A] = 
            f match {
                case Api(_) => None
                case DbQuery(_) => None
            }   
        }
    final case class Api[A](input: String) extends Query[A]
    final case class DbQuery[A](input: String) extends Query[A]
}