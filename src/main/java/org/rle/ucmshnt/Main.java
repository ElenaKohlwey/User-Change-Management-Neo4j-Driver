package org.rle.ucmshnt;

import org.rle.ucms.entity.JsonConversion;
import org.rle.ucms.entity.UserActionHistory;
import org.rle.ucms.entity.UserAction;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Transaction;
import static org.neo4j.driver.Values.parameters;

public class Main implements AutoCloseable {

  private final Driver driver;

  // Constructors
  public Main(String uri, String user, String password) {
    driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
  }

  // close connection to database
  @Override
  public void close() {
    driver.close();
  }

  // Apply User Changes using the Neo4j Driver
  public void applyUserActionHistoryToGraph() {
    // fetch the json String from the Neo4j db
    String changeLogJson;
    try (Transaction tx = driver.session().beginTransaction()) {
      changeLogJson =
        tx
          .run("MATCH (a:UserActionHistory) return a.changeLog as changeLog")
          .single()
          .get("changeLog", "");
    }

    // create new UserActionHistory object
    UserActionHistory uah = new UserActionHistory();

    // transform the json String of the userActionHistory node into the UserActionHistory object
    try {
      uah = JsonConversion.toUserActionHistory(changeLogJson);
    } catch (Exception e) {
      e.printStackTrace();
    }

    // apply all User Actions from the User Action History to the graph
    applyToGraph(uah);
  }
  
  public void applyToGraph(UserActionHistory uah){
    
      UserAction [] uaArray = uah.getAll().toArray(UserAction[]::new);
      
      for (int i = 0; i< uaArray.length; i++ ) {
        UserAction ua = uaArray[i];
        this.driver.session().writeTransaction((Transaction tx) -> {
          tx.run(" MATCH (a:Activity {id: $id}) "
               + " SET a."+  ua.getChangedActionKey() + " = $newValue",
                parameters("id", ua.getAffectedNodeId(),
                           "newValue", ua.getNewValue()));
          return 1;
          });
      }
    }
  
  
  // Apply User Changes using the user-defined procedure
  public void applyUserActionHistoryToGraphThroughUdp() {
      driver.session().writeTransaction((Transaction tx) -> {
        tx.run("call org.rle.ucms.applyUserActionHistory()");
        return 1;
        });
  }

  public static void main(String... args) {
    Main myMain = new Main("bolt://localhost:7687", "neo4j", "test");

    /* set this boolean to true if you want to apply the User Actions using user-defined procedures
    and set this boolean to false if you want to apply the User Actions using the Neo4j Driver.*/
    boolean applicationThroughUdp = false;
    
    // Execute application of User Actions and measure time
    long currentTimeMillisBefore = System.currentTimeMillis();

    if (applicationThroughUdp){
        myMain.applyUserActionHistoryToGraphThroughUdp();
    }
    else{
        myMain.applyUserActionHistoryToGraph();
    }
    
    long currentTimeMillisAfter = System.currentTimeMillis();
    double durationInSeconds = (currentTimeMillisAfter - currentTimeMillisBefore)/1000.0;
    
    System.out.println("Application of user changes took: " + durationInSeconds + " seconds.");
    
    myMain.close();
  }
}
