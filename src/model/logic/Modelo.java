package model.logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.teamdev.jxmaps.swing.MapView;
import com.teamdev.jxmaps.*;
import com.teamdev.jxmaps.Polygon;

import javax.swing.*;
import java.awt.*;

import model.data_structures.*;

/**
 * Definicion del modelo del mundo
 *
 */
public class Modelo {
	/**
	 * Atributos del modelo del mundo
	 */

	/**
	 * Cola de lista encadenada.
	 */

	private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

	private static final Double LONGMIN =-74.094723;
	private static final Double LONGMAX =-74.062707;

	private static final Double LATMIN =4.597714;
	private static final Double LATMAX = 4.621360;

	private Grafo grafo;

	private Grafo grafojson;

	ListaDoblementeEncadenada<EstacionPolicia> estaciones;

	/**
	 * Constructor del modelo del mundo con capacidad predefinida
	 */
	public Modelo()
	{
		grafo= new Grafo();
		grafojson= new Grafo();
		estaciones= new ListaDoblementeEncadenada<EstacionPolicia>();
	}

	/**
	 * Carga el archivo .JSON en una lista enlazada.
	 * @throws FileNotFoundException. Si no encuentra el archivo.
	 */

	public void cargarGrafo() throws FileNotFoundException
	{
		//Definir mejor la entrada para el lector de json

		long inicio = System.currentTimeMillis();
		long inicio2 = System.nanoTime();
		String dirvertices= "./data/bogota_vertices.txt";
		String dirarcos= "./data/bogota_arcos.txt";
		String dir= "./data/estacionpolicia.geojson.json";


		File archivovertices= new File(dirvertices);
		FileReader f = new FileReader(archivovertices);
		BufferedReader b = new BufferedReader(f);

		File archivoarcos= new File(dirarcos);
		FileReader f2 = new FileReader(archivoarcos);
		BufferedReader b2 = new BufferedReader(f2);

		String cadena;

		String cadena1;
		try {

			while((cadena = b.readLine())!=null) {

				String[] propiedades= cadena.split(",");
				Integer id= Integer.parseInt(propiedades[0]);

				Double longitud= Double.parseDouble(propiedades[1]);
				Double latitud= Double.parseDouble(propiedades[2]);
				ListaDoblementeEncadenada coordenadas= new ListaDoblementeEncadenada();
				coordenadas.insertarFinal(longitud);
				coordenadas.insertarFinal(latitud);

				grafo.addVertex(id, coordenadas);

			}
			b.close();


			while((cadena1=b2.readLine())!=null) 
			{
				String[] arcos=cadena1.split(" ");
				if(arcos.length>1) 
				{
					Integer idprincipal=Integer.parseInt(arcos[0]);

					Double latitudinicial=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idprincipal)).darCabeza2().darSiguiente().darE();
					Double longitudinicial=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idprincipal)).darCabeza();

					for(int i=1;i<arcos.length;i++) 
					{
						// calcular haversine con el vertice siguiente y crear arco
						Integer idactual=Integer.parseInt(arcos[i]);
						Double latitudfinal=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idactual)).darCabeza2().darSiguiente().darE();
						Double longitudfinal=(Double)((ListaDoblementeEncadenada)grafo.getInfoVertex(idactual)).darCabeza();

						double distanciaconverticeactual = distance(latitudinicial, longitudinicial,latitudfinal,longitudfinal);

						grafo.addEdge2(idprincipal, idactual, distanciaconverticeactual);
					}

				}

			}
			b2.close();


		} 
		catch (IOException e) {

			e.printStackTrace();
		}

		File archivo= new File(dir);


		JsonReader reader= new JsonReader( new InputStreamReader(new FileInputStream(archivo)));
		JsonObject gsonObj0= JsonParser.parseReader(reader).getAsJsonObject();

		JsonArray estaciones=gsonObj0.get("features").getAsJsonArray();
		int i=0;
		while(i<estaciones.size())
		{
			JsonElement obj= estaciones.get(i);
			JsonObject gsonObj= obj.getAsJsonObject();

			JsonObject gsonObjpropiedades=gsonObj.get("properties").getAsJsonObject();


			int objid= gsonObjpropiedades.get("OBJECTID").getAsInt();
			String correo=gsonObjpropiedades.get("EPOCELECTR").getAsString();
			String direccion=gsonObjpropiedades.get("EPODIR_SITIO").getAsString();
			String descripcion=gsonObjpropiedades.get("EPODESCRIP").getAsString();

			Double longitud= gsonObjpropiedades.get("EPOLONGITU").getAsDouble();
			Double latitud= gsonObjpropiedades.get("EPOLATITUD").getAsDouble();

			EstacionPolicia agregar=new EstacionPolicia(objid,longitud,latitud ,direccion, descripcion, correo);
			this.estaciones.insertarFinal(agregar);
			i++;
		}



		long fin2 = System.nanoTime();
		long fin = System.currentTimeMillis();

		System.out.println((fin2-inicio2)/1.0e9 +" segundos, de la carga de datos normal.");
		System.out.println("Numero de vertices: "+grafo.V());
		System.out.println("Numero de arcos: "+grafo.E());
	}


	public void escribirJson() 
	{
		JSONObject obj = new JSONObject();
		obj.put("type", "Feature Collection");

		JSONArray vertices = new JSONArray();

		NodoHash22<Integer,ListaDoblementeEncadenada<Vertice>>[] verticesnodos= grafo.getNodos().getNodosSet();
		int num=0;
		System.out.println("tamano total: "+grafo.getNodos().getTamTotal());
		System.out.println("tamano actual: "+grafo.getNodos().getTamActual());
		for(int i=0;i<verticesnodos.length;i++) 

		{
			if(verticesnodos[i]!=null) {
				num++;
				Vertice actual=verticesnodos[i].darv().darCabeza(); 

				JSONObject verticeactual = new JSONObject();

				Double latitud=(Double)((ListaDoblementeEncadenada)actual.getValue()).darCabeza2().darSiguiente().darE();
				Double longitud=(Double)((ListaDoblementeEncadenada)actual.getValue()).darCabeza();

				verticeactual.put("IDINICIAL", actual.getKey());
				verticeactual.put("LONGITUD", longitud);
				verticeactual.put("LATITUD", latitud);

				JSONArray arcos = new JSONArray();
				ListaDoblementeEncadenada<Arco> arcoslista=actual.darListaArcos();

				Node<Arco> a= arcoslista.darCabeza2();

				while(a!=null) {

					JSONObject arcoactual = new JSONObject();
					arcoactual.put("IDFINAL", a.darE().getvFinal().getKey());
					arcoactual.put("COSTO", a.darE().getCosto());
					arcos.add(arcoactual);
					a=a.darSiguiente();
				}

				verticeactual.put("ARCOS", arcos);

				vertices.add(verticeactual);
			}
		}

		obj.put("features", vertices);


		try {

			FileWriter filesalida = new FileWriter("./data/grafopersistido.json");
			filesalida.write(obj.toJSONString());
			filesalida.flush();
			filesalida.close();

		} catch (IOException e) {
			//manejar error
		}
		System.out.println(num);
	}


	public void leerJson() 
	{
		grafojson= new Grafo();
		File archivo= new File("./data/grafopersistido.json");
		JsonReader reader=null;
		try {
			reader = new JsonReader( new InputStreamReader(new FileInputStream(archivo)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JsonObject gsonObj0= JsonParser.parseReader(reader).getAsJsonObject();

		JsonArray vertices=gsonObj0.get("features").getAsJsonArray();
		int i=0;
		System.out.println(vertices.size());
		while(i<vertices.size())
		{
			JsonElement obj= vertices.get(i);
			JsonObject verticeactual= obj.getAsJsonObject();
			Double longitud=verticeactual.get("LONGITUD").getAsDouble();
			Double latitud=verticeactual.get("LATITUD").getAsDouble();
			Integer idvertice=verticeactual.get("IDINICIAL").getAsInt();

			JsonArray arcos=verticeactual.get("ARCOS").getAsJsonArray();
			ListaDoblementeEncadenada posicion= new ListaDoblementeEncadenada();
			posicion.insertarFinal(longitud);
			posicion.insertarFinal(latitud);
			grafojson.addVertex(idvertice, posicion);
			++i;
		}


		int j=0;
		while(j<vertices.size())
		{
			JsonElement obj= vertices.get(j);
			JsonObject verticeactual= obj.getAsJsonObject();

			JsonArray arcos=verticeactual.get("ARCOS").getAsJsonArray();
			Integer idvertice=verticeactual.get("IDINICIAL").getAsInt();
			for(int x=0;x<arcos.size();x++) 
			{
				JsonObject arcoactual=arcos.get(x).getAsJsonObject();

				Integer idfinal=arcoactual.get("IDFINAL").getAsInt();
				Double costoactual=arcoactual.get("COSTO").getAsDouble();

				Arco nuevoarco= new Arco(grafojson.getVertex(idvertice),grafojson.getVertex(idfinal),costoactual);
				grafojson.addEdge(idvertice, idfinal, costoactual);

			}
			++j;
		}

		System.out.println("Numero de vertices: "+grafojson.V());
		System.out.println("Numero de arcos: "+grafojson.E());


	}

	public void dibujarTodin() 
	{
		JFrame frame= new JFrame("Grafito");

		class MapExample extends MapView {

			public MapExample() {

				// Setting of a ready handler to MapView object. onMapReady will be called when map initialization is done and
				// the map object is ready to use. Current implementation of onMapReady customizes the map object.
				setOnMapReadyHandler(new MapReadyHandler() {
					@Override
					public void onMapReady(MapStatus status) {
						if (status == MapStatus.MAP_STATUS_OK) {

							final Map map = getMap();

							MapOptions options = new MapOptions();

							MapTypeControlOptions controlOptions = new MapTypeControlOptions();

							options.setMapTypeControlOptions(controlOptions);

							map.setOptions(options);

							map.setCenter(new LatLng(4.609537, -74.078715));

							map.setZoom(15.0);

							ListaDoblementeEncadenada<Arco> arcos=grafojson.getList();
							
							
							Node<EstacionPolicia> actualpol= estaciones.darCabeza2();
							while(actualpol!=null) 
							{
								Double longitud=actualpol.darE().getLongitud();
								Double latitud=actualpol.darE().getLatitud();
								
								if(longitud<=LONGMAX && longitud>=LONGMIN && latitud<=LATMAX && latitud>=LATMIN) 
								{
									LatLng poli=new LatLng(latitud,longitud);
									
									Circle pol=new Circle(map);
									pol.setCenter(poli);
									pol.setRadius(20);
									CircleOptions co= new CircleOptions();
									co.setFillColor("#CB3234");
									pol.setOptions(co);
									pol.setVisible(true);
									
								}
								actualpol=actualpol.darSiguiente();
							}
							
							Node<Arco> actual= arcos.darCabeza2();
							while(actual!=null) 
							{
								ListaDoblementeEncadenada<Double> vertice1coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvInicio().getValue();
								ListaDoblementeEncadenada<Double> vertice2coor=(ListaDoblementeEncadenada<Double>)actual.darE().getvFinal().getValue();

								if(vertice1coor.darCabeza()<=LONGMAX && vertice1coor.darCabeza()>=LONGMIN && vertice2coor.darCabeza()<=LONGMAX && vertice2coor.darCabeza()>=LONGMIN && (Double)vertice1coor.darCabeza2().darSiguiente().darE()<=LATMAX && (Double)vertice1coor.darCabeza2().darSiguiente().darE()>=LATMIN && (Double)vertice2coor.darCabeza2().darSiguiente().darE()<=LATMAX && (Double)vertice2coor.darCabeza2().darSiguiente().darE()>=LATMIN) 
								{
									LatLng ver1=new LatLng((double) vertice1coor.darCabeza2().darSiguiente().darE(),vertice1coor.darCabeza());
									LatLng ver2=new LatLng((double) vertice2coor.darCabeza2().darSiguiente().darE(),vertice2coor.darCabeza());

									Circle ver11=new Circle(map);
									ver11.setCenter(ver1);
									ver11.setRadius(1);
									CircleOptions co= new CircleOptions();
									co.setFillColor("#008F39");
									ver11.setOptions(co);
									ver11.setVisible(true);

									Circle ver22=new Circle(map);
									ver22.setCenter(ver2);
									ver22.setRadius(1);
									CircleOptions co1= new CircleOptions();
									co1.setFillColor("#008F39");
									ver22.setOptions(co1);
									ver22.setVisible(true);

									LatLng[] camino= new LatLng[2];
									camino[0]=ver1;
									camino[1]=ver2;
									Polygon linea= new Polygon(map);
									linea.setPath(camino);
									linea.setVisible(true);
								}

								actual=actual.darSiguiente();
							}	





						}
					}
				});

			}

		}

		MapExample map1 = new MapExample();
		frame.add( map1,BorderLayout.CENTER);
		frame.setSize(700, 500);
		frame.setVisible(true);

	}


	public static double distance(double startLat, double startLong,
			double endLat, double endLong) {

		double dLat  = Math.toRadians((endLat - startLat));
		double dLong = Math.toRadians((endLong - startLong));

		startLat = Math.toRadians(startLat);
		endLat   = Math.toRadians(endLat);

		double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return EARTH_RADIUS * c; // <-- d
	}

	public static double haversin(double val) {
		return Math.pow(Math.sin(val / 2), 2);
	}

	private boolean less(Comparable v,Comparable w)
	{
		return v.compareTo(w) < 0;
	}

	private void exch(Comparable[] datos,int i, int j)
	{
		Comparable t=datos[i];
		datos[i]=datos[j];
		datos[j]=t;
	}








}

