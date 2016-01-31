

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

/**
 * Clase que controla el funcionamiento del Chatbot
 *
 */
@SuppressWarnings("serial")
public class Chatbot extends JApplet implements BaseConocimiento {

	private String sEntrada = new String("");
	private String sRespuesta = new String("");
	private String sEntradaAnterior = new String("");
	private String sRespuestaAnterior = new String("");
	private String sEvento = new String("");
	private String sEventoPrevio = new String("");
	private String sCopiaEntrada = new String("");
	private String sSujeto = new String("");
	private String sPalabra = new String("");
	private boolean salirPrograma = false;
	private boolean salida = false;

	final int maxEntrada = 1;
	final int maxResp = 6;
	final String delim = "?!.;,";
	
	// Variables para la interfaz
	JPanel panel = new JPanel();
	JTextPane textpane = new JTextPane();
    Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);
	JScrollPane scrollpane = new JScrollPane(textpane);
	JTextField textfield = new JTextField("");
	StyledDocument doc = textpane.getStyledDocument();
	SimpleAttributeSet key = new SimpleAttributeSet();
	
	
	public Chatbot() {
		//Frame
		panel.setBackground(Color.BLACK);
		textpane.setEditable(false);
		setLayout(new BorderLayout());
        scrollpane.setBorder(border);

		//TextField
        textfield.setBorder(border);
		textfield.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				sEntrada = textfield.getText();
				try {
					doc.insertString(doc.getLength(), "TÚ:\t" + sEntrada + "\n", null);
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				textfield.setText("");
				salida = true;
			}
		});
		
		//Cambiar el caret para ajustar la barra de scroll siempre al final
		DefaultCaret caret = (DefaultCaret)textpane.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		//Panel
		panel.setLayout(new BorderLayout());
		panel.add(scrollpane, BorderLayout.CENTER);
		panel.add(textfield, BorderLayout.PAGE_END);
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		// Añadir componentes
		add(panel);
		
		//Iniciar
		Thread inicioHilo = new Thread(new Runnable() {
			@Override
			public void run() {
				inicio();
			}
	    }); 
	    inicioHilo.start();
	}
	
	private void inicio() {
		try {
			iniciar();
			while (!salir()) {
				getEntrada();
				respuesta();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private Vector<String> listaRespuestas = new Vector<String>(maxResp);

	public void getEntrada() throws Exception {
		// Guarda la entrada anterior
		guardarEntradaAnterior();
		
		//BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
		//sEntrada = entrada.readLine();
		while(!salida){
			try {
				Thread.sleep(60);
			} catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		preprocesoEntrada();
		salida = false;
	}

	public void respuesta() {
		guardarRespuestaAnterior();
		asignarEvento("BOT UNDERSTAND**");

		if (entradaNula()) {
			controlarEvento("NULL INPUT**");
		} else if (repeticionEntradaNula()) {
			controlarEvento("NULL INPUT REPETITION**");
		} else if (usarioRepite()) {
			controlRepeticionUsuario();
		} else {
			buscarCasamiento();
		}

		if (usarioSalir()) {
			salirPrograma = true;
		}

		if (!botEntiende()) {
			controlarEvento("BOT DON'T UNDERSTAND**");
		}

		if (listaRespuestas.size() > 0) {
			selecRespuesta();

			if (botRepite()) {
				controlRepeticion();
			}
			mostrarRespuesta();
		}
	}

	public boolean salir() {
		return salirPrograma;
	}

	// Se busca la entrada del usuario
	// en la base de datos del programa
	public void buscarCasamiento() {
		listaRespuestas.clear();
		// Se introduce el nuevo String para ayudar
		// a la implementación del ranking de palabras
		// en el proceso de combinación (matching)
		String mejorPalabra = "";
		Vector<Integer> vectorIndices = new Vector<Integer>(maxResp);

		for (int i = 0; i < baseConocimiento.length; ++i) {
			String[] listaPalabras = baseConocimiento[i][0];

			for (int j = 0; j < listaPalabras.length; ++j) {
				String palabra = listaPalabras[j];
				// Se inserta un espacio antes y después
				// de la palabra clave para mejorar el proceso de matching
				palabra = insertarEspacios(palabra);

				if (sEntrada.indexOf(palabra) != -1) {
					if (palabra.length() > mejorPalabra.length()) {
						mejorPalabra = palabra;
						vectorIndices.clear();
						vectorIndices.add(i);
					} else if (palabra.length() == mejorPalabra.length()) {
						vectorIndices.add(i);
					}
				}
			}
		}
		if (vectorIndices.size() > 0) {
			sPalabra = mejorPalabra;
			Collections.shuffle(vectorIndices);
			int indiceRespuesta = vectorIndices.elementAt(0);
			int tamanyoResp = baseConocimiento[indiceRespuesta][1].length;
			for (int j = 0; j < tamanyoResp; ++j) {
				listaRespuestas.add(baseConocimiento[indiceRespuesta][1][j]);
			}
		}
	}

	void preprocesoRespuesta() {
		if (sRespuesta.indexOf("*") != -1) {
			// Se extrae desde la entrada
			buscarSujeto();
			// Se conjuga el sujeto
			sSujeto = transponer(sSujeto);

			sRespuesta = sRespuesta.replaceFirst("*", sSujeto);
		}
	}

	void buscarSujeto() {
		sSujeto = ""; // Se resetea la variable del sujeto
		StringBuffer buffer = new StringBuffer(sEntrada);
		buffer.deleteCharAt(0);
		sEntrada = buffer.toString();
		int pos = sEntrada.indexOf(sPalabra);
		if (pos != -1) {
			sSujeto = sEntrada.substring(pos + sPalabra.length() - 1,
					sEntrada.length());
		}
	}

	// Se implementa la transposición de frases
	public String transponer(String str) {
		boolean transpuesto = false;
		for (int i = 0; i < listaTranspuestos.length; ++i) {
			String first = listaTranspuestos[i][0];
			insertarEspacios(first);
			String second = listaTranspuestos[i][1];
			insertarEspacios(second);

			String backup = str;
			str = str.replaceFirst(first, second);
			if (str != backup) {
				transpuesto = true;
			}
		}

		if (!transpuesto) {
			for (int i = 0; i < listaTranspuestos.length; ++i) {
				String first = listaTranspuestos[i][0];
				insertarEspacios(first);
				String second = listaTranspuestos[i][1];
				insertarEspacios(second);
				str = str.replaceFirst(first, second);
			}
		}
		return str;
	}

	public void controlRepeticion() {
		if (listaRespuestas.size() > 0) {
			listaRespuestas.removeElementAt(0);
		}
		if (ningunaRespuesta()) {
			guardarEntrada();
			asignarEntrada(sEvento);

			buscarCasamiento();
			restaurarEntrada();
		}
		selecRespuesta();
	}

	public void controlRepeticionUsuario() {
		if (mismaEntrada()) {
			controlarEvento("REPETITION T1**");
		} else if (similarEntrada()) {
			controlarEvento("REPETITION T2**");
		}
	}

	public void controlarEvento(String str) {
		guardarEventoAnterior();
		asignarEvento(str);

		guardarEntrada();
		str = insertarEspacios(str);

		asignarEntrada(str);

		if (!mismoEvento()) {
			buscarCasamiento();
		}

		restaurarEntrada();
	}

	public void iniciar() {
		controlarEvento("SIGNON**");
		selecRespuesta();
		mostrarRespuesta();
	}

	public void selecRespuesta() {
		Collections.shuffle(listaRespuestas);
		sRespuesta = listaRespuestas.elementAt(0);
	}

	public void guardarEntradaAnterior() {
		sEntradaAnterior = sEntrada;
	}

	public void guardarRespuestaAnterior() {
		sRespuestaAnterior = sRespuesta;
	}

	public void guardarEventoAnterior() {
		sEventoPrevio = sEvento;
	}

	public void asignarEvento(String str) {
		sEvento = str;
	}

	public void guardarEntrada() {
		sCopiaEntrada = sEntrada;
	}

	public void asignarEntrada(String str) {
		sEntrada = str;
	}

	public void restaurarEntrada() {
		sEntrada = sCopiaEntrada;
	}

	public void mostrarRespuesta() {
		if (sRespuesta.length() > 0) {
			try {
				doc.insertString(doc.getLength(), "GAMERBOT:\t" + sRespuesta + "\n" , key);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
			textpane.repaint();
		}
	}

	public void preprocesoEntrada() {
		sEntrada = formatearString(sEntrada);
		sEntrada = sEntrada.toUpperCase();
		sEntrada = insertarEspacios(sEntrada);
	}

	public boolean botRepite() {
		return (sRespuestaAnterior.length() > 0 && sRespuesta == sRespuestaAnterior);
	}

	public boolean usarioRepite() {
		return (sEntradaAnterior.length() > 0 && ((sEntrada == sEntradaAnterior)
				|| (sEntrada.indexOf(sEntradaAnterior) != -1) || (sEntradaAnterior
					.indexOf(sEntrada) != -1)));
	}

	public boolean botEntiende() {
		return listaRespuestas.size() > 0;
	}

	public boolean entradaNula() {
		return (sEntrada.length() == 0 && sEntradaAnterior.length() != 0);
	}

	public boolean repeticionEntradaNula() {
		return (sEntrada.length() == 0 && sEntradaAnterior.length() == 0);
	}

	public boolean usarioSalir() {
		return sEntrada.indexOf("BYE") != -1;
	}

	public boolean mismoEvento() {
		return (sEvento.length() > 0 && sEvento == sEventoPrevio);
	}

	public boolean ningunaRespuesta() {
		return listaRespuestas.size() == 0;
	}

	public boolean mismaEntrada() {
		return (sEntrada.length() > 0 && sEntrada == sEntradaAnterior);
	}

	public boolean similarEntrada() {
		return (sEntrada.length() > 0 && (sEntrada.indexOf(sEntradaAnterior) != -1 || sEntradaAnterior
				.indexOf(sEntrada) != -1));
	}

	boolean isPuntuacion(char ch) {
		return delim.indexOf(ch) != -1;
	}

	// Se elimina la puntuación y los espacios
	// extra de la entrada del usuario.
	String formatearString(String str) {
		StringBuffer aux = new StringBuffer(str.length());
		char charPrevio = 0;
		for (int i = 0; i < str.length(); ++i) {
			if ((str.charAt(i) == ' ' && charPrevio == ' ')
					|| !isPuntuacion(str.charAt(i))) {
				aux.append(str.charAt(i));
				charPrevio = str.charAt(i);
			} else if (charPrevio != ' ' && isPuntuacion(str.charAt(i))) {
				aux.append(' ');
			}
		}
		return aux.toString();
	}

	String insertarEspacios(String str) {
		StringBuffer aux = new StringBuffer(str);
		aux.insert(0, ' ');
		aux.insert(aux.length(), ' ');
		return aux.toString();
	}
}