package manager;

import java.util.ArrayList;
import java.util.List;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

public class Manager {
	private ArrayList<Vid> videsVendimia = new ArrayList<>();

	private static Manager manager;
	private ArrayList<Entrada> entradas;
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;

	private Manager () {
		this.entradas = new ArrayList<>();
	}
	
	public static Manager getInstance() {
		if (manager == null) {
			manager = new Manager();
		}
		return manager;
	}
	
	private void createSession() {
		org.hibernate.SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    	session = sessionFactory.openSession();
	}

	public void init() {
		createSession();
		getEntrada();
		manageActions();
		showAllCampos();
		showCantidadVidByBodega();
		session.close();
	}

	private void manageActions() {
		for (Entrada entrada : this.entradas) {
			try {
				System.out.println(entrada.getInstruccion());
				switch (entrada.getInstruccion().toUpperCase().split(" ")[0]) {
					case "B":
						addBodega(entrada.getInstruccion().split(" "));
						break;
					case "C":
						addCampo(entrada.getInstruccion().split(" "));
						break;
					case "V":
						addVid(entrada.getInstruccion().split(" "));
						break;
					case "#":
						vendimia();
						break;
					default:
						System.out.println("Instruccion incorrecta");
				}
			} catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}

	private void vendimia() {
	    // Crear un nuevo ArrayList para almacenar temporalmente las vides recolectadas durante la vendimia
	    ArrayList<Vid> videsRecolectadas = new ArrayList<>();
	    
	    // Agregar las vides del campo actual al ArrayList
	    videsRecolectadas.addAll(this.c.getVids());
	    
	    // Agregar las vides del campo a la bodega
	    this.b.getVids().addAll(this.c.getVids());
	    
	    // Guardar cada vid recolectada durante la vendimia en la base de datos
	    tx = session.beginTransaction();
	    for (Vid vid : videsRecolectadas) {
	        session.saveOrUpdate(vid);
	    }
	    session.saveOrUpdate(b); // Guardar la bodega con las nuevas vides
	    tx.commit();
	}



	private void addVid(String[] split) {
	    // Crear una nueva vid con los datos proporcionados
	    Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]),Double.parseDouble(split[3]));

	    // Iniciar una transacción Hibernate
	    tx = session.beginTransaction();

	    // Guardar la nueva vid en la base de datos
	    session.save(v);

	    // Asociar la vid al campo actual
	    c.addVid(v);
	    session.save(c);

	    // Confirmar la transacción
	    tx.commit();
	}







	private void addCampo(String[] split) {
		c = new Campo(b);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(c);
		c = session.get(Campo.class, id);
		
		tx.commit();
	}

	private void addBodega(String[] split) {
		b = new Bodega(split[1]);
		tx = session.beginTransaction();
		
		int id = (Integer) session.save(b);
		b = session.get(Bodega.class, id);
		
		tx.commit();
		
	}

	private void getEntrada() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select e from Entrada e");
		this.entradas.addAll(q.list());
		tx.commit();
	}

	private void showAllCampos() {
		tx = session.beginTransaction();
		Query q = session.createQuery("select c from Campo c");
		List<Campo> list = q.list();
		for (Campo c : list) {
			System.out.println(c);
		}
		tx.commit();
	}
	
	private void showCantidadVidByBodega() {
        tx = session.beginTransaction();
        Query q = session.createQuery("select b, sum(v.cantidad) " +
                "from Bodega b " +
                "inner join b.vids v " +
                "group by b");
        List<Object[]> resultList = q.list();
        for (Object[] result : resultList) {
            Bodega bodega = (Bodega) result[0];
            Long cantidadVid = (Long) result[1];
            System.out.println("Bodega: " + bodega.getNombre() + ", Cantidad de Vid: " + cantidadVid);
        }
        tx.commit();
    }
}
	