package math;/** * Perceptron * * @author Michael Everett Rule * @URL <http://perceptron.sourceforge.net/> */public class ComplexVarList {    private String myVars;    private complex[] myVals;    private int mySize;    public ComplexVarList() {        myVars = "abcdefghijklmnopqrstuvwxyz";        myVals = new complex[26];    }    public ComplexVarList(ComplexVarList otherSet) {        myVals = otherSet.getValues();    }    public int size() {        return mySize;    }    public String getNames() {        return myVars;    }    public complex[] getValues() {        complex[] temp = new complex[myVals.length];        System.arraycopy(myVars, 0, temp, 0, size());        return temp;    }    public complex getVal(char name) {        return myVals[name - 97];    }    public complex get(int index) {        return myVals[index];    }    public complex setVal(char name, complex value) {        return (myVals[name - 97] = value);    }    public complex set(int index, complex value) {        return (myVals[index] = value);    }    public void add(char name, complex value) {        setVal(name, value);    }    public void fillStandard() {        setVal('i', complex.I);        setVal('e', complex.E);        setVal('p', complex.PI);        setVal('f', complex.PHI);    }    public static ComplexVarList standard() {        ComplexVarList result = new ComplexVarList();        result.fillStandard();        return result;    }}