package org.funz.Telemac;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.fudaa.dodico.ef.operation.EfLineSingleIntersectFinder;
//import org.fudaa.dodico.ef.io.serafin.SerafinAdapter;
//import org.fudaa.dodico.ef.io.serafin.SerafinNewReader;
import org.funz.Telemac.SerafinAdapterHelper.Coordinate;
import org.funz.util.Parser;
import static org.funz.util.ParserUtils.getASCIIFileLines;

/**
 *
 * @author richet
 */
public class TelemacHelper {

    final static String fichier_de_title = "FICHIER D";
    final static String file_title = " FILE";
    final static String variables_sorties_graphiques_title = "VARIABLES POUR LES SORTIES GRAPHIQUES";
    final static String graph_printout_variables_title = "VARIABLES FOR GRAPHIC PRINTOUTS";

    public static final String CommentLine = "/";

    public static final Map<String, String> RES_VAR = new HashMap();

    static {
        RES_VAR.put("VITESSE U", "U");
        RES_VAR.put("VELOCITY U", "U");
        RES_VAR.put("VITESSE V", "V");
        RES_VAR.put("VELOCITY V", "V");
        RES_VAR.put("HAUTEUR D'EAU", "H");
        RES_VAR.put("WATER DEPTH", "H");
        RES_VAR.put("SURFACE LIBRE", "S");
        RES_VAR.put("FREE SURFACE", "S");
        RES_VAR.put("FOND", "B");
        RES_VAR.put("BOTTOM", "B");
        RES_VAR.put("FROUDE", "F");
        RES_VAR.put("DEBIT SCALAIRE", "Q");
        RES_VAR.put("DEBIT SUIVANT X", "I");
        RES_VAR.put("DEBIT SUIVANT Y", "J");
        RES_VAR.put("FROTTEMENT", "W");
        RES_VAR.put("BOTTOM FRICTION", "W");
        RES_VAR.put("NBRE DE COURANT", "L");
        RES_VAR.put("COURANT NUMBER", "L");

        /* sources/telemac2d/telemac2d.dico :
         'U="vitesse suivant l''axe des x  (m/s)"'; 
         'V="vitesse suivant l''axe des y  (m/s)"'; 
         'C="celerite des onde  (m/s)"'; 
         'H="hauteur d''eau  (m)"'; 
         'S="cote de surface libre  (m)"'; 
         'B="cote du fond  (m)"'; 
         'F="nombre de Froude  "'; 
         'Q="debit scalaire du fluide  (m2/s)"'; 
         'T1="traceur 1, etc. "'; 
         'K="energie turbulente du modele k-epsilon  (J/kg)"'; 
         'E="dissipation de l''energie turbulente  (W/kg)"'; 
         'D="viscosite turbulente du modele k-epsilon  (m2/s)"'; 
         'I="debit suivant l''axe des x  (m2/s)"'; 
         'J="debit suivant l''axe des y  (m2/s)"'; 
         'M="vitesse scalaire  (m/s)"'; 
         'X="vent suivant l''axe des x  (m/s)"'; 
         'Y="vent suivant l''axe des y  (m/s)"'; 
         'P="pression atmospherique  (Pa)"'; 
         'W="coefficient de frottement sur le fond  "'; 
         'A="derive en x  (m)"'; 
         'G="derive en y  (m)"'; 
         'L="nombre de Courant  "'; 
         'N="variable supplementaire N "'; 
         'O="variable supplementaire O "'; 
         'R="variable supplementaire R "'; 
         'Z="variable supplementaire Z "'; 
         'MAXZ="cote maximum "'; 
         'TMXZ="temps de la cote maximum "'; 
         'MAXV="vitesse maximum "'; 
         'TMXV="temps de la vitesse maximum "'; 
         'US="vitesse de frottement "' 
         */
    }

    static String[] readVarsSortiesGraphiques(File cas) {
        String[] lines = getASCIIFileLines(cas);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(CommentLine)) {
                continue;
            }

            if (lines[i].trim().contains(variables_sorties_graphiques_title) || lines[i].trim().contains(graph_printout_variables_title)) {
                String variables_sorties_graphiques_txt = null;
                if (lines[i].trim().endsWith("=")) {
                    variables_sorties_graphiques_txt = lines[i + 1].trim();
                } else {
                    variables_sorties_graphiques_txt = Parser.after(lines[i], "=").trim();
                }
                return variables_sorties_graphiques_txt.trim().replace("'", "").split(",");
            }
        }
        return null;
    }

    public static String[] readFichiersDe(File cas, String what) {
        List<String> fde = new LinkedList<String>();
        String[] lines = getASCIIFileLines(cas);
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().startsWith(CommentLine)) {
                continue;
            }

            if ((lines[i].trim().contains(fichier_de_title) || lines[i].trim().contains(file_title)) && lines[i].trim().contains(what)) {
                if (lines[i].trim().endsWith("=")) {
                    fde.add(lines[i + 1].replace("'", "").trim());
                } else {
                    fde.add(Parser.after(lines[i], "=").replace("'", "").trim());
                }
            }
        }
        return fde.toArray(new String[fde.size()]);
    }

    static Map<String, double[][]> extractPOIfromCASRES(File cas, Properties poi) throws Exception {
        return extractPOIfromRES(new File(cas.getAbsoluteFile().getParentFile(), readFichiersDe(cas, "RESULT")[0]), poi);
    }

    static Map<String, double[][]> extractPOIfromRES(File res, Properties poi) throws Exception {
        Map<String, double[][]> dat = new HashMap<String, double[][]>();
    
        //        SerafinNewReader r = new SerafinNewReader();
        //        System.err.println("In " + new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
        //        r.setFile(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
        //        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        SerafinAdapterHelper s = new SerafinAdapterHelper(res);
            
        dat.put("T", new double[][]{s.getPasDeTemps()});
        System.err.println("  containing " + s.getPasDeTemps().length + " time steps");

        for (int vi = 0; vi < s.getVariables().length; vi++) {
            String v = s.getVariables()[vi];
            System.err.println("Reading " + v + " at:");
            for (String p : poi.stringPropertyNames()) {
                System.err.println(p);
                if (poi.getProperty(p).contains(",") && poi.getProperty(p).contains(":")) { // so, this is a x0,y0:nx,ny:x1,y1 zone poi
                    String cs = poi.get(p).toString();
                    String cs0 = cs.substring(0, cs.indexOf(":"));
                    double x0 = Double.parseDouble(cs0.substring(0, cs0.indexOf(",")));
                    double y0 = Double.parseDouble(cs0.substring(cs0.indexOf(",") + 1));
                    String csn = cs.substring(cs.indexOf(":")+1,cs.lastIndexOf(":"));
                    int nx = Integer.parseInt(csn.substring(0, csn.indexOf(",")));
                    int ny = Integer.parseInt(csn.substring(csn.indexOf(",")+1));
                    String cs1 = cs.substring(cs.lastIndexOf(":")+1);
                    double x1 = Double.parseDouble(cs1.substring(0, cs1.indexOf(",")));
                    double y1 = Double.parseDouble(cs1.substring(cs1.indexOf(",") + 1));
                    double[][] d = new double[s.getPasDeTemps().length][nx*ny];
                    double[][] xy = new double[nx*ny][2];
                    for (int nxi = 0; nxi < nx; nxi++) {
                        double x = x0 + ((double)nxi)/((double)nx) * (x1-x0);
                        for (int nyi = 0; nyi < ny; nyi++) {
                            double y = y0 + ((double)nyi)/((double)ny) * (y1-y0);

                            System.err.println("x:"+x+" y:"+y);
                            xy[nxi+nx*nyi][0] = x;
                            xy[nxi+nx*nyi][1] = y;
                            
                            int ie = s.getGrid().getEltContainingXY(x, y);
                            int[] Ix = s.getGrid().getElement(ie).getIndices();
                            int ix = -1;
                            double min_dist2 = Double.MAX_VALUE;
                            for (int _ix = 0; _ix < Ix.length; _ix++) {
                                double dist2 = Math.pow(s.getGrid().getPtX(Ix[_ix]) - x, 2) + Math.pow(s.getGrid().getPtY(Ix[_ix]) - y, 2);
                                if (dist2 < min_dist2) {
                                    min_dist2 = dist2;
                                    ix = Ix[_ix];
                                }
                            }
                            for (int i = 0; i < d.length; i++) {
                                d[i][nxi+nx*nyi] = s.getDonnees(i, vi)[ix];
                            }
                        }
                    }
                    dat.put(RES_VAR.get(v) + "_" + p, d);
                    dat.put(p+"_xy", xy);

                } else if (poi.getProperty(p).contains(",")) { // so, this is a x,y poi, to get containing cell results
                    double[] d = new double[s.getPasDeTemps().length];
                    String cs = poi.get(p).toString();
                    double x = Double.parseDouble(cs.substring(0, cs.indexOf(",")));
                    double y = Double.parseDouble(cs.substring(cs.indexOf(",") + 1));
                    int ie = s.getGrid().getEltContainingXY(x, y);
                    int[] Ix = s.getGrid().getElement(ie).getIndices();
                    int ix = -1;
                    double min_dist2 = Double.MAX_VALUE;
                    for (int _ix = 0; _ix < Ix.length; _ix++) {
                        double dist2 = Math.pow(s.getGrid().getPtX(Ix[_ix]) - x, 2) + Math.pow(s.getGrid().getPtY(Ix[_ix]) - y, 2);
                        if (dist2 < min_dist2) {
                            min_dist2 = dist2;
                            ix = Ix[_ix];
                        }
                    }
                    for (int i = 0; i < d.length; i++) {
                        d[i] = s.getDonnees(i, vi)[ix];
                    }
                    dat.put(RES_VAR.get(v) + "_" + p, new double[][]{d});
                } else { // so, it is a cell number result
                    double[] d = new double[s.getPasDeTemps().length];
                    int ix = Integer.parseInt(poi.get(p).toString()) - 1;
                    for (int i = 0; i < d.length; i++) {
                        d[i] = s.getDonnees(i, vi)[ix];
                    }
                    dat.put(RES_VAR.get(v) + "_" + p, new double[][]{d});
                }
            }
        }

        return dat;
    }

    public static void main(String[] args) {
        if (args==null || args.length<2) {
            System.err.println("You must provide a .res and a poi (as file or arg)");
            System.exit(1);
        }

        String res_file = null;
        if ((args[0].endsWith(".res") || args[0].endsWith(".slf")) && new File(args[0]).isFile())
            res_file=args[0];
        else if ((args[1].endsWith(".res") || args[1].endsWith(".slf")) && new File(args[1]).isFile())
            res_file=args[1];

        String poi_file = null;
        if (args[0].endsWith(".poi") && new File(args[0]).isFile())
            poi_file=args[0];   
        else if (args[1].endsWith(".poi") && new File(args[1]).isFile())
            poi_file=args[1];
        Properties poi = new Properties();  
        if (poi_file==null) {
            for (String arg : args) {
            try{
                if (arg.equals(res_file)) {
                    //do nothing
                } else if (arg.contains("=")) {
                    poi.setProperty(arg.substring(0, arg.indexOf("=")), arg.substring(arg.indexOf("=")+1));
                    System.out.println("Using poi: "+arg);
                } else {
                    poi.setProperty(arg, arg);
                    System.out.println("Using poi: "+arg);
                } 
            } catch(Exception e) {
                System.err.println("Ignoring: "+arg);
            }
            }
        } else {
            try{ poi.load(new FileReader(new File(poi_file))); } catch(Exception e) {e.printStackTrace();}
        }

        if (poi==null || poi.isEmpty()) {
            System.err.println("You must provide a poi file or arg");
            System.exit(1);
        } else 
            System.out.println("Extracting poi:\n"+poi);
        
        if (res_file==null || poi==null){
            System.err.println("You must provide a .res/slf and a poi (as file or arg)");
            System.exit(1);
        }
        
        try {
            Map<String, double[][]> dat = extractPOIfromRES(new File(res_file), poi);
            if (dat==null || dat.isEmpty() || dat.keySet().isEmpty()) 
                System.err.println("Could not extract POI from RES");
            for (String d : dat.keySet()) {
                    write(new File(new File(res_file).getAbsoluteFile().getParentFile(), d + ".csv"), printDoubleArray2D(dat.get(d)));
            }        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    static boolean writeCSVfromCASRES(File cas, Properties poi) throws Exception {
        Map<String, double[][]> dat = extractPOIfromCASRES(cas, poi);
        if (dat==null || dat.isEmpty() || dat.keySet().isEmpty()) return false;
        for (String d : dat.keySet()) {
            write(new File(cas.getAbsoluteFile().getParentFile(), d + ".csv"), printDoubleArray2D(dat.get(d)));
        }
        return true;
    }

    static void write(File f, String s) {
        try {
            FileUtils.writeStringToFile(f, s);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*static String printDoubleArray(double[] d) {
        Object[] o = new Object[d.length];
        for (int i = 0; i < o.length; i++) {
            o[i] = d[i];
        }
        return StringUtils.join(o, '\n');
    }*/

    static String printDoubleArray2D(double[][] d) {
        Object[] o = new Object[d[0].length];
        for (int i = 0; i < o.length; i++) {
            Object[] oi = new Object[d.length];
            for (int j = 0; j < oi.length; j++) {
                oi[j] = d[j][i];
            }
            o[i] = StringUtils.join(oi, ',');
        }
        return StringUtils.join(o, '\n');
    }

    static double[] readDoubleArray(String s) {
        String[] sd = s.split(",");
        double[] d = new double[sd.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = Double.parseDouble(sd[i]);
        }
        return d;
    }

    static double[][] readDoubleArray2D(String s) {
        String[] sd = s.split("\n");
        double[][] d = new double[sd.length][];
        for (int i = 0; i < d.length; i++) {
            d[i] = readDoubleArray(sd[i]);
        }
        return d;
    }
}
