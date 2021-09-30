package org.funz.Telemac;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.funz.Constants;

/**
 *
 * @author richet
 */
public class SerafinAdapterHelper {

    static Object serafinNewReader;
    static Method serafinNewReader_setFile;
    static Method serafinNewReader_read;
    Object serafinAdapter;
    static Class classCoordinate;

    static {
        ClassLoader child = ClassLoader.getSystemClassLoader();
        try {
            File jar = new File(Constants.APP_INSTALL_DIR.getPath() + File.separator +"lib", "fudaa-prepro-1.3.0.jar");
            if (!jar.isFile()) {
                System.err.println("Cannot access file: " + jar);
                throw new Exception("Cannot access file: " + jar);
            }
            child = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()});/*,
                    (URLClassLoader) ClassLoader.getSystemClassLoader()
            );*/
        } catch (Exception ex) {
            System.err.println("Failed to load fudaa jar");
            ex.printStackTrace();
        } catch (Error e) {
            System.err.println("Error while loading fudaa jar");
            e.printStackTrace();
        }

        try {
            Class classSerafinNewReader = Class.forName("org.fudaa.dodico.ef.io.serafin.SerafinNewReader", true, child);
            if (classSerafinNewReader==null) System.err.println("Cannot load class org.fudaa.dodico.ef.io.serafin.SerafinNewReader");
            serafinNewReader = classSerafinNewReader.newInstance();
            serafinNewReader_setFile = classSerafinNewReader.getMethod("setFile", File.class);
            serafinNewReader_read = classSerafinNewReader.getMethod("read");

            classCoordinate = Class.forName("com.vividsolutions.jts.geom.Coordinate", true, child);

            //System.out.println("Well loaded com.vividsolutions.jts.geom.Coordinate.");
        } catch (Exception ex) {                
            System.err.println("Cannot load com.vividsolutions.jts.geom.Coordinate !");
            ex.printStackTrace();
        }
    }

    public SerafinAdapterHelper(File source) throws Exception {
        serafinNewReader_setFile.invoke(serafinNewReader, source);
        Object read = serafinNewReader_read.invoke(serafinNewReader);
        serafinAdapter = read.getClass().getMethod("getSource").invoke(read);
        //System.err.println("serafinAdapter: " + serafinAdapter);
    }

    double[] getPasDeTemps() throws Exception {
        Object ret = serafinAdapter.getClass().getMethod("getPasDeTemps").invoke(serafinAdapter);
        return (double[]) ret;
    }

    String[] getVariables() throws Exception {
        Object ret = serafinAdapter.getClass().getMethod("getVariables").invoke(serafinAdapter);
        return (String[]) ret;
    }

    double[] getDonnees(int i, int vi) throws Exception {
        Object ret = serafinAdapter.getClass().getMethod("getDonnees", Integer.TYPE, Integer.TYPE).invoke(serafinAdapter, i, vi);
        return (double[]) ret;
    }

    Grid getGrid() throws Exception {
        return new Grid(serafinAdapter.getClass().getMethod("getGrid").invoke(serafinAdapter));
    }

    public class Grid {

        Object grid;

        public Grid(Object grid) {
            this.grid = grid;
        }

        public Element getElement(int i) throws Exception {
            return new Element(grid.getClass().getMethod("getElement", Integer.TYPE).invoke(grid, i));
        }

        public int getEltContainingXY(double x, double y) throws Exception {
            Coordinate c = new Coordinate(0,0);
            return (Integer) grid.getClass().getMethod("getEltContainingXY", Double.TYPE, Double.TYPE, classCoordinate).invoke(grid, x, y, c.coordinate);
        }

        public double getPtX(int i) throws Exception {
            return (Double) grid.getClass().getMethod("getPtX", Integer.TYPE).invoke(grid, i);
        }

        public double getPtY(int i) throws Exception {
            return (Double) grid.getClass().getMethod("getPtY", Integer.TYPE).invoke(grid, i);
        }

    }

    public class Element {

        Object element;

        public Element(Object element) {
            this.element = element;
        }

        public int[] getIndices() throws Exception {
            return (int[]) element.getClass().getMethod("getIndices").invoke(element);
        }
    }

    public class Coordinate {

        Object coordinate;

        public Coordinate(double i, double j) throws Exception {
            coordinate = classCoordinate.getConstructor(Double.TYPE, Double.TYPE).newInstance(i, j);
        }
    }

    /*public static void main(String[] args) throws Exception {
//        SerafinNewReader r = new SerafinNewReader();
//        r.setFile(new File("src/test/resources/t2d_garonne_hydro_init_eng.cas/output/r2d_garonne_MC.res"));
//        SerafinAdapter s = (SerafinAdapter) (r.read().getSource());
        SerafinAdapterProxy s = new SerafinAdapterProxy(new File("src/test/cases/t2d_breach.cas/output/r2d_breach.slf"));
        System.err.println(Arrays.asList(s.getVariables()));

        Properties p = new Properties();
        p.setProperty("p1", "1000");
        p.setProperty("xymediumcenter", "2500,250");
        try {
            System.err.println(ASCII.cat(",\n", extractPOIfromRES(new File("src/test/cases/t2d_breach.cas/output/r2d_breach.slf"), p)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/
}
