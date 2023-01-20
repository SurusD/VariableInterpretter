# VariableInterpretter
Creating Variables in Text
# Example
```Java
VariableInterpretter interpretter = new VariableInterpretter();
HashMap<String, VariableInterpretter.Variable> vars = interpretter.parse("MyPI = PI : $PI;");
for (String k : vars.keySet()) {
     VariableInterpretter.Variable variable1 = vars.get(k);
     String variableData = "Name:" + variable1.NAME + " value: " + String.valueOf(variable1.VALUE);
}
```
