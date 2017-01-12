package zln.shadowdemo.Render;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zln.shadowdemo.Utils.Constant;
import zln.shadowdemo.Utils.Index;
import zln.shadowdemo.Utils.Point;
import zln.shadowdemo.Utils.Vec2;
import zln.shadowdemo.Utils.Vec3;

/**
 * Created by zln on 04/01/2017.
 */
public class LoadModel {

    private static final String TAG = "LoadModel";
    private final List<Vec3> vertices = new ArrayList<>();
    private final List<Vec3> normals = new ArrayList<>();
    private final List<Vec2> textures = new ArrayList<>();
    private final List<Point> points = new ArrayList<>();
    private final List<Index> indexes = new ArrayList<>();

    private final List<String> names = new ArrayList<>();
    private final List<String> texFilenames = new ArrayList<>();
    private final List<Vec3> diffuses = new ArrayList<>();
    private final List<Vec3> speculars = new ArrayList<>();
    private final List<Index> indices = new ArrayList<>();


    private Map<String, Integer> map = new HashMap<>();

    private int num = 0;
    private List<Integer> start = new ArrayList<>();
    private List<Integer> count = new ArrayList<>();


    private Vec3 minPoint = new Vec3(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
    private Vec3 maxPoint = new Vec3(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

    public LoadModel(InputStream objInputStream, InputStream mtlInputStream) throws Exception {
        parseMTL(mtlInputStream);
        parseOBJ(objInputStream);

    }

    public List<Integer> getCount() {
        return count;
    }

    public List<Vec3> getDiffuses() {
        return diffuses;
    }

    public List<Vec3> getSpeculars() {
        return speculars;
    }

    public List<String> getTexFilenames() {
        return texFilenames;
    }

    public List<Integer> getStart() {
        return start;
    }

    public int getNum() {
        //Log.v("zln-tex: ", num+"");
        return num;
    }

    public void initIndices(List<Index> indexes){
        for(int k = 0; k < num; k ++) {
            int first = 0;
            int cnt = 0;
            if(k == 0){
                start.add(first);
            }else{
                start.add(start.get(k-1) + count.get(k-1));
            }
            for (int i = 0; i < indexes.size(); i++) {

                if(indexes.get(i).id == k){
                    indices.add(indexes.get(i));
                    cnt ++;
                }
            }
            count.add(cnt * 3);
        }
        //Log.v("zln-----", indices.size() +"");
    }

    public int getIndexNumber() {
        return indexes.size();
    }

    public void parseOBJ(InputStream objInputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(objInputStream));
        String line;
        int m_id = -1;
        List<String[]> tempFaceTokens = new ArrayList<>();
        List<Integer> material_ids = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split(" ");
            switch (tokens[0]) {
                case "v":
                    final Vec3 position = new Vec3(Arrays.copyOfRange(tokens, 1, tokens.length));
                    vertices.add(position);
                    updateMaxMin(position);
                    break;
                case "vn":
                    normals.add(new Vec3(Arrays.copyOfRange(tokens, 1, tokens.length)).normalize());
                    break;
                case "vt":
                    textures.add(new Vec2(Arrays.copyOfRange(tokens, 1, tokens.length)));
                    break;
                case "usemtl":
                    String s = tokens[1];
                    //Log.v("zln-tex:", tokens[1]);

                    for(int i = 0; i < num; i ++){
                        if(s.equals(names.get(i))){
                            m_id = i;
                            //Log.v("zln-tex:", m_id + "");

                        }
                    }
                    break;
                case "f":
                    tempFaceTokens.add(Arrays.copyOfRange(tokens, 1, tokens.length));
                    material_ids.add(m_id);
                    break;
                default:
                    Log.e(TAG, "unknown element: " + tokens[0]);
            }
        }
        //归一model到(-1,1)
        normalizedPosition();

        parseFace(tempFaceTokens, material_ids);

        initIndices(indexes);

    }

    private void updateMaxMin(Vec3 position) {
        minPoint = minPoint.min(position);
        maxPoint = maxPoint.max(position);
    }

    private void normalizedPosition() {
        Vec3 length = maxPoint.subtract(minPoint);
        Float d = length.maxComponent() / 2;
        //length = length.div(d);
        d *= 1.5f;
        Vec3 centre = minPoint.mid(maxPoint);
        for (int i = 0; i < vertices.size(); i++) {
            vertices.set(i, vertices.get(i).subtract(centre).div(d));
        }
    }

    private void parseFace(List<String[]> Tokens, List<Integer> m_ids) {
        for (int k = 0; k < Tokens.size(); k ++) {
            for(int i = 0 ; i < Tokens.get(k).length - 2; i ++){
                Index index = new Index();
                index.id = m_ids.get(k);
                //Log.v("zln-tex:", index.id +"");

                getPoint(Tokens.get(k)[0], 1 ,index);
                getPoint(Tokens.get(k)[i+1], 2, index);
                getPoint(Tokens.get(k)[i+2], 3, index);
                indexes.add(index);
            }
        }
    }

    private void getPoint(String pointString, int key, Index index) {
        Integer id = map.get(pointString);

        if(id == null) {
            id = map.size();
            map.put(pointString, map.size());
            String[] indexes = pointString.split("/");
            Point point = new Point();

            point.setPosition(vertices.get(Integer.parseInt(indexes[0]) - 1));
            if (indexes[1].length() != 0) {
                point.setTexture(textures.get(Integer.parseInt(indexes[1]) - 1));
            } else {
                point.setTexture(new Vec2(0f, 0f));
            }
            if (indexes.length == 3) {
                point.setNormal(normals.get(Integer.parseInt(indexes[2]) - 1));
            }
            points.add(point);

        }
        if(key == 1)
            index.a = id;
        if(key == 2)
            index.b = id;
        if(key == 3)
            index.c = id;

    }

    public void parseMTL(InputStream mtlInputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mtlInputStream));
        String line;
        int i = 0;
        int j = 0;
        int k = 0;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0 || line.startsWith("#")) {
                continue;
            }
            String[] tokens = line.split(" ");
            switch (tokens[0]) {
                case "newmtl":
                    names.add(tokens[1]);
                    num++;
                    break;
                case "Kd":
                    while(i + 1 < num){
                        diffuses.add(new Vec3(0.0f, 0.0f, 0.0f));
                    }
                    diffuses.add(new Vec3(Arrays.copyOfRange(tokens, 1, tokens.length)));
                    i++;
                    break;
                case "Ks":
                    while(j + 1 < num){
                        speculars.add(new Vec3(0.0f, 0.0f, 0.0f));
                    }
                    speculars.add(new Vec3(Arrays.copyOfRange(tokens, 1, tokens.length)));
                    j++;
                    break;
                case "map_Kd":
                    while(k + 1 < num){
                        texFilenames.add("");
                    }
                    texFilenames.add(tokens[1]);
                    k++;
                    break;
                default:
                    Log.e(TAG, "no use element: " + tokens[0]);
            }
        }
        while(i < num){
            diffuses.add(new Vec3(0.0f, 0.0f, 0.0f));
            i++;
        }
        while(j < num){
            speculars.add(new Vec3(0.0f, 0.0f, 0.0f));
            j++;
        }
        Log.v("zln-tex: ", speculars.size() + "");
        while(k < num){
            texFilenames.add("");
            k++;
        }
        Log.v("zln-tex: ", texFilenames.size() + "");

    }

    public ByteBuffer getVertexes() {
//        for(int i = 0; i < points.size(); i ++){
//            Log.v("zln- vertex:", points.get(i).getPosition().x + " " + points.get(i).getPosition().y + " " + points.get(i).getPosition().z);
//        }
        ByteBuffer bb = Constant.genDirectBuffer(points.size() * Point.SIZE_AS_BYTE);
        for (Point p : points) {
            bb.put(p.toByteBuffer());
        }
        bb.flip();
        return bb;
    }

    public ByteBuffer getIndices() {
//        for(int i = 0; i < indices.size(); i ++){
//            Log.v("zln- index:", indices.get(i).a + " " + indices.get(i).b + " " + indices.get(i).c);
//
//        }
        ByteBuffer bb = Constant.genDirectBuffer(indices.size() * Index.SIZE_AS_BYTE);
        for (Index i : indices) {
            bb.put(i.toByteBuffer());
        }
        bb.flip();
        return bb;
    }
}
