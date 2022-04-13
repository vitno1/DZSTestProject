
import entity.Equipment;
import entity.Well;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class WellMenu implements Menu {

    SessionFactory sessionFactory = new Configuration()
            .configure("hibernate.cfg.xml")
            .addAnnotatedClass(Well.class)
            .addAnnotatedClass(Equipment.class)
            .buildSessionFactory();
    Session session = null;

    public static void main(String[] args) throws IOException {
        WellMenu wm = new WellMenu();
        wm.packToXml("MyFirstXml");

    }

    @Override
    public void addWellWithEquipment(String well, int sumEquipmentsToAdd) {
        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            List<Well> list = session.createQuery("from Well " + "where name = \'" + well + "\'").getResultList();
            boolean isEmpty = list.isEmpty();
            if (isEmpty) {
                Well newWell = new Well(well);
                for (int i = 0; i < sumEquipmentsToAdd; i++) {
                    Equipment equipment = new Equipment();
                    newWell.addEquipmentToWell(equipment);
                    if (i == sumEquipmentsToAdd - 1) {
                        session.save(newWell);
                        session.getTransaction().commit();
                    }
                }
                session = sessionFactory.openSession();
                session.beginTransaction();
                List<Equipment> list1 = session.createQuery("from Equipment " + "WHERE name = null ").getResultList();
                for (Equipment eq : list1) {
                    Equipment equipment = session.get(Equipment.class, eq.getId());
                    equipment.setName("EQ" + equipment.getId());
                }
                session.getTransaction().commit();

            } else {
                int id = list.get(0).getId();
                Well well1 = session.get(Well.class, id);
                for (int i = 0; i < sumEquipmentsToAdd; i++) {
                    Equipment equipment = new Equipment();
                    well1.addEquipmentToWell(equipment);
                }
                session.getTransaction().commit();
                session = sessionFactory.openSession();
                session.beginTransaction();
                List<Equipment> list1 = session.createQuery("from Equipment " + "WHERE name = null ").getResultList();
                for (Equipment eq : list1) {
                    Equipment equipment = session.get(Equipment.class, eq.getId());
                    equipment.setName("EQUIP" + equipment.getId());
                }
                session.getTransaction().commit();
            }

        } finally {
            session.close();
            sessionFactory.close();
        }

    }


    @Override
    public void sumEquipmentsOfWell(String wells) {
        String[] strings = wells.split(",");
        String result = "";


        try {
            session = sessionFactory.openSession();
            session.beginTransaction();
            for(String string : strings){
                List<Well> list = session.createQuery("from Well "+ "where name = \'" + string + "\'").getResultList();
                List<Equipment> equipment = list.get(0).getEquipments();
//                System.out.println("Well name is " + list.get(0).getName() +" size is  "+ equipment.size());
                result += "Имя скважины " + list.get(0).getName() +" Количество оборудования  "+ equipment.size() + "\n" ;
            }
            System.out.println(result);
            session.getTransaction().commit();
        } finally {
            session.close();
            sessionFactory.close();
        }
    }

    @Override
    public void packToXml(String fileName) {

        SessionFactory sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(Well.class)
                .addAnnotatedClass(Equipment.class)
                .buildSessionFactory();
        Session session = null;

        try {

            session = sessionFactory.openSession();
            session.beginTransaction();

            List<Well> well = session.createQuery("from Well ").getResultList();

            XMLOutputFactory output = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = output.createXMLStreamWriter(new FileWriter(fileName + ".xml"));

            writer.writeStartElement("dbinfo");

            for (int i = 0; i < well.size(); i++) {

                writer.writeStartElement("well");
                writer.writeAttribute("name", well.get(i).getName());
                writer.writeAttribute("id", String.valueOf(well.get(i).getId()));

                List<Equipment> equip = well.get(i).getEquipments();

                for (int j = 0; j < equip.size(); j++) {
                    writer.writeEmptyElement("equip");
                    writer.writeAttribute("name", equip.get(j).getName());
                    writer.writeAttribute("id", String.valueOf(equip.get(j).getId()));
                }
                writer.writeEndElement();
            }
            writer.writeEndDocument();
            writer.flush();

        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    }



