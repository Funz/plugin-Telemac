package org.funz.Telemac;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import static org.funz.Telemac.TelemacHelper.CommentLine;
import static org.funz.Telemac.TelemacHelper.readDoubleArray;
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

        File cas = null;
        File coords = null;
        for (File file : inputfiles) {
            if (file.isFile() && file.getName().endsWith(".cas")) {
                cas = file;
            }
            if (file.isFile() && file.getName().endsWith(".poi")) {
                coords = file;
            }
        }

        Properties poi = null;
        if (coords == null) {
            System.err.println("Could not find .poi file !");
        } else {
            poi = new Properties();
            try {
                poi.load(new FileInputStream(coords));
            } catch (Exception ex) {
                ex.printStackTrace();
                poi = null;
            }
        }

        if (cas == null) {
            System.err.println("Could not find .cas file !");
        } else {
            String[] variables_sorties_graphiques = readVarsSortiesGraphiques(cas);

            _output.put("T", new double[]{0, .1, .2, .3, .4, .5, .6, .7, .8, .9});

            if (variables_sorties_graphiques != null && poi != null) {
                for (String o : variables_sorties_graphiques) {
                    for (String p : poi.stringPropertyNames()) {
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
            System.err.println("Warning: could not find csv files, so reading results from .res.");
            try {
                File cas = null;
                File coords = null;
                for (File file : outdir.listFiles()) {
                    if (file.isFile() && file.getName().endsWith(".cas")) {
                        cas = file;
                    }
                    if (file.isFile() && file.getName().endsWith(".poi")) {
                        coords = file;
                    }
                }

                Properties poi = null;
                if (coords == null) {
                    System.err.println("Could not find .poi file !");
                    return lout;
                } else {
                    poi = new Properties();
                    try {
                        poi.load(new FileInputStream(coords));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        poi = null;
                    }
                }

                lout.putAll(TelemacHelper.extractPOIfromRES(cas, poi));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (File f : csvfiles) {
            try {
                lout.put(f.getName().substring(0, f.getName().indexOf(".csv")), readDoubleArray(FileUtils.readFileToString(f)));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
            File poi = new File(cas.getParentFile(),cas.getName().replace(".cas",".poi"));
            if (poi.isFile()) toimport.add(poi);
            String[] deps = TelemacHelper.readFichiersDe(cas, ""); // get any possible deps
            for (String d : deps) {
                System.err.println("? "+d);

                File f = new File(cas.getParentFile(),d);
                if (f.isFile()) {
                    System.err.println("Found related file "+f);
                    toimport.add(f);
                } else {
                    System.err.println("Could not find related file "+f);
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
