# User Change Management Study - how not to
This project contains an implementation of a solution (JSON Serialization) on how to apply User Actions in a Neo4j database. 
The project contains a main function that can be executed to directly apply the User Actions.

## Getting started
The main function executes either the cypher query application of User Actions or the user-defined procedures of the "User-Change-Management-UDP" project. There is a boolean in the main function which controls the execution. In oder to execute the main function a Neo4j database needs to be active. All Activity nodes and the User Action node have to already exist. In order to add them to the database use the respective udp procedures. A description on how to do this is given in the Readme in the "User-Change-Management-UDP" project.