package org.funz.Telemac;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.fudaa.dodico.ef.io.serafin.SerafinAdapter;
import org.fudaa.dodico.ef.io.serafin.SerafinNewReader;
import static org.funz.Telemac.TelemacHelper.RES_VAR;
import static org.funz.Telemac.TelemacHelper.readFichiersDe;
import org.funz.util.ASCII;
import org.junit.Test;

/**
 *
 * @author richet
 */
public class SerafinAdapterHelperTest {

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main(SerafinAdapterHelperTest.class.getName());
    }

    static Map<String, double[]> extractPOIfromRES_PROXY(File cas, Properties poi) throws Exception {
        Map<String, double[]> dat = new HashMap<String, double[]>();

//        SerafinNewReader r = new SerafinNewReader();
//        System.err.println("In " + new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
//        r.setFile(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
//        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        SerafinAdapterHelper s = new SerafinAdapterHelper(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));

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

    static Map<String, double[]> extractPOIfromRES_NO_PROXY(File cas, Properties poi) throws Exception {
        Map<String, double[]> dat = new HashMap<String, double[]>();

        SerafinNewReader r = new SerafinNewReader();
        System.err.println("In " + new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
        r.setFile(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));
        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
//        SerafinAdapterHelper s = new SerafinAdapterHelper(new File(cas.getParentFile(), readFichiersDe(cas, "RESULT")[0]));

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
                    int ie = s.getGrid().getEltContainingXY(x, y, new com.vividsolutions.jts.geom.Coordinate(0.0, 0.0));
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

    @Test
    public void testProxy() throws Exception {

        SerafinNewReader r = new SerafinNewReader();
        r.setFile(new File("src/test/cases/t2d_breach.cas/output/r2d_breach.slf"));
        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());

        SerafinAdapterHelper sp = new SerafinAdapterHelper(new File("src/test/cases/t2d_breach.cas/output/r2d_breach.slf"));
        assert Arrays.deepEquals(s.getVariables(), sp.getVariables()) : Arrays.asList(s.getVariables()) + " != " + Arrays.asList(sp.getVariables());

        Properties p = new Properties();
        //p.setProperty("p1", "1000");
        p.setProperty("xymediumcenter", "2500,250");
        try {
            assert ASCII.cat(",\n", extractPOIfromRES_NO_PROXY(new File("src/test/cases/t2d_breach.cas/output/t2d_breach.cas"), p)).equals(ASCII.cat(",\n", extractPOIfromRES_PROXY(new File("src/test/cases/t2d_breach.cas/output/t2d_breach.cas"), p))) : ASCII.cat(",\n", extractPOIfromRES_NO_PROXY(new File("src/test/cases/t2d_breach.cas/output/t2d_breach.cas"), p)) + "\n != \n" + ASCII.cat(",\n", extractPOIfromRES_PROXY(new File("src/test/cases/t2d_breach.cas/output/t2d_breach.cas"), p));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
