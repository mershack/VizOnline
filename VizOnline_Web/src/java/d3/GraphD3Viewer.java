/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package d3;

import Graph.Graph;
import Graph.GraphData;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import properties.PDouble;
import properties.PFile;
import properties.Property;

/**
 *
 * @author rajin
 */
public class GraphD3Viewer extends D3Viewer {
    private Graph graph;
    public GraphD3Viewer(String name, GraphData graphData)            
    {
        super(name);
        this.graph = graphData.graph;
        try {

            Property<PFile> p33 = new Property<PFile>("Load Positions", new PFile());
            this.addProperty(p33);

            Property<PDouble> p = new Property<PDouble>("Simulation.SPRING_LENGTH", new PDouble(300.));
            this.addProperty(p);

           

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject updateData()
    {
        JSONObject graphJSON = new JSONObject();
        ArrayList<String> nodes = this.graph.getNodes();
        ArrayList<Integer> e1 = new ArrayList<Integer>();
        ArrayList<Integer> e2 = new ArrayList<Integer>();
        this.graph.getEdgesAsIndeces(e1, e2);
        
        JSONArray nodeArray = new JSONArray();
        for(String node: nodes)
        {
            JSONObject obj = new JSONObject();
            obj.put("name", node);
            nodeArray.put(obj);
        }
        graphJSON.put("nodes", nodeArray);
        
        JSONArray linkArray = new JSONArray();
        for(int i=0;i<e1.size();i++)
        {
            int source = e1.get(i).intValue();
            int destination = e2.get(i).intValue();
            JSONObject obj = new JSONObject();
            obj.put("source", source);
            obj.put("target", destination);
            linkArray.put(obj); 
        }
        graphJSON.put("links", linkArray);
        return graphJSON;
    }
   
}
