import java.util.*;

public class SemanticAnalyzer {
    private Map<String, String> varTypes = new HashMap<>();
    private Map<String, Integer> intVars = new HashMap<>();
    private Map<String, Float> floatVars = new HashMap<>();
    private boolean hasError = false;
    private List<String> errors = new ArrayList<>();

    public void checkDeclaration(String name, String type) {
        if (varTypes.containsKey(name)) {
            error("Variable already declared: " + name);
        } else {
            varTypes.put(name, type);
            if (type.equals("int")) {
                intVars.put(name, 0);
            } else {
                floatVars.put(name, 0.0f);
            }
        }
    }

    public void checkVariable(String name, int line) {
        if (!varTypes.containsKey(name)) {
            error("Undeclared variable: " + name + " at line " + line);
        }
    }

    public void checkAssignment(String name, String valueType, int line) {
        if (!varTypes.containsKey(name)) {
            error("Undeclared variable: " + name + " at line " + line);
            return;
        }
        String varType = varTypes.get(name);
        if (!varType.equals(valueType)) {
            error("Type mismatch: cannot assign " + valueType + " to " + varType + " variable " + name + " at line " + line);
        }
    }

    public String getVarType(String name) {
        return varTypes.getOrDefault(name, "int");
    }

    public int getIntValue(String name) {
        return intVars.getOrDefault(name, 0);
    }

    public float getFloatValue(String name) {
        return floatVars.getOrDefault(name, 0.0f);
    }

    public void setIntValue(String name, int value) {
        if (varTypes.containsKey(name) && varTypes.get(name).equals("int")) {
            intVars.put(name, value);
        }
    }

    public void setFloatValue(String name, float value) {
        if (varTypes.containsKey(name) && varTypes.get(name).equals("float")) {
            floatVars.put(name, value);
        }
    }

    private void error(String msg) {
        hasError = true;
        errors.add(msg);
        System.err.println("SEMANTIC ERROR: " + msg);
    }

    public boolean hasError() { return hasError; }
    public List<String> getErrors() { return errors; }
    public Map<String, String> getSymbolTable() { return varTypes; }
}