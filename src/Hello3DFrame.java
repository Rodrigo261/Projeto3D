import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.picking.PickIntersection;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickResult;
import Floor.Floor;
import com.sun.j3d.utils.image.TextureLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Hello3DFrame extends Frame {
    private int numCylindersClicked = 0;
    private Set<Vector3f> clickedLocations = new HashSet<>();
    private SimpleUniverse su;
    private TransformGroup characterTG;
    private PickCanvas pickCanvas;
    private BranchGroup textTG;
    private BranchGroup root;

    private List<TransformGroup> listOfCubeTransformGroups = new ArrayList<>();
    private boolean forwardKeyPressed = false;
    private boolean backwardKeyPressed = false;
    private boolean leftKeyPressed = false;
    private boolean rightKeyPressed = false;
    private Appearance greenAppearance;
    public static void main(String[] args) {
        Frame frame = new Hello3DFrame();
        frame.setPreferredSize(new Dimension(640, 480));
        frame.setTitle("Hello 3D");
        frame.pack();
        frame.setVisible(true);
    }

    public Hello3DFrame() {
        GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
        Canvas3D cv = new Canvas3D(gc);

        setLayout(new BorderLayout());
        add(cv, BorderLayout.CENTER);

        WindowListener wl = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        };
        addWindowListener(wl);
        greenAppearance = new Appearance();
        Material greenMaterial = new Material();
        greenMaterial.setDiffuseColor(new Color3f(0.0f, 1.0f, 0.0f));
        greenMaterial.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        greenAppearance.setMaterial(greenMaterial);
        su = new SimpleUniverse(cv);
        su.getViewingPlatform().setNominalViewingTransform();

        root = createSceneGraph();
        root.compile();

        su.addBranchGraph(root);

        pickCanvas = new PickCanvas(cv, root);
        pickCanvas.setMode(PickCanvas.GEOMETRY);

        Timer timer = new Timer(40, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                updateCharacterPosition();
            }
        });
        timer.start();

        Canvas3D canvas = su.getCanvas();
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });

        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e);
            }
        });
        canvas.setFocusable(true);
        canvas.requestFocus();

        MouseRotate mouseRotate = new MouseRotate(su.getViewingPlatform().getViewPlatformTransform());
        mouseRotate.setSchedulingBounds(new BoundingSphere());
        mouseRotate.setFactor(0.005);
        su.getViewer().getView().setMinimumFrameCycleTime(5);
        su.getViewer().getView().setSceneAntialiasingEnable(true);

        BranchGroup mouseBranch = new BranchGroup();
        mouseBranch.addChild(mouseRotate);
        su.addBranchGraph(mouseBranch);
    }

    private TransformGroup createRandomCylinder(Vector3f position) {

        Appearance randomAppearance = new Appearance();
        Material randomMaterial = new Material();
        randomMaterial.setDiffuseColor(new Color3f(0.0f, 0.0f, 1.0f)); // Cor azul
        randomMaterial.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        randomAppearance.setMaterial(randomMaterial);


        Cylinder randomCylinder = new Cylinder(0.1f, 0.3f, Cylinder.GENERATE_NORMALS | Cylinder.GENERATE_TEXTURE_COORDS, randomAppearance);


        Transform3D cylinderTransform = new Transform3D();
        cylinderTransform.setTranslation(position);
        TransformGroup cylinderTG = new TransformGroup(cylinderTransform);
        cylinderTG.addChild(randomCylinder);

        return cylinderTG;
    }
    private TransformGroup createGreenCube(Vector3f position) {
        Appearance greenAppearance = new Appearance();
        Material greenMaterial = new Material();
        greenMaterial.setDiffuseColor(new Color3f(0.0f, 1.0f, 0.0f)); // Cor verde
        greenMaterial.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        greenAppearance.setMaterial(greenMaterial);

        Box greenBox = new Box(0.2f, 0.2f, 0.2f, greenAppearance);
        Transform3D greenTransform = new Transform3D();
        greenTransform.setTranslation(position);
        TransformGroup greenTG = new TransformGroup(greenTransform);
        greenTG.addChild(greenBox);

        return greenTG;
    }

    private BranchGroup createSceneGraph() {
        BranchGroup root = new BranchGroup();
        root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
        Appearance app = new Appearance();
        Material material = new Material();
        material.setDiffuseColor(new Color3f(0.8f, 0.8f, 0.8f));
        material.setSpecularColor(new Color3f(1.0f, 1.0f, 1.0f));
        app.setMaterial(material);

        BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
        Background bk = new Background();
        bk.setApplicationBounds(bounds);
        bk.setColor(new Color3f(0.5f, 0.5f, 0.5f));
        root.addChild(bk);

        for (int i = 0; i < 30; i++) {
            float randomCubeX, randomCubeZ;
            boolean isCloseToExistingObject;

            do {
                randomCubeX = (float) (Math.random() * 10 - 5);
                randomCubeZ = (float) (Math.random() * 10 - 5);
                Vector3f cubePosition = new Vector3f(randomCubeX, 0.0f, randomCubeZ);
                isCloseToExistingObject = isCloseToExistingObject(cubePosition, null);
            } while (isCloseToExistingObject);

            Vector3f cubePosition = new Vector3f(randomCubeX, 0.0f, randomCubeZ);
            TransformGroup cubeTG = createGreenCube(cubePosition);
            listOfCubeTransformGroups.add(cubeTG);
            root.addChild(cubeTG);
        }


        for (int i = 0; i < 5; i++) {
            float randomCylinderX, randomCylinderZ;
            boolean isCloseToExistingObject;

            do {
                randomCylinderX = (float) (Math.random() * 10 - 5);
                randomCylinderZ = (float) (Math.random() * 10 - 5);
                Vector3f cylinderPosition = new Vector3f(randomCylinderX, -0.2f, randomCylinderZ);
                isCloseToExistingObject = isCloseToExistingObject(cylinderPosition, null);
            } while (isCloseToExistingObject);

            Vector3f cylinderPosition = new Vector3f(randomCylinderX, -0.2f, randomCylinderZ);
            TransformGroup cylinderTG = createRandomCylinder(cylinderPosition);
            root.addChild(cylinderTG);
        }

        Box greenBox = new Box(0.2f, 0.2f, 0.2f, greenAppearance);
        Transform3D greenTransform = new Transform3D();
        greenTransform.setTranslation(new Vector3f(1.0f, 0.0f, -2.0f));
        TransformGroup greenTG = new TransformGroup(greenTransform);
        greenTG.addChild(greenBox);

        root.addChild(greenTG);

        Font3D font3D = new Font3D(new Font("SanSerif", Font.PLAIN, 1), new FontExtrusion());
        Text3D text = new Text3D(font3D, "Hello 3D");
        Shape3D shape = new Shape3D(text, app);

        PointLight light = new PointLight(new Color3f(1f, 1f, 1f), new Point3f(1f, 1f, 1f), new Point3f(1f, 0.1f, 0f));

        light.setInfluencingBounds(bounds);
        root.addChild(light);

        Transform3D tr = new Transform3D();
        tr.setScale(0.5);
        tr.setTranslation(new Vector3f(-0.95f, 0.65f, 0f));
        TransformGroup tg = new TransformGroup(tr);
        tg.addChild(shape);
        root.addChild(tg);


        Appearance sphereTextureAppearance = new Appearance();
        TextureLoader textureLoader = new TextureLoader("C:/Users/hg197/IdeaProjects/Projeto3D/src/images.jpeg", this);
        Texture texture = textureLoader.getTexture();
        sphereTextureAppearance.setTexture(texture);

        Sphere characterSphere = new Sphere(0.1f, Sphere.GENERATE_NORMALS | Sphere.GENERATE_TEXTURE_COORDS, 32, sphereTextureAppearance);
        Transform3D characterTransform = new Transform3D();
        characterTransform.setTranslation(new Vector3f(0.0f, -0.2f, 0f));
        characterTG = new TransformGroup(characterTransform);
        characterTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        characterTG.addChild(characterSphere);


        root.addChild(characterTG);

        KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(characterTG);
        keyNavBeh.setSchedulingBounds(bounds);
        root.addChild(keyNavBeh);

        Floor floor = new Floor(50, -5.0f, 5.0f, new Color3f(0.5f, 0.5f, 0.5f), new Color3f(0.2f, 0.2f, 0.2f), true);
        tr = new Transform3D();
        tr.setScale(1.0f);
        tr.setTranslation(new Vector3f(0.0f, -0.3f, 0.0f));
        tg = new TransformGroup(tr);
        tg.addChild(floor);
        root.addChild(tg);

        createTorus(root);



        textTG = new BranchGroup();
        textTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        root.addChild(textTG);

        return root;
    }
    private boolean isCloseToExistingObject(Vector3f position, Vector3f specificObjectPosition) {
        float minDistanceSquared = 0.5f * 0.5f;


        if (specificObjectPosition != null) {
            float dxSpecific = position.x - specificObjectPosition.x;
            float dzSpecific = position.z - specificObjectPosition.z;
            if ((dxSpecific * dxSpecific + dzSpecific * dzSpecific) < minDistanceSquared) {
                return true;
            }
        }


        for (TransformGroup cubeTG : listOfCubeTransformGroups) {
            Transform3D cubeTransform = new Transform3D();
            cubeTG.getTransform(cubeTransform);
            Vector3f cubePosition = new Vector3f();
            cubeTransform.get(cubePosition);

            float dx = position.x - cubePosition.x;
            float dz = position.z - cubePosition.z;

            if ((dx * dx + dz * dz) < minDistanceSquared) {
                return true;
            }
        }

        return false;
    }

    private void createTorus(BranchGroup root) {
        Appearance torusApp = new Appearance();
        Material torusMaterial = new Material();
        torusMaterial.setDiffuseColor(new Color3f(1.0f, 0.0f, 0.0f));
        torusMaterial.setSpecularColor(new Color3f(1.0f, 0.0f, 0.0f));
        torusApp.setMaterial(torusMaterial);

        GeometryInfo torusGeometry = createTorusGeometry(0.1f, 0.05f, 32, 16);
        Shape3D torusShape = new Shape3D(torusGeometry.getGeometryArray(), torusApp);

        Transform3D torusTransform = new Transform3D();
        torusTransform.setTranslation(new Vector3f(4.0f, 0.5f, -3.0f));
        TransformGroup torusTG = new TransformGroup(torusTransform);
        torusTG.addChild(torusShape);

        root.addChild(torusTG);
    }

    private GeometryInfo createTorusGeometry(float majorRadius, float minorRadius, int numMajor, int numMinor) {
        float[] coords = new float[3 * numMajor * numMinor];
        int[] stripCounts = new int[numMajor];
        int coordIndex = 0;

        double majorStep = 2.0 * Math.PI / numMajor;
        double minorStep = 2.0 * Math.PI / numMinor;

        for (int i = 0; i < numMajor; i++) {
            double a0 = i * majorStep;
            double x0 = majorRadius * Math.cos(a0);
            double y0 = majorRadius * Math.sin(a0);

            for (int j = 0; j < numMinor; j++) {
                double a1 = j * minorStep;
                double x1 = x0 + minorRadius * Math.cos(a1) * Math.cos(a0);
                double y1 = y0 + minorRadius * Math.cos(a1) * Math.sin(a0);
                double z1 = minorRadius * Math.sin(a1);

                coords[coordIndex++] = (float) x1;
                coords[coordIndex++] = (float) y1;
                coords[coordIndex++] = (float) z1;
            }
            stripCounts[i] = numMinor;
        }

        GeometryInfo torusGeometry = new GeometryInfo(GeometryInfo.TRIANGLE_STRIP_ARRAY);
        torusGeometry.setCoordinates(coords);
        torusGeometry.setStripCounts(stripCounts);

        NormalGenerator ng = new NormalGenerator();
        ng.generateNormals(torusGeometry);

        return torusGeometry;
    }

    private void handleKeyPress(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_S) {
            forwardKeyPressed = true;
        } else if (key == KeyEvent.VK_W) {
            backwardKeyPressed = true;
        } else if (key == KeyEvent.VK_A) {
            leftKeyPressed = true;
        } else if (key == KeyEvent.VK_D) {
            rightKeyPressed = true;
        }
    }

    private void handleKeyRelease(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_S) {
            forwardKeyPressed = false;
        } else if (key == KeyEvent.VK_W) {
            backwardKeyPressed = false;
        } else if (key == KeyEvent.VK_A) {
            leftKeyPressed = false;
        } else if (key == KeyEvent.VK_D) {
            rightKeyPressed = false;
        }
    }


    private void updateCharacterPosition() {
        Transform3D currentTransform = new Transform3D();
        characterTG.getTransform(currentTransform);

        Vector3f translationVector = new Vector3f();
        currentTransform.get(translationVector);

        float speed = 0.1f;

        Vector3f newPosition = new Vector3f(translationVector);

        if (forwardKeyPressed) {
            newPosition.z += speed;
        }

        if (backwardKeyPressed) {
            newPosition.z -= speed;
        }

        if (leftKeyPressed) {
            newPosition.x -= speed;
        }

        if (rightKeyPressed) {
            newPosition.x += speed;
        }


        if (!checkWallCollision(newPosition)) {
            currentTransform.setTranslation(newPosition);
            characterTG.setTransform(currentTransform);
        }


        Vector3f characterPosition = new Vector3f();
        characterTG.getTransform(currentTransform);
        currentTransform.get(characterPosition);

        Transform3D cameraTransform = new Transform3D();
        su.getViewingPlatform().getViewPlatformTransform().getTransform(cameraTransform);

        Vector3f cameraTranslationVector = new Vector3f();
        currentTransform.get(translationVector);
        cameraTransform.setTranslation(new Vector3f(translationVector.x, translationVector.y + 0.5f, translationVector.z + 2.5f));

        su.getViewingPlatform().getViewPlatformTransform().setTransform(cameraTransform);
    }
    private boolean checkWallCollision(Vector3f newPosition) {
        for (TransformGroup wallTG : listOfCubeTransformGroups) {
            Transform3D wallTransform = new Transform3D();
            wallTG.getTransform(wallTransform);
            Vector3f wallPosition = new Vector3f();
            wallTransform.get(wallPosition);

            float dx = newPosition.x - wallPosition.x;
            float dz = newPosition.z - wallPosition.z;


            float wallSize = 0.2f;

            if (Math.abs(dx) < wallSize && Math.abs(dz) < wallSize) {

                return true;
            }
        }


        return false;
    }
    private void handleMouseClick(MouseEvent e) {
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();

        if (result != null) {
            Node pickedNode = result.getNode(PickResult.SHAPE3D);

            if (pickedNode instanceof Shape3D) {
                Shape3D pickedShape = (Shape3D) pickedNode;
                Appearance pickedAppearance = pickedShape.getAppearance();

                if (pickedAppearance != null) {
                    Material pickedMaterial = pickedAppearance.getMaterial();

                    if (pickedMaterial != null) {
                        Color3f diffuseColor = new Color3f();
                        pickedMaterial.getDiffuseColor(diffuseColor);

                        if (diffuseColor.equals(new Color3f(0.0f, 0.0f, 1.0f))) { // Cor azul

                            Vector3f clickedLocation = getClickedLocation(result);
                            if (!clickedLocations.contains(clickedLocation)) {

                                clickedLocations.add(clickedLocation);


                                numCylindersClicked++;


                                updateCylinderCounter();
                            }
                        }
                    }
                }
            }
        }
    }
    private Vector3f getClickedLocation(PickResult result) {

        PickIntersection pickIntersection = result.getIntersection(0);


        if (pickIntersection != null) {

            Point3d intersectionPoint = pickIntersection.getPointCoordinates();

            // Converta para Vector3f
            Vector3f clickedLocation = new Vector3f((float) intersectionPoint.x, (float) intersectionPoint.y, (float) intersectionPoint.z);
            return clickedLocation;
        } else {

            return null;
        }
    }

    private void updateCylinderCounter() {

        JLabel label = new JLabel("Cilindros Clicados: " + numCylindersClicked);


        this.add(label, BorderLayout.SOUTH);
        if (numCylindersClicked == 5) {
            JOptionPane.showMessageDialog(this, "Parabéns, você ganhou!");

        }

        this.revalidate();
        this.repaint();
    }

}