package org.funz.Telemac;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import static org.funz.Telemac.TelemacHelper.CommentLine;
import static org.funz.Telemac.TelemacHelper.readDoubleArray2D;
import static org.funz.Telemac.TelemacHelper.simplify;
import static org.funz.Telemac.TelemacHelper.readVarsSortiesGraphiques;
import org.funz.ioplugin.*;
import org.funz.parameter.OutputFunctionExpression;
import org.funz.parameter.SyntaxRules;
import org.funz.util.Parser;
import org.funz.parameter.InputFile;

public class TelemacIOPlugin extends ExtendedIOPlugin {

    static String[] DOC_LINKS = {"http://www.opentelemac.org/"};
    static String INFORMATION = "Telemac plugin made by Yann Richet\nCopyright IRSN";

    public TelemacIOPlugin() {
        variableStartSymbol = VariableStartSymbol;
        variableLimit = VariableLimit;
        formulaStartSymbol = FormulaStartSymbol;
        formulaLimit = FormulaLimit;
        commentLine = CommentLine;
        String id = "Telemac";
        String class_str = this.getClass().getResource('/' + this.getClass().getName().replace('.', '/') + ".class").toString();
        if (class_str.contains(".ioplugin.jar")) {
            id = class_str.substring(0,class_str.indexOf(".ioplugin.jar"));
            id = id.substring(id.lastIndexOf('/')+1);
        }
        this.setID(id);    
    }

    public static final int VariableStartSymbol = SyntaxRules.START_SYMBOL_DOLLAR;
    public static final int VariableLimit = SyntaxRules.LIMIT_SYMBOL_PARENTHESIS;
    public static final int FormulaStartSymbol = SyntaxRules.START_SYMBOL_AT;
    public static final int FormulaLimit = SyntaxRules.LIMIT_SYMBOL_BRACKETS;

    @Override
    public boolean acceptsDataSet(File f) {
        return f.isFile() && f.getName().endsWith(".cas") && ( Parser.contains(f, "FICHIER DE GEOMETRIE") || Parser.contains(f, "GEOMETRY FILE"));
    }

    @Override
    public String getPluginInformation() {
        return INFORMATION;
    }

    @Override
    public void setInputFiles(File... inputfiles) {
        _inputfiles = inputfiles;
        _output.clear();
        Properties pois = new Properties();

        File cas = null;
        for (File file : inputfiles) {
            if (file.isFile() && file.getName().endsWith(".cas")) {
                cas = file;
            }
            if (file.isFile() && file.getName().endsWith(".poi")) {
                try {
                    Properties poi =  new Properties();
                    poi.load(new FileInputStream(file));
                    
                    Stream<Entry<Object, Object>> stream = poi.entrySet().stream();
                    Map<String, String> m = stream.collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue())));
                    
                    pois.putAll(m);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (pois.isEmpty()) {
            System.err.println("Could not find .poi file !");
        }

        if (cas == null) {
            System.err.println("Could not find .cas file !");
        } else {
            String[] variables_sorties_graphiques = readVarsSortiesGraphiques(cas);

            _output.put("T", new double[]{0, .1, .2, .3, .4, .5, .6, .7, .8, .9});

            if (variables_sorties_graphiques != null && !pois.isEmpty()) {
                for (String o : variables_sorties_graphiques) {
                    for (String p : pois.stringPropertyNames()) {
                        if (p.charAt(0)!='.') // ignore ".abc" output
                            _output.put(o + "_" + p.replace(" ", ""), new double[]{Math.random(), Math.random(), Math.random(), Math.random(), Math.random(), Math.random(), Math.random(), Math.random(), Math.random(), Math.random()});
                    }
                }

            }
        }
    }

    @Override
    public HashMap<String, Object> readOutput(File outdir) {
        HashMap<String, Object> lout = new HashMap<String, Object>();

        File[] csvfiles = outdir.listFiles(new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".csv");
            }
        });

        if (csvfiles == null || csvfiles.length == 0) {
            lout.put("warning","Could not find csv files, so reading results from results slf.");
            File cas = null;
            Properties pois = new Properties();
            try {
                for (File file : outdir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".cas")) {
                        cas = file;
                    }
                    if (file.isFile() && file.getName().endsWith(".poi")) {
                        try {
                            Properties poi =  new Properties();
                            poi.load(new FileInputStream(file));
                            
                            Stream<Entry<Object, Object>> stream = poi.entrySet().stream();
                            Map<String, String> m = stream.collect(Collectors.toMap(
                                e -> String.valueOf(e.getKey()),
                                e -> String.valueOf(e.getValue())));
                            
                            pois.putAll(m);
                        } catch (Exception ex) {
                            StringWriter sw = new StringWriter();
                            PrintWriter pw = new PrintWriter(sw);
                            ex.printStackTrace(pw);                        
                            lout.put("error","Could not read poi file "+file.getName()+" : "+sw.toString());
                        }
                    }
                }

                if (pois.isEmpty()) {
                    lout.put("error","Could not find any .poi file !");
                    return lout;
                }

                if (cas == null) {
                    lout.put("error","Could not find .cas file !");
                    return lout;
                }
                Map<String, double[][]> all = TelemacHelper.extractPOIfromCASRES(cas, pois);
                for (String k : all.keySet())
                    if (k.charAt(0)!='.') // ignore ".abc" output
                        lout.put(k, all.get(k));
                all = null;
            } catch (Exception e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                lout.put("error","Could not read coord "+pois+" in results of cas "+cas.getName()+" : "+sw.toString());
            }
        } else {
            for (File f : csvfiles) {
                try {
                    String k = f.getName().substring(0, f.getName().indexOf(".csv"));
                    if (k.length()>3 && k.charAt(2)!='.') // ignore ".abc" output
                        lout.put(k, readDoubleArray2D(FileUtils.readFileToString(f)));
                } catch (IOException ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    lout.put("error","Could not read csv file "+f.getName()+" : "+sw.toString());
                }
            }
        }

        for (String k:lout.keySet()) { // simplify if possible to 1D arrays
            if (lout.get(k) instanceof double[][])
                lout.put(k,simplify((double[][])lout.get(k)));
        }

        return lout;
    }

    @Override
    public LinkedList<OutputFunctionExpression> suggestOutputFunctions() {
        LinkedList<OutputFunctionExpression> s = new LinkedList<OutputFunctionExpression>();
        s.addFirst(new OutputFunctionExpression.NumericArray("T"));
        for (String k : _output.keySet()) {
            if (_output.get(k) instanceof Double) {
                s.addFirst(new OutputFunctionExpression.Numeric(k));
            } else if (_output.get(k) instanceof double[] && k.contains("_")) {
                s.addFirst(new OutputFunctionExpression.Numeric("max(" + k + ")"));
                double[] v = (double[]) _output.get(k);
                if (v.length > 1) {
                    s.addFirst(new OutputFunctionExpression.NumericArray(k));
                }
            }
        }
        return s;
    }

    @Override
    public LinkedList<File> getRelatedFiles(File cas) {
        LinkedList<File> toimport = new LinkedList();
        toimport.add(cas);
        if (cas.isFile() && cas.getName().endsWith(".cas")) {
            File[] pois = cas.getAbsoluteFile().getParentFile().listFiles();
            for (File file : pois) {
                if (file.isFile() && file.getName().endsWith(".poi"))
                toimport.add(file);
            }
            String[] deps = TelemacHelper.readFichiersDe(cas, ""); // get any possible deps
            for (String d : deps) {
                //System.err.println("? "+d);
                File f = new File(cas.getAbsoluteFile().getParentFile(),d);
                if (f.isFile()) {
                    //System.err.println("Found related file "+f);
                    toimport.add(f);
                } else {
                    //System.err.println("Could not find related file "+f);
                }
            }
        }
        return toimport;
    }
    
    @Override
    public InputFile[] importFileOrDir(File src) throws Exception {
        LinkedList<InputFile> all = new LinkedList();
        for (File f : getRelatedFiles(src)) {
            InputFile[] toadd = super.importFileOrDir(f);
            for (InputFile a : toadd) all.add(a);
        }
        return all.toArray(new InputFile[all.size()]);
    }
    
}
