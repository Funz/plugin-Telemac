package org.funz.Telemac;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
//import org.fudaa.dodico.ef.io.serafin.SerafinAdapter;
//import org.fudaa.dodico.ef.io.serafin.SerafinNewReader;
import org.funz.Telemac.SerafinAdapterProxy.Coordinate;
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

    static Map<String, double[]> extractPOIfromRES(File cas, Properties poi) throws Exception {
        Map<String, double[]> dat = new HashMap<String, double[]>();

//        SerafinNewReader r = new SerafinNewReader();
//        System.err.println("In " + new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
//        r.setFile(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
//        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        SerafinAdapterProxy s = new SerafinAdapterProxy(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
        
        dat.put("T", s.getPasDeTemps());
        System.err.println("  containing " + s.getPasDeTemps().length + " time steps");

        for (int vi = 0; vi < s.getVariables().length; vi++) {
            String v = s.getVariables()[vi];
            System.err.println("Reading " + v + " at:");
            for (String p : poi.stringPropertyNames()) {
                System.err.println(p);
                if (p.startsWith("xy")) {
                    double[] d = new double[s.getPasDeTemps().length];
                    String cs = poi.get(p).toString();
                    double x = Double.parseDouble(cs.substring(0, cs.indexOf(",")));
                    double y = Double.parseDouble(cs.substring(cs.indexOf(",") + 1));
                    int ie = s.getGrid().getEltContainingXY(x, y, new Coordinate(0, 0));
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
                    dat.put(RES_VAR.get(v) + "_" + p, d);
                } else {
                    double[] d = new double[s.getPasDeTemps().length];
                    int ix = Integer.parseInt(poi.get(p).toString()) - 1;
                    for (int i = 0; i < d.length; i++) {
                        d[i] = s.getDonnees(i, vi)[ix];
                    }
                    dat.put(RES_VAR.get(v) + "_" + p, d);
                }
            }
        }

        return dat;
    }

    /*public static void main(String[] args) {
        SerafinNewReader r = new SerafinNewReader();
        r.setFile(new File("src/test/resources/t2d_garonne_hydro_init_eng.cas/output/r2d_garonne_MC.res"));
        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        System.err.println(Arrays.asList(s.getVariables()));

        Properties p = new Properties();
        p.setProperty("p1", "34398");
        p.setProperty("xy1", "426379.5,244174.90625");
        try {
            System.err.println(ASCII.cat(",\n", extractPOIfromRES(new File("src/test/resources/t2d_garonne_hydro_init_eng.cas/output/t2d_garonne_hydro_init_eng.cas"), p)));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }*/

    static void writeCSVfromRES(File cas, Properties poi) throws Exception {
        Map<String, double[]> dat = extractPOIfromRES(cas, poi);
        for (String d : dat.keySet()) {
            write(new File(cas.getParentFile(), d + ".csv"), printDoubleArray(dat.get(d)));
        }
    }

    static void write(File f, String s) {
        try {
            FileUtils.writeStringToFile(f, s);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static String printDoubleArray(double[] d) {
        Object[] o = new Object[d.length];
        for (int i = 0; i < o.length; i++) {
            o[i] = d[i];
        }
        return StringUtils.join(o, '\n');
    }

    static double[] readDoubleArray(String s) {
        String[] sd = s.split("\n");
        double[] d = new double[sd.length];
        for (int i = 0; i < d.length; i++) {
            d[i] = Double.parseDouble(sd[i]);
        }
        return d;
    }

}
