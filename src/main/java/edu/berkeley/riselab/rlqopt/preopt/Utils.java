package edu.berkeley.riselab.rlqopt.preopt;

import edu.berkeley.riselab.rlqopt.Operator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {
  static List<Operator> map(List<Operator> in, Function<Operator, Operator> func) {
    return in.stream().map(func).collect(Collectors.toList());
  }

  static List<Operator> filterRecursive(Operator in, Predicate<Operator> pred) {
    LinkedList<Operator> operators = new LinkedList<>();
    if (pred.test(in)) {
      operators.add(in);
    }
    for (Operator child : in.source) operators.addAll(filterRecursive(child, pred));
    return operators;
  }
}
