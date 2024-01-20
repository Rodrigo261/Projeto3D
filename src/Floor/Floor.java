package Floor;

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

public class Floor extends Shape3D {
    public Floor(int divisions, float min, float max, Color3f color1, Color3f color2, boolean solid) {
        int m = divisions;
        float a = min;
        float b = max;
        float divX = (b - a) / m;

        int n = divisions;
        float c = min;
        float d = max;
        float divZ = (d - c) / n;

        int totalPts = m * n * 4;

        Point3f[] pts = new Point3f[totalPts];
        Color3f[] col = new Color3f[totalPts];

        int idx = 0;
        boolean invert = true;

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                float x = a + i * divX;
                float z = c + j * divZ;
                float y = 0f;
                pts[idx] = new Point3f(x, y, z);
                col[idx] = (invert ? color1 : color2);
                idx++;

                x = a + i * divX;
                z = c + (j + 1) * divZ;
                pts[idx] = new Point3f(x, y, z);
                col[idx] = (invert ? color1 : color2);
                idx++;

                x = a + (i + 1) * divX;
                z = c + (j + 1) * divZ;
                pts[idx] = new Point3f(x, y, z);
                col[idx] = (invert ? color1 : color2);
                idx++;

                x = a + (i + 1) * divX;
                z = c + j * divZ;
                pts[idx] = new Point3f(x, y, z);
                col[idx] = (invert ? color1 : color2);
                idx++;

                invert = !invert;
            }
            if (divisions % 2 == 0)
                invert = !invert;
        }

        QuadArray geom = null;
        Appearance app = new Appearance();
        PolygonAttributes pa = new PolygonAttributes();

        if (solid) {
            geom = new QuadArray(totalPts, QuadArray.COORDINATES | QuadArray.COLOR_3);
            geom.setColors(0, col);
            pa.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        } else {
            geom = new QuadArray(totalPts, QuadArray.COORDINATES);
            pa.setPolygonMode(PolygonAttributes.POLYGON_LINE);
            app.setColoringAttributes(new ColoringAttributes(color1, ColoringAttributes.SHADE_FLAT));
        }

        geom.setCoordinates(0, pts);
        pa.setCullFace(PolygonAttributes.CULL_NONE);

        app.setPolygonAttributes(pa);

        this.setGeometry(geom);
        this.setAppearance(app);
    }
}