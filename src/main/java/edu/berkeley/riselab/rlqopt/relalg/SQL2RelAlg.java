package edu.berkeley.riselab.rlqopt.relalg;

import edu.berkeley.riselab.rlqopt.Attribute;
import edu.berkeley.riselab.rlqopt.Database;
import edu.berkeley.riselab.rlqopt.Expression;
import edu.berkeley.riselab.rlqopt.ExpressionList;
import edu.berkeley.riselab.rlqopt.Operator;
import edu.berkeley.riselab.rlqopt.OperatorException;
import edu.berkeley.riselab.rlqopt.OperatorParameters;
import edu.berkeley.riselab.rlqopt.Relation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlJoin;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;

public class SQL2RelAlg {

  Database db;

  public SQL2RelAlg(Database db) {
    this.db = db;
  }

  private HashMap<String, Relation> getTableNameMaps(SqlSelect statement) {

    HashMap<String, Relation> rtn = new HashMap();

    if (statement.getFrom() instanceof SqlJoin) {

      String[] joinExp = statement.getFrom().toString().split(",");

      for (String s : joinExp) {
        String exp = s.toLowerCase();
        if (exp.contains("as")) {
          String relName = exp.split(" ")[0].replace("`", "").trim();
          String aliasName = exp.split(" ")[2].replace("`", "").trim();

          rtn.put(relName, getSingleTable(relName));
          rtn.put(aliasName, getSingleTable(relName));
          // System.out.println(relName + " >> " + aliasName);
        }
      }
    }

    return rtn;
  }

  private HashMap<Relation, TableAccessOperator> getActiveTables(SqlSelect statement)
      throws OperatorException {
    HashMap<Relation, TableAccessOperator> rtn = new HashMap();

    if (statement.getFrom() instanceof SqlIdentifier) {
      Relation r = getSingleTable(statement.getFrom());
      rtn.put(r, getTableAccessOperator(r));
      return rtn;
    }

    SqlJoin joinExp = (SqlJoin) statement.getFrom();

    rtn.putAll(getActiveJoinTables(joinExp));

    return rtn;
  }

  private HashMap<Relation, TableAccessOperator> getActiveJoinTables(SqlJoin joinExp)
      throws OperatorException {

    HashMap<Relation, TableAccessOperator> rtn = new HashMap();

    for (SqlNode s : joinExp.getOperandList()) {

      if (s == null) continue;

      if (s instanceof SqlJoin) rtn.putAll(getActiveJoinTables((SqlJoin) s));

      Relation r = getSingleTable(s);

      if (r == null) continue;

      rtn.put(r, getTableAccessOperator(r));
    }

    return rtn;
  }

  /*private LinkedList<Expression> gatherAllJoinExpressions(
      SqlSelect statement, HashMap<Relation, TableAccessOperator> activeTables) {

    LinkedList<SqlNode> opList = new LinkedList();

    if (statement.getFrom() instanceof SqlJoin) {
      SqlJoin joinExp = (SqlJoin) statement.getFrom();
      opList.addAll(doGatherAllJoinExpressions(joinExp));
    }

    LinkedList<Expression> rtn = new LinkedList();

    for (SqlNode s : opList) rtn.add(parseExpression((SqlBasicCall) s, activeTables));

    return rtn;
  }

  private LinkedList<SqlNode> doGatherAllJoinExpressions(SqlJoin joinExp) {

    LinkedList<SqlNode> opList = new LinkedList();

    for (SqlNode s : joinExp.getOperandList()) {

      if (s instanceof SqlJoin) opList.addAll(doGatherAllJoinExpressions((SqlJoin) s));

      if (isExpression(s)) opList.add(s);
    }
    return opList;
  }*/

  private boolean isExpression(SqlNode s) {

    if (s == null) return false;

    switch (s.getKind()) {
      case EQUALS:
        return true;
      case AND:
        return true;
      case OR:
        return true;
      case NOT_EQUALS:
        return true;
      case GREATER_THAN:
        return true;
      case LESS_THAN:
        return true;
      case GREATER_THAN_OR_EQUAL:
        return true;
      case LESS_THAN_OR_EQUAL:
        return true;
      default:
        break;
    }

    return false;
  }

  private Expression basicCall2Expression(SqlNode node, HashMap<String, Relation> activeTables) {

    SqlSelect select = (SqlSelect) node;
    SqlBasicCall stmt = (SqlBasicCall) (select.getWhere());

    if (stmt == null) return null;

    return parseExpression(stmt, activeTables);
  }

  private Expression parseExpression(SqlBasicCall stmt, HashMap<String, Relation> activeTables) {

    Expression e;

    List<SqlNode> l = stmt.getOperandList();

    switch (stmt.getKind()) {
      case AND:
        return new Expression(
            Expression.AND,
            parseExpression((SqlBasicCall) (l.get(0)), activeTables),
            parseExpression((SqlBasicCall) (l.get(1)), activeTables));

      case OR:
        return new Expression(
            Expression.OR,
            parseExpression((SqlBasicCall) (l.get(0)), activeTables),
            parseExpression((SqlBasicCall) (l.get(1)), activeTables));

      case EQUALS:
        return new Expression(
            Expression.EQUALS,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case LESS_THAN:
        return new Expression(
            Expression.LESS_THAN,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case LESS_THAN_OR_EQUAL:
        return new Expression(
            Expression.LESS_THAN_EQUALS,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case GREATER_THAN:
        return new Expression(
            Expression.GREATER_THAN,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case GREATER_THAN_OR_EQUAL:
        return new Expression(
            Expression.GREATER_THAN_EQUALS,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case IN:
        return new Expression(
            Expression.IN,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case LIKE:
        return new Expression(
            Expression.LIKE,
            literalOrAttribute(l.get(0).toString(), activeTables),
            literalOrAttribute(l.get(1).toString(), activeTables));

      case NOT_EQUALS:
        return new Expression(
            Expression.NOT,
            new Expression(
                Expression.EQUALS,
                literalOrAttribute(l.get(0).toString(), activeTables),
                literalOrAttribute(l.get(1).toString(), activeTables)));

      default:
        return new Expression(stmt.toString().replace("`", ""));
    }

    // return null;
  }

  private Expression literalOrAttribute(String name, HashMap<String, Relation> activeTables) {

    name = name.replace("`", "");

    // System.out.println(name);

    Attribute a = getAttribute(name, activeTables);
    if (a == null) {
      return new Expression(name);
    }

    return new Expression(a);
  }

  private Attribute getAttribute(String name, HashMap<String, Relation> activeTables) {

    name = name.toLowerCase();

    if (name.contains("\'")) return null;

    String[] comps = name.split("\\.");

    if (comps.length != 2) return null;

    String relation = name.split("\\.")[0];
    String attrName = name.split("\\.")[1];

    // System.out.println(name);

    Relation r = activeTables.get(relation);

    // System.out.println(r + " , " + name + " " + r.get(attrName));

    if (r == null) return null;

    return r.get(attrName);
  }

  private Relation getSingleTable(SqlNode in) {
    String name = in.toString();
    Relation r = this.db.getByName(name);
    return r;
  }

  private Relation getSingleTable(String in) {
    String name = in;
    Relation r = this.db.getByName(name);
    return r;
  }

  private TableAccessOperator getTableAccessOperator(Relation r) throws OperatorException {
    OperatorParameters scan_params = new OperatorParameters(r.getExpressionList());
    return new TableAccessOperator(scan_params);
  }

  /*private String cleanSQLString(String in, HashMap<Relation, TableAccessOperator> activeTables) {

    in = in.toLowerCase();

    for (Relation r : activeTables.keySet()) {
      for (String attr : r) {
        in = in.replace("\"" + attr + "\"", attr);
        in = in.replace("\"" + r.name + "\"", r.name);
      }
    }

    return in;
  }*/

  /*

    public Operator makeJoins(SqlNode sqlNode) throws OperatorException {

      HashMap<Relation, TableAccessOperator> activeTables = getActiveTables((SqlSelect) sqlNode);
      ExpressionList elist =
          new ExpressionList(gatherAllJoinExpressions((SqlSelect) sqlNode, activeTables));

      if (activeTables.keySet().size() == 1) {
        for (Relation r : activeTables.keySet()) return activeTables.get(r);
      }

      OperatorParameters params = new OperatorParameters(elist);
      LinkedList<Operator> inputOps = new LinkedList();

      for (Relation r : activeTables.keySet()) inputOps.add(activeTables.get(r));

      Operator[] inputOpsArray = new Operator[inputOps.size()];
      inputOpsArray = inputOps.toArray(inputOpsArray);
      return new KWayJoinOperator(params, inputOpsArray);
    }

    public Operator makeSelect(SqlNode sqlNode, Operator src) throws OperatorException {

      HashMap<Relation, TableAccessOperator> activeTables = getActiveTables((SqlSelect) sqlNode);
      Expression e = basicCall2Expression(sqlNode, activeTables);

      if (e == null)
        return src;

      ExpressionList elist = e.getExpressionList();
      OperatorParameters params = new OperatorParameters(elist);
      return new SelectOperator(params, src);
    }

  */

  public Operator makeJoins(
      java.util.Collection<edu.berkeley.riselab.rlqopt.Relation> tables,
      ArrayList<Expression> conditions)
      throws OperatorException {

    ExpressionList elist =
        new ExpressionList(conditions.toArray(new Expression[conditions.size()]));

    OperatorParameters params = new OperatorParameters(elist);
    LinkedList<Operator> inputOps = new LinkedList();
    HashSet<Relation> tset = new HashSet(tables);

    for (Relation r : tset) inputOps.add(getTableAccessOperator(r));

    Operator[] inputOpsArray = new Operator[inputOps.size()];
    inputOpsArray = inputOps.toArray(inputOpsArray);
    return new KWayJoinOperator(params, inputOpsArray);
  }

  public Operator makeSelect(ArrayList<Expression> conditions, Operator src)
      throws OperatorException {

    for (Expression child : conditions) {
      ExpressionList elist = child.getExpressionList();
      OperatorParameters params = new OperatorParameters(elist);
      src = new SelectOperator(params, src);
    }

    return src;
  }

  public Operator convert(String sql) throws SqlParseException {
    SqlParser parser = SqlParser.create(sql);
    SqlNode sqlNode = parser.parseStmt();
    HashMap<String, Relation> activeTables = getTableNameMaps((SqlSelect) sqlNode);
    Expression conditions = basicCall2Expression(sqlNode, activeTables);

    ArrayList<Expression> predicates = conditions.getAllSingleTableExpressions();
    ArrayList<Expression> joinConditions = conditions.getAllJoinTableExpressions();

    try {

      Operator j = makeJoins(activeTables.values(), joinConditions);
      Operator sj = makeSelect(predicates, j);

      return j;

    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ;

    return null;
  }

  public static String prettyPrint(String sql) {
    int index0 = sql.indexOf("(");
    int indexf = sql.lastIndexOf(")");
    sql = sql.substring(index0 + 1, indexf);

    /*sql = sql.replace("FROM", "\nFROM");
    sql = sql.replace("WHERE", "\nWHERE");
    sql = sql.replace("SELECT", "SELECT");
    sql = sql.replace("GROUP BY", "\nGROUP BY");*/

    return sql;
  }
}
