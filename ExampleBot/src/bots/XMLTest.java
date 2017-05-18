package bots;

import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.beans.XMLDecoder;

public class XMLTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Node n0 = new Node();
		n0.value =0;
		Edge e0 = new Edge();
		e0.value = 0;
		Edge e1 = new Edge();
		e1.value = 1;
		Edge e2 = new Edge();
		e2.value = 2;
		Node n1 = new Node();
		n1.value = 1;
		Node n2 = new Node();
		n2.value = 2;
		Node n3 = new Node();
		n3.value = 3;
		e0.children.add(n1);
		e0.children.add(n2);
		e1.children.add(n2);
		e2.children.add(n3);
		n0.children.add(e0);
		n0.children.add(e1);
		n0.children.add(e2);
		
		
		try {
			XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream("XMLTEST")));
			encoder.writeObject(n0);
			encoder.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		try {
			XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream("XMLTEST")));
			Node xmlNode = (Node)decoder.readObject();
			Node test0 = null;
			Node test1 = null;
			for(Edge e : xmlNode.children){
				if(e.value == 0){
					for(Node n : e.children){
						if(n.value == 2){
							test0 = n;
						}
					}
				}
				if(e.value == 1){
					test1 = e.children.get(0);
				}
			}
			if(test0 == null || test1 == null){
				return;
			}
			System.out.println("test0.value = " + test0.value);
			System.out.println("test1.value = " + test1.value);
			System.out.println(test0.equals(test1));
			System.out.println(test0 == test1);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}
}
