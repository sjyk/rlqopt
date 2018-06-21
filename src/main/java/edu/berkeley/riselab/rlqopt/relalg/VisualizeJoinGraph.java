package edu.berkeley.riselab.rlqopt.relalg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.calcite.sql.SqlCall;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlBasicVisitor;

class JoinGraph {
  // From (left, right) -> [joinCondition].
  private Map<List<String>, List<String>> graph = new HashMap<>();

  // FROM <orig> AS <alias>.
  private Map<String, String> aliasToOrig = new HashMap<>();

  private String cleanSqlString(String sql) {
    return sql.toLowerCase().replace("`", "").replace(" = ", "=");
  }

  private String resolveAlias(String alias) {
    if (aliasToOrig.containsKey(alias)) return aliasToOrig.get(alias);
    return alias;
  }

  void addEdge(String leftRel, String rightRel, String joinCond) {
    leftRel = resolveAlias(cleanSqlString(leftRel));
    rightRel = resolveAlias(cleanSqlString(rightRel));
    joinCond = cleanSqlString(joinCond);
    List<String> key = Arrays.asList(leftRel, rightRel);
    List<String> val = graph.get(key);
    if (val == null) {
      val = new ArrayList<>();
      val.add(joinCond);
    } else {
      val.add(joinCond);
    }
    graph.put(key, val);
  }

  void addNameAlias(String origName, String alias) {
    aliasToOrig.put(cleanSqlString(alias), cleanSqlString(origName));
  }

  String toGraphvizString() {
    StringBuilder sb = new StringBuilder();
    sb.append("graph JoinGraph {\n");
    for (List<String> key : graph.keySet()) {
      List<String> joinConds = graph.get(key);
      for (String joinCond : joinConds) {
        sb.append("  ");
        sb.append(key.get(0));
        sb.append(" -- ");
        sb.append(key.get(1));
        sb.append(" [label=\"");
        sb.append(joinCond);
        sb.append("\"]\n");
      }
    }
    sb.append("}\n");
    return sb.toString();
  }
}

class MyVisitor<R> extends SqlBasicVisitor {
  private JoinGraph joinGraph;

  MyVisitor(JoinGraph joinGraph) {
    this.joinGraph = joinGraph;
  }

  public R visit(SqlCall call) {
    if (call.getOperator().toString().equals("=")) {
      //      System.out.println("**** found atomic join cond?");
      // Left rel.
      SqlNode leftNode = call.getOperandList().get(0);
      String leftId = leftNode.toString();
      String[] leftSplits = leftId.split("\\.");
      String leftRelation = leftSplits[0];
      // Right rel.
      SqlNode rightNode = call.getOperandList().get(1);
      String rightId = rightNode.toString();
      String[] rightSplits = rightId.split("\\.");
      String rightRelation = rightSplits[0];

      //      System.out.println(
      //          "leftRel "
      //              + leftRelation
      //              + " rightRel "
      //              + rightRelation
      //              + " cond "
      //              + leftCol
      //              + "="
      //              + rightCol);

      // This check is a hack...
      if (!leftRelation.contains("'") && !rightRelation.contains("'")) {
        joinGraph.addEdge(leftRelation, rightRelation, call.toString());
      }
    } else if (call.getOperator().toString().equals("AS")) {
      //      System.out.println("call = " + call + "; " + call.getOperator());
      String[] splits = call.toString().split(" AS ");
      joinGraph.addNameAlias(splits[0], splits[1]);
    }
    return (R) call.getOperator().acceptCall(this, call);
  }
}

/** Takes in a list of file paths pointing to queries, writes out a .dot file for each. */
public class VisualizeJoinGraph {

  public static String queryToGraphviz(SqlNode root) {
    JoinGraph joinGraph = new JoinGraph();
    MyVisitor<Void> visitor = new MyVisitor<>(joinGraph);
    visitor.visit(SqlNodeList.of(root));
    return joinGraph.toGraphvizString();
  }

  public static void main(String[] args) throws IOException, SqlParseException {
    for (String arg : args) {
      Path inputPath = Paths.get(arg);
      List<String> allLines = Files.readAllLines(inputPath);
      String query = String.join("\n", allLines);

      // For some reason an ending ";" crashes Calcite, but it works for postgres ;).
      query = query.trim();
      query = query.substring(0, query.length() - 1);

      // Also, the input queries have != in predicates, but Calcite recognizes <> only.
      query = query.replace("!=", "<>");

      try {
        System.out.println("query = " + query);
        SqlParser parser = SqlParser.create(query);
        SqlNode sqlNode = parser.parseStmt();

        String graphViz = queryToGraphviz(sqlNode);

        Path dotPath =
            Paths.get(
                inputPath.getParent().toString(),
                inputPath.getFileName().toString().replace(".sql", ".dot"));
        Files.write(dotPath, graphViz.getBytes("utf8"));
      } catch (Exception e) {
        System.out.println(e.toString());
        // Continue.
      }
    }
  }
}
