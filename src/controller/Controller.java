package controller;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import model.data_structures.HashLinearProbing;
import model.data_structures.HashSeparateChaining;
import model.data_structures.ListaDoblementeEncadenada;
import model.data_structures.Node;
import model.data_structures.NodoHash;
import model.data_structures.NodoHash22;
import model.logic.Comparendo;
import model.logic.Modelo;
import view.View;

public class Controller {

	/* Instancia del Modelo*/
	private Modelo modelo;

	/* Instancia de la Vista*/
	private View view;

	/**
	 * Crear la vista y el modelo del proyecto
	 * @param capacidad tamaNo inicial del arreglo
	 */
	public Controller ()
	{
		view = new View();
		modelo = new Modelo();
	}



	public void run() 
	{
		Scanner lector = new Scanner(System.in);
		boolean fin = false;
		String respuesta = "";

		while( !fin ){
			view.printMenu();

			int option = lector.nextInt();
			switch(option){
			case 1:
				modelo = new Modelo();
				view.printMessage("Cargando los comparendos...");

				try {


					modelo.cargarGrafo();


				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case 2: 
				modelo.escribirJson();
				break;
			case 3: 

				modelo.leerJson();;
				break;	
			case 4: 

				modelo.dibujarTodin();
				break;	
			case 5: 
				view.printMessage("--------- \n Hasta pronto !! \n---------"); 
				lector.close();
				fin = true;
				break;	

			default: 
				view.printMessage("--------- \n Opcion Invalida !! \n---------");
				break;
			}
		}

	}	
}
