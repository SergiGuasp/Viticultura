package manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Projections;

import model.Bodega;
import model.Campo;
import model.Entrada;
import model.Vid;
import utils.TipoVid;

public class Manager {

	private static Manager manager;
	ArrayList<Entrada> entradas = new ArrayList<>();
	private Session session;
	private Transaction tx;
	private Bodega b;
	private Campo c;
	MongoCollection<Document> collection;
	MongoDatabase database;
	
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
    	
    	
    	String uri = "mongodb://localhost:27017";
    	MongoClientURI mongoClientUri = new MongoClientURI(uri);
    	MongoClient mongoClient = new MongoClient(mongoClientUri);
    	database = mongoClient.getDatabase("dam2tm06uf2p2");
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
				String instruccion = entrada.getInstruccion();
				if (instruccion != null) {
				    switch (instruccion.toUpperCase().split(" ")[0]) {
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
			} }catch (HibernateException e) {
				e.printStackTrace();
				if (tx != null) {
					tx.rollback();
				}
			}
		}
	}

	private void vendimia() {
	   /*if (this.c != null) {
		this.b.getVids().addAll(this.c.getVids());
	   }
		tx = session.beginTransaction();
		session.save(b);

		tx.commit();*/
	}

	
	
	public void addVid(String[] split) {
		Vid v = new Vid(TipoVid.valueOf(split[1].toUpperCase()), Integer.parseInt(split[2]),Double.parseDouble(split[3]));
		collection = database.getCollection("campo");
		Document lastVineyard = collection.find().sort(new Document("_id", -1)).first();
		collection = database.getCollection("vid");
		Document document = new Document().append("type", v.getVid().toString()).append("quantity", v.getCantidad()).append("campo", lastVineyard);
		collection.insertOne(document);
		
		Document document2 = new Document().append("type", v.getVid().toString()).append("quantity", v.getCantidad());
		collection = database.getCollection("campo");
		
		Document update = new Document("$push", new Document("vid", document2));
		collection.updateOne(lastVineyard, update);
	}
	
	


	
	public void addCampo(String[] split) {
		collection = database.getCollection("bodega");
		Document lastWinery = collection.find().sort(new Document("_id", -1)).first();
		collection = database.getCollection("campo");
		Document documento = new Document().append("collected", false).append("winery", lastWinery);
		collection.insertOne(documento);
	}
	
	

	
	public void addBodega(String[] split) {
		b = new Bodega(split[1]);
		collection = database.getCollection("bodega");
		Document document = new Document().append("name", b.getNombre());
		collection.insertOne(document);
	}

	public void getEntrada() {
		collection = database.getCollection("entrada");
		
		
		for(Document document : collection.find()) {
			
			Entrada entrada = new Entrada();
			entrada.setInstruccion(document.getString("action"));
			if(entrada != null) {
				entradas.add(entrada);
				System.out.println(entrada);
			}
		}
	}

	private void showAllCampos() {
	    MongoCollection<Document> campoCollection = database.getCollection("campo");
	    
	    FindIterable<Document> cursor = campoCollection.find();
	    
	    for (Document campoDoc : cursor) {
	        System.out.println(campoDoc.toJson());
	    }
	}
	
	public void showCantidadVidByBodega() {
        Bson groupStage = Aggregates.group(
                "$winery.name",
                Accumulators.sum("totalQuantity", "$quantity")
        );

        Bson projectStage = Aggregates.project(
                Projections.fields(
                        Projections.excludeId(),
                        Projections.include("bodega", "_id"),
                        Projections.computed("totalQuantity", "$totalQuantity")
                )
        );

        List<Document> results = collection.aggregate(Arrays.asList(groupStage, projectStage))
                .allowDiskUse(true)
                .into(new ArrayList<>());

        for (Document doc : results) {
            System.out.println(doc.toJson());
        }
    }
}
	