package Server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Player {
	private String name;
	private Typ typ; //X or O
	private ObjectOutputStream writer; //stream to send data to client
	private ObjectInputStream reader; //stream to receive data from client
	

	public Player(String name) {
		super();
		this.name = name;
	}
	
	public Player(ObjectOutputStream writer, ObjectInputStream reader) {
		super();
		this.writer = writer;
		this.reader = reader;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Typ getTyp() {
		return typ;
	}

	public void setTyp(Typ typ) {
		this.typ = typ;
	}

	public ObjectOutputStream getWriter() {
		return writer;
	}

	public void setWriter(ObjectOutputStream writer) {
		this.writer = writer;
	}

	public ObjectInputStream getReader() {
		return reader;
	}

	public void setReader(ObjectInputStream reader) {
		this.reader = reader;
	}
	
}
